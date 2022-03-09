/*
 * Copyright 2022 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package services.dec64

import java.time.ZoneOffset
import java.util.concurrent.TimeUnit
import com.mongodb.client.model.Updates
import config.AppConfig
import domain.FileUploadMongo
import javax.inject.Inject
import models.dec64.{FileUploadRequest, UploadedFile}
import org.mongodb.scala.model.Filters.equal
import org.mongodb.scala.model.Indexes.ascending
import org.mongodb.scala.model.{Filters, IndexModel, IndexOptions}
import services.DateTimeService
import uk.gov.hmrc.mongo.MongoComponent
import uk.gov.hmrc.mongo.play.json.Codecs.logger
import uk.gov.hmrc.mongo.play.json.PlayMongoRepository
import utils.RandomUUIDGenerator
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

class DefaultFileUploadCache @Inject()(
                                        mongoComponent: MongoComponent,
                                        dateTimeService: DateTimeService,
                                        randomUUIDGenerator: RandomUUIDGenerator,
                                        config: AppConfig)(implicit executionContext: ExecutionContext)
  extends PlayMongoRepository[FileUploadMongo](
    collectionName = config.fileUploadCacheCollectionName,
    mongoComponent = mongoComponent,
    domainFormat = FileUploadMongo.format,
    indexes = Seq(
      IndexModel(
        ascending("receivedAt"),
        IndexOptions().name("file-upload-cache-received-at-index")
          .expireAfter(config.dbTimeToLiveInSeconds, TimeUnit.SECONDS)
      ),
      IndexModel(
        ascending("uploadDocumentsRequest.id"),
        IndexOptions().name("document-id-index")
      )
    )) with FileUploadCache {

  override def enqueueFileUploadJob(request: FileUploadRequest): Future[Boolean] = {
   Future.sequence(
   request.uploadedFiles.zipWithIndex.map { case (file: UploadedFile, index: Int) =>
      enqueueFiles(Seq(mongoRecord(file, index, request)))
   }).map { v => !v.contains(false)
   }
  }

  private def mongoRecord(uploadedFile: UploadedFile, index: Int, uploadDocumentsRequest: FileUploadRequest): FileUploadMongo = {
    val id = randomUUIDGenerator.generateUuid
    val timeStamp = dateTimeService.now()
    val fileUploadDetail = uploadDocumentsRequest.toFileUploadDetail(uploadedFile, index)
    FileUploadMongo(id, processing = false, timeStamp, fileUploadDetail)
  }

  override def enqueueFiles(fileUploadMongo: Seq[FileUploadMongo]): Future[Boolean] = {
    val result: Future[Boolean] = collection.insertMany(fileUploadMongo).toFuture().map(_.wasAcknowledged())
    result.onComplete {
      case Failure(error) =>
        logger.error(s"Could not enqueue FileUploadMongo record: ${error.getMessage}")
      case Success(_) =>
        logger.info(s"Successfully enqueued FileUploadMongo record: : $fileUploadMongo")
    }
    result
  }

  override def nextJob: Future[Option[FileUploadMongo]] = {
    collection.findOneAndUpdate(
      equal("processing", false),
      Updates.set("processing", true)
    ).toFutureOption()
  }

  override def deleteJob(id: String): Future[Boolean] = {
    val result = collection.deleteOne(equal("_id", id)).toFuture().map(_.wasAcknowledged())
    result.onComplete {
      case Success(_) =>
        logger.info(s"Successfully deleted FileUploadMongo job: $id")
      case Failure(error) =>
        logger.error(s"Could not delete completed FileUploadMongo job: $error")
    }
    result
  }

  override def resetProcessing: Future[Unit] = {
    val maxAge = dateTimeService.now().minusMinutes(config.fileUploadMaxAgeMins)
    val updates = Updates.set("processing", false)
    collection.updateMany(
      filter = Filters.and(
        Filters.equal("processing", true),
        Filters.lt("receivedAt", maxAge.toInstant(ZoneOffset.UTC))
      ),
      updates
    ).toFuture().map(_ => ())
  }

  override def resetProcessingFailedUpload(id: String): Future[Boolean] = {
    collection.updateOne(equal("_id", id),
      Updates.combine(
        Updates.inc("failedSubmission", 1),
        Updates.set("processing", false)
      )
    ).toFuture().map(_.wasAcknowledged())
  }
}

trait FileUploadCache {
  def enqueueFileUploadJob(payload: FileUploadRequest): Future[Boolean]

  def enqueueFiles(fileUploadMongo: Seq[FileUploadMongo]): Future[Boolean]

  def nextJob: Future[Option[FileUploadMongo]]

  def deleteJob(id: String): Future[Boolean]

  def resetProcessing: Future[Unit]

  def resetProcessingFailedUpload(id: String): Future[Boolean]
}



