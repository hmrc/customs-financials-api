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
import org.joda.time.DateTime
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
        ascending("expireAt"),
        IndexOptions().name("dataExpiry_idx").expireAfter(
          appConfig.mongoHistDocSearchTtl, TimeUnit.SECONDS).background(true)
      ),
      IndexModel(
        ascending("searchID"),
        IndexOptions()
          .name("SearchIDIndex")
          .unique(false)
          .sparse(false).background(true)
      )
    ),
    extraCodecs = Seq(
      Codecs.playFormatCodec(Params.paramsFormat),
      Codecs.playFormatCodec(SearchRequest.searchRequestFormat),
      Codecs.playFormatCodec(SearchResultStatus.searchResultStatusFormat)
    )
  ) {
  private val logger = play.api.Logger(getClass)

  private val searchIDFieldKey = "searchID"
  private val searchRequestsFieldKey = "searchRequests"
  private val currentEoriFieldKey = "currentEori"
  private val statementRequestIdFieldKey = "searchRequests.statementRequestId"
  private val  resultsFoundFieldKey = "resultsFound"
  private val searchStatusUpdateDateFieldKey = "searchStatusUpdateDate"
  private val searchFailureReasonCodeKey = "searchFailureReasonCode"

  def insertDocument(req: HistoricDocumentRequestSearch): Future[Boolean] = {
    val expireAtTS = DateTime.now().plusSeconds(appConfig.mongoHistDocSearchTtl.toInt)

    collection.insertOne(req.copy(expireAt = Option(expireAtTS))).toFuture() map { _ => false } recover {
      case _ => true
    }
  }

  def retrieveDocumentsForCurrentEori(currentEori: String): Future[Seq[HistoricDocumentRequestSearch]] =
    collection.find(equal(currentEoriFieldKey, currentEori)).toFuture() recover {
      case exception =>
        logger.error(s"Failed to retrieve the document for currentEori ::: $currentEori " +
          s"and error is ::: ${exception.getMessage}")
        Seq()
    }

  def retrieveDocumentForStatementRequestID(statementRequestID: String): Future[Option[HistoricDocumentRequestSearch]] =
    collection.find(equal(statementRequestIdFieldKey, statementRequestID)).headOption().recover {
      case exception =>
        logger.error(s"Failed to retrieve the document for $statementRequestID " +
          s"and error is ::: ${exception.getMessage}")
        None
    }

  /**
   * Updates the matching document (as per queryFilter) with the provided updates
   */
  def updateDocumentForQueryFilter(queryFilter: Bson,
                                   updates: Bson): Future[Option[HistoricDocumentRequestSearch]] =
    collection.findOneAndUpdate(
      filter = queryFilter,
      update = updates,
      new FindOneAndUpdateOptions().returnDocument(ReturnDocument.AFTER).upsert(false)).headOption().recover {
      case exception =>
        logger.error(s"Failed to update the document and error is ::: ${exception.getMessage}")
        None
    }

  /**
   * Retrieves the document using SearchId and
   * Updates the search requests array with the provided searchRequests
   */
  def updateSearchRequestForStatementRequestId(searchRequests: Set[SearchRequest],
                                               searchID: String): Future[Option[HistoricDocumentRequestSearch]] = {

    val queryFiler = Filters.equal(searchIDFieldKey, searchID)
    val updates = Updates.set(searchRequestsFieldKey, searchRequests)

    updateDocumentForQueryFilter(queryFiler, updates)
  }

  /**
   * Updates the resultsFound status to the provided updatedStatus
   * for the given searchID
   */
  def updateResultsFoundStatus(searchID: String,
                               updatedStatus: SearchResultStatus.Value):Future[Option[HistoricDocumentRequestSearch]] = {
    val queryFiler = Filters.equal(searchIDFieldKey, searchID)

    val updates = Updates.combine(
      Updates.set(resultsFoundFieldKey, updatedStatus.toString),
      Updates.set(searchStatusUpdateDateFieldKey, Utils.dateTimeAsIso8601(LocalDateTime.now))
    )

    updateDocumentForQueryFilter(queryFiler, updates)
  }

  /**
   * Updates the resultsFound status and  searchRequests to the provided values
   * for the given searchID
   */
  def updateSearchReqsAndResultsFoundStatus(searchID: String,
                                            searchRequests: Set[SearchRequest],
                                            updatedStatus: SearchResultStatus.Value): Future[Option[HistoricDocumentRequestSearch]] = {
    val queryFiler = Filters.equal(searchIDFieldKey, searchID)

    val updates = Updates.combine(
      Updates.set(searchRequestsFieldKey, searchRequests),
      Updates.set(resultsFoundFieldKey, updatedStatus.toString),
      Updates.set(searchStatusUpdateDateFieldKey, Utils.dateTimeAsIso8601(LocalDateTime.now))
    )

    updateDocumentForQueryFilter(queryFiler, updates)
  }

  def updateSearchRequestRetryCount(searchID: String,
                                    searchRequests: Set[SearchRequest],
                                    failureReason: String): Future[Option[HistoricDocumentRequestSearch]] = {
    val queryFiler = Filters.equal(searchIDFieldKey, searchID)

    val updates = Updates.combine(
      Updates.set(searchRequestsFieldKey, searchRequests),
      Updates.set(searchFailureReasonCodeKey, failureReason)
    )

    updateDocumentForQueryFilter(queryFiler, updates)
  }
}
