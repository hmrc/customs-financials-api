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

import java.util.concurrent.TimeUnit

import config.AppConfig
import domain.FileUploadMongo
import javax.inject.Inject
import models.css.UploadDocumentsRequest
import org.mongodb.scala.model.Indexes.ascending
import org.mongodb.scala.model.{IndexModel, IndexOptions}
import uk.gov.hmrc.mongo.MongoComponent
import uk.gov.hmrc.mongo.play.json.PlayMongoRepository

import scala.concurrent.{ExecutionContext, Future}

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

  override def set(payload: FileUploadMongo): Future[Boolean] = {
    collection.insertOne(payload).toFuture().map(_.wasAcknowledged())
  }

  override def get(): Future[Option[UploadDocumentsRequest]] =
    collection.find().headOption().map(_.map(_.uploadDocumentsRequest))
}

trait FileUploadCache {
    def set(payload: FileUploadMongo): Future[Boolean]
    def get(): Future[Option[UploadDocumentsRequest]]
}



