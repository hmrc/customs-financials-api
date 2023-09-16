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

import com.mongodb.client.model.FindOneAndUpdateOptions
import com.mongodb.client.model.Indexes.ascending
import config.AppConfig
import models.{HistoricDocumentRequestSearch, Params, SearchRequest, SearchStatus}
import org.mongodb.scala.bson.conversions.Bson
import org.mongodb.scala.model.Filters.equal
import org.mongodb.scala.model.{Filters, IndexModel, IndexOptions, Updates}
import uk.gov.hmrc.mongo.MongoComponent
import uk.gov.hmrc.mongo.play.json.{Codecs, PlayMongoRepository}
import utils.Utils

import java.time.LocalDateTime
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class HistoricDocumentRequestSearchCache @Inject()(appConfig: AppConfig,
                                                   mongoComponent: MongoComponent)
                                                  (implicit val ec: ExecutionContext)
  extends PlayMongoRepository[HistoricDocumentRequestSearch](
    mongoComponent = mongoComponent,
    collectionName = appConfig.mongoHistDocSearchCollectionName,
    domainFormat = HistoricDocumentRequestSearch.historicDocumentRequestSearchFormat,
    indexes = Seq(
      IndexModel(
        ascending("currentEori"),
        IndexOptions()
          .name("CurrentEoriIndex")
          .unique(false)
          .sparse(false)
          .expireAfter(appConfig.mongoHistDocSearchTtl, TimeUnit.SECONDS).background(true)
      ), IndexModel(
        ascending("searchID"),
        IndexOptions()
          .name("SearchIDIndex")
          .unique(false)
          .sparse(false).background(true)
      )
    ),
    extraCodecs = Seq(
      Codecs.playFormatCodec(Params.paramsFormat),
      Codecs.playFormatCodec(SearchRequest.searchRequestFormat)
    )
  ) {

  private val docFieldSearchID = "searchID"
  private val docFieldSearchRequests = "searchRequests"
  private val docFieldCurrentEori = "currentEori"
  private val docFieldStatementRequestId = "searchRequests.statementRequestId"

  def insertDocument(req: HistoricDocumentRequestSearch): Future[Boolean] =
    collection.insertOne(req).toFuture() map { _ => false } recover { case _ => true }

  def retrieveDocumentsForCurrentEori(currentEori: String): Future[Seq[HistoricDocumentRequestSearch]] =
    collection.find(equal(docFieldCurrentEori, currentEori)).toFuture()

  def retrieveDocumentForStatementRequestID(statementRequestID: String): Future[Option[HistoricDocumentRequestSearch]] =
    collection.find(equal(docFieldStatementRequestId, statementRequestID)).headOption()

  /**
   * Updates the matching document (as per queryFilter) with the provided updates
   */
  def updateDocumentForQueryFilter(queryFilter: Bson,
                                   updates: Bson): Future[Option[HistoricDocumentRequestSearch]] =
    collection.findOneAndUpdate(
      filter = queryFilter,
      update = updates,
      new FindOneAndUpdateOptions().upsert(false)).headOption()

  def updateSearchRequestForStatementRequestId(searchRequests: Set[SearchRequest],
                                               searchID: String,
                                               statementRequestID: String,
                                               failureReason: String): Future[Option[HistoricDocumentRequestSearch]] = {

    val queryFiler = Filters.equal(docFieldSearchID, searchID)

    val updatedSearchRequests: Set[SearchRequest] = searchRequests.map {
      sr =>
        if (sr.statementRequestId.equals(statementRequestID)) sr.copy(
          searchSuccessful = SearchStatus.no.toString,
          searchDateTime = Utils.dateTimeAsIso8601(LocalDateTime.now),
          searchFailureReasonCode = failureReason) else sr
    }

    val updates = Updates.set(docFieldSearchRequests, updatedSearchRequests)

    updateDocumentForQueryFilter(queryFiler, updates)
  }
}
