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

import models.{HistoricDocumentRequestSearch, Params, SearchRequest, SearchResultStatus}
import play.api.{Application, inject}
import utils.Utils.emptyString
import utils.{SpecBase, Utils}

import java.time.LocalDateTime
import java.util.UUID
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class HistoricDocumentRequestSearchCacheServiceSpec extends SpecBase {

  "saveHistoricDocumentRequestSearch" should {
    "return true when document is insert successfully" in new Setup {
      when(mockHistDocReqSearchCache.insertDocument(any)).thenReturn(Future.successful(true))

      val service: HistoricDocumentRequestSearchCacheService =
        app.injector.instanceOf[HistoricDocumentRequestSearchCacheService]

      service.saveHistoricDocumentRequestSearch(histDocRequestSearch).map {
        result => result mustBe true
      }
    }

    "return false when document is not inserted" in new Setup {
      when(mockHistDocReqSearchCache.insertDocument(any)).thenReturn(Future.successful(false))

      val service: HistoricDocumentRequestSearchCacheService =
        app.injector.instanceOf[HistoricDocumentRequestSearchCacheService]

      service.saveHistoricDocumentRequestSearch(histDocRequestSearch).map {
        result => result mustBe false
      }
    }
  }

  "retrieveHistDocRequestSearchDocsForCurrentEori" should {
    "retrieve the documents when documents are present in the DB" in new Setup {
      when(mockHistDocReqSearchCache.retrieveDocumentsForCurrentEori(any)).thenReturn(Future.successful(
        Seq(histDocRequestSearch, histDocRequestSearch)))

      val service: HistoricDocumentRequestSearchCacheService =
        app.injector.instanceOf[HistoricDocumentRequestSearchCacheService]

      service.retrieveHistDocRequestSearchDocsForCurrentEori("test_eori").map {
        records => records mustBe Seq(histDocRequestSearch, histDocRequestSearch)
      }
    }

    "return the empty documents when documents are not present in the DB" in new Setup {
      when(mockHistDocReqSearchCache.retrieveDocumentsForCurrentEori(any)).thenReturn(Future.successful(
        Seq()))

      val service: HistoricDocumentRequestSearchCacheService =
        app.injector.instanceOf[HistoricDocumentRequestSearchCacheService]

      service.retrieveHistDocRequestSearchDocsForCurrentEori("test_eori").map {
        records => records mustBe Seq()
      }
    }
  }

  "retrieveHistDocRequestSearchDocForStatementReqId" should {
    "retrieve the document when document is present in the DB" in new Setup {
      when(mockHistDocReqSearchCache.retrieveDocumentForStatementRequestID(any)).thenReturn(Future.successful(
        Option(histDocRequestSearch)))

      val service: HistoricDocumentRequestSearchCacheService =
        app.injector.instanceOf[HistoricDocumentRequestSearchCacheService]

      service.retrieveHistDocRequestSearchDocForStatementReqId(
        "5b89895-f0da-4472-af5a-d84d340e7mn5").map {
        record => record.get mustBe histDocRequestSearch
      }
    }

    "return None when document is not present in the DB" in new Setup {
      when(mockHistDocReqSearchCache.retrieveDocumentForStatementRequestID(any)).thenReturn(Future.successful(None))

      val service: HistoricDocumentRequestSearchCacheService =
        app.injector.instanceOf[HistoricDocumentRequestSearchCacheService]

      service.retrieveHistDocRequestSearchDocForStatementReqId(
        "5b89895-f0da-4472-af5a-d84d340e7mn5").map {
        record => record mustBe None
      }
    }
  }

  "updateSearchRequestForStatementRequestId" should {
    "update the doc correctly" in new Setup {

      val statReqId = "5b89895-f0da-4472-af5a-d84d340e7mn5"
      val searchFailureReasonCode = "AWSUnreachable"
      val searchDtTime: String = Utils.dateTimeAsIso8601(LocalDateTime.now)

      val updatedSearchRequests: Set[SearchRequest] = searchRequests.map {
        sr =>
          if (sr.statementRequestId.equals(statReqId)) sr.copy(
            searchSuccessful = SearchResultStatus.no,
            searchDateTime = searchDtTime,
            searchFailureReasonCode = searchFailureReasonCode) else sr
      }

      when(mockHistDocReqSearchCache.updateSearchRequestForStatementRequestId(
        updatedSearchRequests,
        searchID.toString)).thenReturn(Future.successful(
        Option(histDocRequestSearch.copy(searchRequests = updatedSearchRequests))))

      val service: HistoricDocumentRequestSearchCacheService =
        app.injector.instanceOf[HistoricDocumentRequestSearchCacheService]

      service.updateSearchRequestForStatementRequestId(
        histDocRequestSearch,
        statReqId,
        searchFailureReasonCode).map {
        optDoc => {
          val doc = optDoc.get
          val updatedSR = doc.searchRequests.find(x => x.statementRequestId == statReqId).get
          doc.searchID.toString mustBe searchID.toString
          updatedSR.searchFailureReasonCode mustBe searchFailureReasonCode
          updatedSR.searchDateTime mustBe searchDtTime
        }
      }

      verify(mockHistDocReqSearchCache, times(1)).updateSearchRequestForStatementRequestId(
        updatedSearchRequests, searchID.toString)
    }
  }

  "updateResultsFoundStatusToNoIfEligible" should {
    "update the resultsFound to no if all the search requests have no for searchSuccessful field" in new Setup {
      val searchFailureReasonCode = "AWSUnreachable"
      val searchDtTime: String = Utils.dateTimeAsIso8601(LocalDateTime.now)

      val updatedSearchRequests: Set[SearchRequest] = searchRequests.map {
        sr =>
          sr.copy(
            searchSuccessful = SearchResultStatus.no,
            searchDateTime = searchDtTime,
            searchFailureReasonCode = searchFailureReasonCode)
      }

      val histDocSearchWithAllSearchRequestsProcessed: HistoricDocumentRequestSearch =
        histDocRequestSearch.copy(searchRequests = updatedSearchRequests)

      when(mockHistDocReqSearchCache.updateResultsFoundStatus(
        histDocSearchWithAllSearchRequestsProcessed.searchID.toString,
        SearchResultStatus.no
      )).thenReturn(Future.successful(
        Option(histDocRequestSearch.copy(searchRequests = updatedSearchRequests))))

      val service: HistoricDocumentRequestSearchCacheService =
        app.injector.instanceOf[HistoricDocumentRequestSearchCacheService]

      service.updateResultsFoundStatusToNoIfEligible(histDocSearchWithAllSearchRequestsProcessed).map {
        optDoc => {
          val doc = optDoc.get

          doc.resultsFound mustBe SearchResultStatus.no
          doc.searchStatusUpdateDate must not be empty
        }
      }

      verify(mockHistDocReqSearchCache, times(1)).updateResultsFoundStatus(
        histDocSearchWithAllSearchRequestsProcessed.searchID.toString,
        SearchResultStatus.no)
    }

    "not update the resultsFound if all the search requests do not have no for searchSuccessful field" in new Setup {
      val service: HistoricDocumentRequestSearchCacheService =
        app.injector.instanceOf[HistoricDocumentRequestSearchCacheService]

      service.updateResultsFoundStatusToNoIfEligible(histDocRequestSearch).map {
        optDoc => {
          val doc = optDoc.get

          doc.resultsFound mustBe SearchResultStatus.inProcess
          doc.searchStatusUpdateDate mustBe empty
        }
      }
    }

    "not update the resultsFound to no if one or more search requests do not have no for searchSuccessful field" in new Setup {
      val service: HistoricDocumentRequestSearchCacheService =
        app.injector.instanceOf[HistoricDocumentRequestSearchCacheService]

      val searchRequestsOb: Set[SearchRequest] = Set(
        SearchRequest(
          "GB123456789012", "5b89895-f0da-4472-af5a-d84d340e7mn5", SearchResultStatus.inProcess, emptyString, emptyString, 0),
        SearchRequest(
          "GB234567890121", "5c79895-f0da-4472-af5a-d84d340e7mn6", SearchResultStatus.no,
          Utils.dateTimeAsIso8601(LocalDateTime.now), "AWSUnreachable", 0)
      )

      service.updateResultsFoundStatusToNoIfEligible(histDocRequestSearch.copy(searchRequests = searchRequestsOb)).map {
        optDoc => {
          val doc = optDoc.get

          doc.resultsFound mustBe SearchResultStatus.inProcess
          doc.searchStatusUpdateDate mustBe empty
        }
      }
    }
  }

  "processSDESNotificationForStatReqId" should {
    "update the doc correctly" in new Setup {
      val statReqId = "5b89895-f0da-4472-af5a-d84d340e7mn5"
      val searchDtTime: String = Utils.dateTimeAsIso8601(LocalDateTime.now)

      val updatedSearchRequests: Set[SearchRequest] = searchRequests.map {
        sr =>
          if (sr.statementRequestId.equals(statReqId)) sr.copy(
            searchSuccessful = SearchResultStatus.yes,
            searchDateTime = searchDtTime) else sr
      }

      when(mockHistDocReqSearchCache.updateSearchReqsAndResultsFoundStatus(
        any, any, any)).thenReturn(Future.successful(
        Option(histDocRequestSearch.copy(resultsFound = SearchResultStatus.yes,
          searchRequests = updatedSearchRequests,
          searchStatusUpdateDate = searchDtTime))))

      val service: HistoricDocumentRequestSearchCacheService =
        app.injector.instanceOf[HistoricDocumentRequestSearchCacheService]

      service.processSDESNotificationForStatReqId(
        histDocRequestSearch,
        statReqId).map {
        optDoc => {
          val doc = optDoc.get
          val updatedSR = doc.searchRequests.find(x => x.statementRequestId == statReqId).get

          doc.searchID.toString mustBe searchID.toString
          doc.resultsFound mustBe SearchResultStatus.yes
          doc.searchStatusUpdateDate mustBe searchDtTime
          updatedSR.searchSuccessful mustBe SearchResultStatus.yes
          updatedSR.searchDateTime mustBe searchDtTime
        }
      }

      verify(mockHistDocReqSearchCache, times(1)).updateSearchReqsAndResultsFoundStatus(
        searchID.toString,
        updatedSearchRequests,
        SearchResultStatus.yes)
    }
  }

  trait Setup {
    val mockHistDocReqSearchCache: HistoricDocumentRequestSearchCache =
      mock[HistoricDocumentRequestSearchCache]

    val app: Application = application().overrides(
      inject.bind[HistoricDocumentRequestSearchCache].toInstance(mockHistDocReqSearchCache)
    ).build()

    val searchID: UUID = UUID.randomUUID()
    val userId: String = "test_userId"
    val resultsFound: SearchResultStatus.Value = SearchResultStatus.inProcess
    val searchStatusUpdateDate: String = emptyString
    val currentEori: String = "GB123456789012"
    val params: Params = Params("2", "2021", "4", "2021", "DutyDefermentStatement", "1234567")
    val searchRequests: Set[SearchRequest] = Set(
      SearchRequest(
        "GB123456789012", "5b89895-f0da-4472-af5a-d84d340e7mn5", SearchResultStatus.inProcess, emptyString, emptyString, 0),
      SearchRequest(
        "GB234567890121", "5c79895-f0da-4472-af5a-d84d340e7mn6", SearchResultStatus.inProcess, emptyString, emptyString, 0)
    )

    val histDocRequestSearch: HistoricDocumentRequestSearch =
      HistoricDocumentRequestSearch(searchID,
        resultsFound,
        searchStatusUpdateDate,
        currentEori,
        params,
        searchRequests)
  }
}
