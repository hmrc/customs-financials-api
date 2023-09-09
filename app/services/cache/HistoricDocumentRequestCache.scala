/*
 * Copyright 2023 HM Revenue & Customs
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

package services.cache

import com.mongodb.client.model.Indexes.ascending
import config.AppConfig
import models.HistoricDocumentRequestSearch
import org.mongodb.scala.bson.BsonDocument
import org.mongodb.scala.model.Filters.equal
import org.mongodb.scala.model.{IndexModel, IndexOptions}
import uk.gov.hmrc.mongo.MongoComponent
import uk.gov.hmrc.mongo.play.json.PlayMongoRepository

import java.util.concurrent.TimeUnit
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class HistoricDocumentRequestCache @Inject()(
                                              appConfig: AppConfig,
                                              mongoComponent: MongoComponent)
                                            (implicit val ec: ExecutionContext) extends PlayMongoRepository[HistoricDocumentRequestSearch](
  mongoComponent = mongoComponent,
  collectionName = appConfig.mongoHistDocSearchCollectionName,
  domainFormat = HistoricDocumentRequestSearch.historicDocumentRequestSearchFormat,
  indexes = Seq(
    IndexModel(
      ascending("userId"),
      IndexOptions()
        .name("UserIdIndex")
        .unique(false)
        .sparse(false)
        .expireAfter(appConfig.mongoHistDocSearchTtl, TimeUnit.SECONDS)
    )
  )
) {
  def insertHistoricDocRequestSearchRecord(req: HistoricDocumentRequestSearch): Future[Boolean] =
    collection.insertOne(req).toFuture() map { _ => false } recover { case _ => true }

 /* def upsertHistoricDocRequestSearchRecord(req: HistoricDocumentRequestSearch) =
    collection.updateOne(filter = Filters.and(),req, new FindOneAndUpdateOptions().upsert(true)).toFuture().map(_ => ())*/

  def retrieveHistoricDocRequestSearchRecord(userId: String): Future[Seq[HistoricDocumentRequestSearch]] =
    collection.find(equal("userId", userId)).toFuture()

  def deleteAllRecords: Future[String] =
    collection.deleteMany(BsonDocument()).toFuture() map { _ => "All records removed" }
}
