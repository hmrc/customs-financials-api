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

package controllers

import connectors.SecureMessageConnector
import models._
import models.requests.StatementSearchFailureNotificationRequest
import models.requests.StatementSearchFailureNotificationRequest.ssfnRequestFormat
import models.responses.{ErrorCode, StatementSearchFailureNotificationErrorResponse}
import org.mockito.Mockito
import play.api.http.Status.{BAD_REQUEST, INTERNAL_SERVER_ERROR, NO_CONTENT, UNAUTHORIZED}
import play.api.libs.json.{JsObject, Json}
import play.api.mvc._
import play.api.test.CSRFTokenHelper.CSRFFRequestHeader
import play.api.test.FakeRequest
import play.api.test.Helpers.{contentAsJson, route, running, status}
import play.api.{Application, inject}
import services.HistoricDocumentService
import services.cache.HistoricDocumentRequestSearchCacheService
import utils.Utils.emptyString
import utils.{JSONSchemaValidator, SpecBase, Utils}

import java.time.LocalDateTime
import java.util.UUID
import scala.concurrent.Future

class StatementSearchFailureNotificationControllerSpec extends SpecBase {
  "processNotification" should {

    "return BAD_REQUEST with correct error response when statementRequestId is not present in the db" in new Setup {
      when(mockHistDocReqSearchCacheService.retrieveHistDocRequestSearchDocForStatementReqId(any)).thenReturn(
        Future.successful(None)
      )

      running(app) {
        val response = route(app, validRequest).value
        status(response) mustBe BAD_REQUEST

        contentAsJson(response) mustBe Json.toJson(StatementSearchFailureNotificationErrorResponse(
          None, ErrorCode.code400, correlationId, Option(incomingStatementReqId)))
      }
    }

    "return 204 when req is valid and one searchRequest is already in no status and other is inProcess" in new Setup {
      when(mockHistDocReqSearchCacheService.retrieveHistDocRequestSearchDocForStatementReqId(
        incomingStatementReqId)).thenReturn(
        Future.successful(Option(historicDocumentRequestSearchDoc))
      )

      val updatedSearchRequests: Set[SearchRequest] = Set(
        SearchRequest(
          "GB123456789012", incomingStatementReqId, SearchResultStatus.no, emptyString, emptyString, 0),
        SearchRequest(
          "GB234567890121", "5c79895-f0da-4472-af5a-d84d340e7mn6", SearchResultStatus.inProcess,
          emptyString, emptyString, 0)
      )

      when(mockHistDocReqSearchCacheService.updateSearchRequestForStatementRequestId(
        historicDocumentRequestSearchDoc,
        incomingStatementReqId,
        "NoDocumentsFound"
      )).thenReturn(Future.successful(Some(historicDocumentRequestSearchDoc.copy(
        searchRequests = updatedSearchRequests))))

      when(mockHistDocReqSearchCacheService.updateResultsFoundStatusToNoIfEligible(
        historicDocumentRequestSearchDoc.copy(searchRequests = updatedSearchRequests))).thenReturn(
        Future.successful(Some(historicDocumentRequestSearchDoc.copy(searchRequests = updatedSearchRequests))))

      running(app) {
        val response = route(app, validRequest).value
        status(response) mustBe NO_CONTENT

        verify(mockHistDocReqSearchCacheService, Mockito.times(1))
          .retrieveHistDocRequestSearchDocForStatementReqId(any)

        verify(mockHistDocReqSearchCacheService, Mockito.times(1))
          .updateSearchRequestForStatementRequestId(any, any, any)

        verify(mockHistDocReqSearchCacheService, Mockito.times(1))
          .updateResultsFoundStatusToNoIfEligible(any)
      }
    }

    "return 204 when request is valid and all searchRequests have no for searchSuccessful" in new Setup {

      val searchDateTime: String = Utils.dateTimeAsIso8601(LocalDateTime.now)

      val searchRequestsWithNoStatus: Set[SearchRequest] = Set(
        SearchRequest(
          "GB123456789012", incomingStatementReqId, SearchResultStatus.no, searchDateTime, "NoDocumentsFound", 0),
        SearchRequest(
          "GB234567890121", "5c79895-f0da-4472-af5a-d84d340e7mn6", SearchResultStatus.no,
          searchDateTime, "NoDocumentsFound", 0)
      )

      when(mockHistDocReqSearchCacheService.retrieveHistDocRequestSearchDocForStatementReqId(
        incomingStatementReqId)).thenReturn(
        Future.successful(Option(historicDocumentRequestSearchDoc.copy(searchRequests = searchRequestsWithNoStatus)))
      )

      running(app) {
        val response = route(app, validRequest).value
        status(response) mustBe NO_CONTENT
      }
    }

    "return 204 when request is valid and reason is not NoDocumentsFound" in new Setup {
      val searchRequestsInProcess: Set[SearchRequest] = Set(
        SearchRequest(
          "GB123456789012", incomingStatementReqId, SearchResultStatus.inProcess, emptyString, emptyString, 0),
        SearchRequest(
          "GB234567890121", "5c79895-f0da-4472-af5a-d84d340e7mn6", SearchResultStatus.inProcess,
          emptyString, emptyString, 0)
      )

      val updatedSearchRequests: Set[SearchRequest] = Set(
        SearchRequest(
          "GB123456789012", incomingStatementReqId, SearchResultStatus.inProcess, emptyString, docUnreachable, 1),
        SearchRequest(
          "GB234567890121", "5c79895-f0da-4472-af5a-d84d340e7mn6", SearchResultStatus.inProcess,
          emptyString, emptyString, 0)
      )

      when(mockHistDocReqSearchCacheService.retrieveHistDocRequestSearchDocForStatementReqId(
        incomingStatementReqId)).thenReturn(
        Future.successful(Option(historicDocumentRequestSearchDoc.copy(searchRequests = searchRequestsInProcess)))
      )

      when(mockHistDocReqSearchCacheService.updateSearchRequestRetryCount(any, any, any, any)).thenReturn(
        Future.successful(Option(historicDocumentRequestSearchDoc.copy(searchRequests = updatedSearchRequests)))
      )

      when(mockHistDocService.sendHistoricDocumentRequest(any)(any)).thenReturn(Future.successful(true))

      running(app) {
        val response = route(app, validRequestWithReasonOtherThanNoDocuments).value
        status(response) mustBe NO_CONTENT
      }

      verify(mockHistDocReqSearchCacheService, Mockito.times(1))
        .retrieveHistDocRequestSearchDocForStatementReqId(any)
      verify(mockHistDocReqSearchCacheService, Mockito.times(1))
        .updateSearchRequestRetryCount(any, any, any, any)
      verify(mockHistDocService, Mockito.times(1))
        .sendHistoricDocumentRequest(any)(any)
    }

    "return 500 and valid error response when request is valid and reason is not NoDocumentsFound and " +
      "failureRetryCount already has 5 as value" in new Setup {

      val searchRequestsWithMaximumRetryCount: Set[SearchRequest] = Set(
        SearchRequest(
          "GB123456789012", incomingStatementReqId, SearchResultStatus.inProcess, emptyString, docUnreachable, 5),
        SearchRequest(
          "GB234567890121", "5c79895-f0da-4472-af5a-d84d340e7mn6", SearchResultStatus.inProcess,
          emptyString, emptyString, 0)
      )

      when(mockHistDocReqSearchCacheService.retrieveHistDocRequestSearchDocForStatementReqId(
        incomingStatementReqId)).thenReturn(
        Future.successful(Option(historicDocumentRequestSearchDoc.copy(
          searchRequests = searchRequestsWithMaximumRetryCount)))
      )

      running(app) {
        val response = route(app, validRequestWithReasonOtherThanNoDocuments).value
        status(response) mustBe INTERNAL_SERVER_ERROR

        contentAsJson(response) mustBe Json.toJson(StatementSearchFailureNotificationErrorResponse(
          None, ErrorCode.code500, correlationId, Option(incomingStatementReqId)))
      }

      verify(mockHistDocReqSearchCacheService, Mockito.times(1))
        .retrieveHistDocRequestSearchDocForStatementReqId(any)
    }

    "send error response when the request is not valid" in new Setup {
      running(app) {
        val response: Future[Result] = route(app, invalidRequest).value
        status(response) mustBe UNAUTHORIZED
      }
    }
  }

  trait Setup {
    val docUnreachable = "DocumentumUnreachable"

    val incomingStatementReqId: String = UUID.randomUUID().toString
    val ssfnMeteData: StatementSearchFailureNotificationMetadata =
      StatementSearchFailureNotificationMetadata(incomingStatementReqId, "NoDocumentsFound")

    val ssfnMetaDataWithReasonOtherThanNoDocuments: StatementSearchFailureNotificationMetadata =
      StatementSearchFailureNotificationMetadata(incomingStatementReqId, docUnreachable)

    val ssfnReq: StatementSearchFailureNotificationRequest = StatementSearchFailureNotificationRequest(ssfnMeteData)

    val validRequestJSON: JsObject = ssfnRequestFormat.writes(ssfnReq)
    val validReqJSONForReasonCodeOtherThanNoDocuments: JsObject =
      ssfnRequestFormat.writes(ssfnReq.copy(
        StatementSearchFailureNotificationMetadata = ssfnMetaDataWithReasonOtherThanNoDocuments))

    val inValidRequestJSON: JsObject = ssfnRequestFormat.writes(ssfnReq.copy(
      StatementSearchFailureNotificationMetadata = ssfnReq.StatementSearchFailureNotificationMetadata.copy(
        reason = "UnKnown")))

    val validRequestWithoutHeaders: FakeRequest[JsObject] = FakeRequest(
      "POST",
      routes.StatementSearchFailureNotificationController.processNotification().url)
      .withCSRFToken.asInstanceOf[FakeRequest[AnyContentAsEmpty.type]].withBody(validRequestJSON)

    val validRequestWithReasonCodeOtherThanNoDocumentsWithoutHeaders: FakeRequest[JsObject] = FakeRequest(
      "POST",
      routes.StatementSearchFailureNotificationController.processNotification().url)
      .withCSRFToken.asInstanceOf[FakeRequest[AnyContentAsEmpty.type]].withBody(
      validReqJSONForReasonCodeOtherThanNoDocuments
    )

    val inValidRequestWithoutHeaders: FakeRequest[JsObject] = FakeRequest(
      "POST",
      routes.StatementSearchFailureNotificationController.processNotification().url)
      .withCSRFToken.asInstanceOf[FakeRequest[AnyContentAsEmpty.type]].withBody(inValidRequestJSON)

    val correlationId = "some-id"

    val validRequest: FakeRequest[JsObject] = validRequestWithoutHeaders
      .withHeaders(
        "Date" -> "Fri, 16 Aug 2019 18:15:41 GMT",
        "X-Correlation-ID" -> correlationId,
        "X-Forwarded-Host" -> "CDDM",
        "Content-Type" -> "application/json",
        "Accept" -> "application/json",
        "Authorization" -> "Bearer test1234567"
      )

    val validRequestWithReasonOtherThanNoDocuments: FakeRequest[JsObject] =
      validRequestWithReasonCodeOtherThanNoDocumentsWithoutHeaders.withHeaders(
        "Date" -> "Fri, 16 Aug 2019 18:15:41 GMT",
        "X-Correlation-ID" -> correlationId,
        "X-Forwarded-Host" -> "CDDM",
        "Content-Type" -> "application/json",
        "Accept" -> "application/json",
        "Authorization" -> "Bearer test1234567"
      )

    val invalidRequest: FakeRequest[JsObject] = inValidRequestWithoutHeaders
      .withHeaders(
        "Date" -> "Fri, 16 Aug 2019 18:15:41 GMT",
        "X-Correlation-ID" -> correlationId,
        "X-Forwarded-Host" -> "MD/TP",
        "Content-Type" -> "application/json",
        "Accept" -> "application/json",
        "Authorization" -> "Bearer test1234567"
      )

    val schemaValidator = new JSONSchemaValidator()
    val mockHistDocReqSearchCacheService: HistoricDocumentRequestSearchCacheService =
      mock[HistoricDocumentRequestSearchCacheService]

    val mockSecureMessageConnector: SecureMessageConnector = mock[SecureMessageConnector]
    val mockHistDocService: HistoricDocumentService = mock[HistoricDocumentService]

    val app: Application = application().overrides(
      inject.bind[JSONSchemaValidator].toInstance(schemaValidator),
      inject.bind[HistoricDocumentRequestSearchCacheService].toInstance(mockHistDocReqSearchCacheService),
      inject.bind[SecureMessageConnector].toInstance(mockSecureMessageConnector),
      inject.bind[HistoricDocumentService].toInstance(mockHistDocService)
    ).build()

    val historicDocumentRequestSearchDoc: HistoricDocumentRequestSearch = {
      val searchID: UUID = UUID.randomUUID()
      val resultsFound: SearchResultStatus.Value = SearchResultStatus.inProcess
      val searchStatusUpdateDate: String = emptyString
      val currentEori: String = "GB123456789012"
      val params: Params = Params("2", "2021", "4", "2021", "DutyDefermentStatement", "1234567")
      val searchRequests: Set[SearchRequest] = Set(
        SearchRequest(
          "GB123456789012", incomingStatementReqId, SearchResultStatus.inProcess, emptyString, emptyString, 0),
        SearchRequest(
          "GB234567890121", "5c79895-f0da-4472-af5a-d84d340e7mn6", SearchResultStatus.inProcess,
          emptyString, emptyString, 0)
      )

      HistoricDocumentRequestSearch(searchID,
        resultsFound,
        searchStatusUpdateDate,
        currentEori,
        params,
        searchRequests)
    }
  }
}
