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

package services

import java.time.{LocalDateTime, ZoneOffset}
import java.util.UUID
import java.util.concurrent.TimeUnit
import com.mongodb.client.model.Updates
import config.AppConfig
import domain.FileUploadMongo
import javax.inject.Inject
import models.css.UploadedFilesRequest
import org.mongodb.scala.model.Filters.equal
import org.mongodb.scala.model.Indexes.ascending
import org.mongodb.scala.model.{Filters, IndexModel, IndexOptions}
import uk.gov.hmrc.mongo.MongoComponent
import uk.gov.hmrc.mongo.play.json.Codecs.logger
import uk.gov.hmrc.mongo.play.json.PlayMongoRepository
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

class DefaultFileUploadCache @Inject()(
  mongoComponent: MongoComponent,
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
      )
    )) with FileUploadCache  {

  override def enqueueFileUploadJob(uploadDocumentsRequest: UploadedFilesRequest): Future[Boolean] = {
    val timeStamp = LocalDateTime.now
    val id = UUID.randomUUID().toString
    val record = FileUploadMongo(id, uploadDocumentsRequest, processing = false, timeStamp)
    val result: Future[Boolean] = collection.insertOne(record).toFuture().map(_.wasAcknowledged())
    result.onComplete {
      case Failure(error) =>
        logger.error(s"Could not enqueue FileUploadMongo: ${error.getMessage}")
      case Success(_) =>
        logger.info(s"Successfully enqueued FileUploadMongo:  $timeStamp : $uploadDocumentsRequest")
    }
    result
  }

  override def nextJob: Future[Option[UploadedFilesRequest]] = {
    collection.findOneAndUpdate(
      equal("processing", false),
      Updates.set("processing", true)
    ).toFutureOption().map {
      case fileUploadMongo@Some(value) =>
        logger.info(s"Successfully marked latest FileUploadMongo for processing: ${value}")
        fileUploadMongo.map(_.uploadDocumentsRequest)
      case None =>
        logger.debug(s"FileUploadMongo queue is empty")
        None
    }.recover {
      case e =>
        logger.error(s"Marking FileUploadMongo for processing failed. Unexpected MongoDB error: $e")
        throw e
    }
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
    val maxAge = getLocalDateTime.minusMinutes(config.fileUploadMaxAgeMins)
    val updates = Updates.set("processing", false)
    collection.updateMany(
      filter = Filters.and(
        Filters.equal("processing", true),
        Filters.lt("lastUpdated", maxAge.toInstant(ZoneOffset.UTC))
      ),
      updates
    ).toFuture().map(_ => ())
  }

  def getLocalDateTime: LocalDateTime = LocalDateTime.now
}

trait FileUploadCache {
    def enqueueFileUploadJob(payload: UploadedFilesRequest): Future[Boolean]
    def nextJob: Future[Option[UploadedFilesRequest]]
    def deleteJob(id: String): Future[Boolean]
    def resetProcessing: Future[Unit]
}



