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
import com.mongodb.client.model.{FindOneAndUpdateOptions, ReturnDocument}
import config.AppConfig
import models.{HistoricDocumentRequestSearch, Params, SearchRequest, SearchResultStatus}
import org.mongodb.scala.bson.conversions.Bson
import org.mongodb.scala.model.Filters.equal
import org.mongodb.scala.model.{Filters, IndexModel, IndexOptions, Updates}
import org.mongodb.scala.{ObservableFuture, SingleObservableFuture}
import uk.gov.hmrc.mongo.MongoComponent
import uk.gov.hmrc.mongo.play.json.{Codecs, PlayMongoRepository}
import utils.Utils

import java.time.LocalDateTime
import java.util.concurrent.TimeUnit
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class HistoricDocumentRequestSearchCache @Inject() (appConfig: AppConfig, mongoComponent: MongoComponent)(implicit
  val ec: ExecutionContext
) extends PlayMongoRepository[HistoricDocumentRequestSearch](
      mongoComponent = mongoComponent,
      collectionName = appConfig.mongoHistDocSearchCollectionName,
      domainFormat = HistoricDocumentRequestSearch.historicDocumentRequestSearchFormat,
      indexes = Seq(
        IndexModel(
          ascending("expireAt"),
          IndexOptions()
            .name("dataExpiry_idx")
            .expireAfter(appConfig.mongoHistDocSearchTtl, TimeUnit.SECONDS)
            .background(true)
        ),
        IndexModel(
          ascending("searchID"),
          IndexOptions()
            .name("SearchIDIndex")
            .unique(false)
            .sparse(false)
            .background(true)
        )
      ),
      extraCodecs = Seq(
        Codecs.playFormatCodec(Params.paramsFormat),
        Codecs.playFormatCodec(SearchRequest.searchRequestFormat),
        Codecs.playFormatCodec(SearchResultStatus.searchResultStatusFormat)
      )
    ) {
  private val searchIDFieldKey               = "searchID"
  private val searchRequestsFieldKey         = "searchRequests"
  private val currentEoriFieldKey            = "currentEori"
  private val statementRequestIdFieldKey     = "searchRequests.statementRequestId"
  private val resultsFoundFieldKey           = "resultsFound"
  private val searchStatusUpdateDateFieldKey = "searchStatusUpdateDate"

  def insertDocument(req: HistoricDocumentRequestSearch): Future[Boolean] = {
    val expireAtTS = LocalDateTime.now().plusSeconds(appConfig.mongoHistDocSearchTtl.toInt)

    collection.insertOne(req.copy(expireAt = Option(expireAtTS))).toFuture() map { _ => false } recover { case _ =>
      true
    }
  }

  def retrieveDocumentsForCurrentEori(currentEori: String): Future[Seq[HistoricDocumentRequestSearch]] =
    collection.find(equal(currentEoriFieldKey, currentEori)).toFuture()

  def retrieveDocumentForStatementRequestID(statementRequestID: String): Future[Option[HistoricDocumentRequestSearch]] =
    collection.find(equal(statementRequestIdFieldKey, statementRequestID)).headOption()

  def updateDocumentForQueryFilter(queryFilter: Bson, updates: Bson): Future[Option[HistoricDocumentRequestSearch]] =
    collection
      .findOneAndUpdate(
        filter = queryFilter,
        update = updates,
        new FindOneAndUpdateOptions().returnDocument(ReturnDocument.AFTER).upsert(false)
      )
      .headOption()

  def updateSearchRequestForStatementRequestId(
    searchRequests: Set[SearchRequest],
    searchID: String
  ): Future[Option[HistoricDocumentRequestSearch]] = {

    val queryFiler = Filters.equal(searchIDFieldKey, searchID)
    val updates    = Updates.set(searchRequestsFieldKey, searchRequests)

    updateDocumentForQueryFilter(queryFiler, updates)
  }

  def updateResultsFoundStatus(
    searchID: String,
    updatedStatus: SearchResultStatus.Value
  ): Future[Option[HistoricDocumentRequestSearch]] = {
    val queryFiler = Filters.equal(searchIDFieldKey, searchID)

    val updates = Updates.combine(
      Updates.set(resultsFoundFieldKey, updatedStatus.toString),
      Updates.set(searchStatusUpdateDateFieldKey, Utils.dateTimeAsIso8601(LocalDateTime.now))
    )

    updateDocumentForQueryFilter(queryFiler, updates)
  }

  def updateSearchReqsAndResultsFoundStatus(
    searchID: String,
    searchRequests: Set[SearchRequest],
    updatedStatus: SearchResultStatus.Value
  ): Future[Option[HistoricDocumentRequestSearch]] = {
    val queryFiler = Filters.equal(searchIDFieldKey, searchID)

    val updates = Updates.combine(
      Updates.set(searchRequestsFieldKey, searchRequests),
      Updates.set(resultsFoundFieldKey, updatedStatus.toString),
      Updates.set(searchStatusUpdateDateFieldKey, Utils.dateTimeAsIso8601(LocalDateTime.now))
    )

    updateDocumentForQueryFilter(queryFiler, updates)
  }

}
