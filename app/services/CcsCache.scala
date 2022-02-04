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

import javax.inject.Inject
import models.{ErrorResponse, UnknownException}
import models.css.{CcsSubmissionPayload, FileUploadCache}
import org.joda.time.DateTime
import org.mongodb.scala.model.Indexes.ascending
import org.mongodb.scala.model.{IndexModel, IndexOptions}
import uk.gov.hmrc.mongo.MongoComponent
import uk.gov.hmrc.mongo.play.json.Codecs.logger
import uk.gov.hmrc.mongo.play.json.PlayMongoRepository
import scala.concurrent.{ExecutionContext, Future}


class CcsCache @Inject()()(mongoComponent: MongoComponent)
                        (implicit executionContext: ExecutionContext) extends PlayMongoRepository[FileUploadCache](


  collectionName = "ccs-uploaded-file-work-item",
  mongoComponent = mongoComponent,
  domainFormat = FileUploadCache.format,
  indexes = Seq(
    IndexModel(
      ascending("receivedAt"),
      IndexOptions().name("receivedAtIndex")
        .unique(true)
        .sparse(true)))
) {


  def set(ccsSubmissionPayload: CcsSubmissionPayload): Future[Unit] = {
    val record = FileUploadCache(ccsSubmissionPayload, DateTime.now())
    collection.insertOne(record).toFuture().map {
      result => result.wasAcknowledged()
    }
  }

  def get(): Future[Either[ErrorResponse, Option[FileUploadCache]]] = {
    collection.find().headOption.map {
      cssSubmission => Right(cssSubmission)
    }.recover {
      case e: Exception =>
        logger.error(s"[$collectionName][get] Exception from Mongo. Exception: ${e.getMessage}")
        Left(UnknownException)
    }
  }
}