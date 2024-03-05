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

import models.{HistoricDocumentRequestSearch, SearchRequest, SearchResultStatus}
import utils.Utils

import java.time.LocalDateTime
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class HistoricDocumentRequestSearchCacheService @Inject()(historicDocRequestCache: HistoricDocumentRequestSearchCache)
                                                         (implicit ec: ExecutionContext) {

  def saveHistoricDocumentRequestSearch(req: HistoricDocumentRequestSearch): Future[Boolean] =
    historicDocRequestCache.insertDocument(req)

  def retrieveHistDocRequestSearchDocsForCurrentEori(currentEori: String): Future[Seq[HistoricDocumentRequestSearch]] =
    historicDocRequestCache.retrieveDocumentsForCurrentEori(currentEori)

  def retrieveHistDocRequestSearchDocForStatementReqId(statReqID: String): Future[Option[HistoricDocumentRequestSearch]] =
    historicDocRequestCache.retrieveDocumentForStatementRequestID(statReqID)

  def updateSearchRequestForStatementRequestId(req: HistoricDocumentRequestSearch,
                                               statementRequestID: String,
                                               failureReason: String): Future[Option[HistoricDocumentRequestSearch]] = {

    val updatedSearchRequests: Set[SearchRequest] = req.searchRequests.map {
      sr =>
        if (sr.statementRequestId.equals(statementRequestID)) {
          sr.copy(
            searchSuccessful = SearchResultStatus.no,
            searchDateTime = Utils.dateTimeAsIso8601(LocalDateTime.now),
            searchFailureReasonCode = failureReason)
        } else {
          sr
        }
    }

    historicDocRequestCache.updateSearchRequestForStatementRequestId(
      updatedSearchRequests,
      req.searchID.toString)
  }

  def updateResultsFoundStatusToNoIfEligible(req: HistoricDocumentRequestSearch): Future[Option[HistoricDocumentRequestSearch]] = {
    val isAllSearchRequestsSearchStatusNo: Boolean = !req.searchRequests.exists(
      sr => sr.searchSuccessful == SearchResultStatus.inProcess || sr.searchSuccessful == SearchResultStatus.yes)

    if (isAllSearchRequestsSearchStatusNo) {
      historicDocRequestCache.updateResultsFoundStatus(req.searchID.toString, SearchResultStatus.no)
    } else {
      Future(Option(req))
    }
  }

  def processSDESNotificationForStatReqId(req: HistoricDocumentRequestSearch,
                                          statementRequestID: String): Future[Option[HistoricDocumentRequestSearch]] = {

    val updatedSearchRequests: Set[SearchRequest] = req.searchRequests.map {
      sr =>
        if (sr.statementRequestId.equals(statementRequestID) &&
          sr.searchSuccessful == SearchResultStatus.inProcess) {
          sr.copy(
            searchSuccessful = SearchResultStatus.yes,
            searchDateTime = Utils.dateTimeAsIso8601(LocalDateTime.now)
          )
        } else {
          sr
        }
    }

    historicDocRequestCache.updateSearchReqsAndResultsFoundStatus(req.searchID.toString,
      updatedSearchRequests,
      SearchResultStatus.yes)
  }

  def updateSearchRequestRetryCount(statementRequestID: String,
                                    failureReason: String,
                                    searchId: String,
                                    searchRequests: Set[SearchRequest]): Future[Option[HistoricDocumentRequestSearch]] = {
    val updatedSearchRequests = searchRequests.map {
      sr =>
        if (sr.statementRequestId == statementRequestID &&
          sr.searchSuccessful == SearchResultStatus.inProcess) {
          sr.copy(searchFailureReasonCode = failureReason, failureRetryCount = sr.failureRetryCount + 1)
        } else {
          sr
        }
    }

    historicDocRequestCache.updateSearchRequestForStatementRequestId(
      updatedSearchRequests,
      searchId)
  }

}
