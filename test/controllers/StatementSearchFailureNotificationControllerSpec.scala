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

import models.{HistoricDocumentRequestSearch, Params, SearchRequest, StatementSearchFailureNotificationMetadata}
import models.requests.StatementSearchFailureNotificationRequest
import models.requests.StatementSearchFailureNotificationRequest.ssfnRequestFormat
import play.api.http.Status.{BAD_REQUEST, NO_CONTENT}
import play.api.{Application, inject}
import play.api.libs.json.JsObject
import play.api.mvc._
import play.api.test.CSRFTokenHelper.CSRFFRequestHeader
import play.api.test.FakeRequest
import play.api.test.Helpers.{route, running, status}
import services.cache.HistoricDocumentRequestSearchCacheService
import utils.Utils.emptyString
import utils.{JSONSchemaValidator, SpecBase}

import java.util.UUID
import scala.concurrent.Future

class StatementSearchFailureNotificationControllerSpec extends SpecBase {
  "processNotification" should {
    "return 204 when the request is valid" in new Setup {
      running(app) {
        when(mockHistDocReqSearchCacheService.retrieveHistDocRequestSearchDocForStatementReqId(any)).thenReturn(
          Future.successful(Option(historicDocumentRequestSearchDoc))
        )
        val response = route(app, validRequest).value
        status(response) mustBe NO_CONTENT
      }
    }

    "send error response when the request is not valid" in new Setup {
      running(app) {
        val response: Future[Result] = route(app, invalidRequest).value
        status(response) mustBe BAD_REQUEST
      }
    }
  }


  trait Setup {
    val ssfnMeteData: StatementSearchFailureNotificationMetadata =
      StatementSearchFailureNotificationMetadata(UUID.randomUUID().toString, "NoDocumentsFound")

    val ssfnReq: StatementSearchFailureNotificationRequest = StatementSearchFailureNotificationRequest(ssfnMeteData)

    val validRequestJSON: JsObject = ssfnRequestFormat.writes(ssfnReq)
    val inValidRequestJSON: JsObject = ssfnRequestFormat.writes(ssfnReq.copy(
      StatementSearchFailureNotificationMetadata = ssfnReq.StatementSearchFailureNotificationMetadata.copy(
        reason = "UnKnown")))

    val validRequestWithoutHeaders: FakeRequest[JsObject] = FakeRequest(
      "POST",
      routes.StatementSearchFailureNotificationController.processNotification().url)
      .withCSRFToken.asInstanceOf[FakeRequest[AnyContentAsEmpty.type]].withBody(validRequestJSON)

    val inValidRequestWithoutHeaders: FakeRequest[JsObject] = FakeRequest(
      "POST",
      routes.StatementSearchFailureNotificationController.processNotification().url)
      .withCSRFToken.asInstanceOf[FakeRequest[AnyContentAsEmpty.type]].withBody(inValidRequestJSON)

    val validRequest: FakeRequest[JsObject] = validRequestWithoutHeaders
      .withHeaders(
        "Date" -> "Fri, 16 Aug 2019 18:15:41 GMT",
        "X-Correlation-ID" -> "some-id",
        "X-Forwarded-Host" -> "MD/TP",
        "Content-Type" -> "application/json",
        "Accept" -> "application/json",
        "Authorization" -> "Bearer test1234567"
      )

    val invalidRequest: FakeRequest[JsObject] = inValidRequestWithoutHeaders
      .withHeaders(
        "Date" -> "Fri, 16 Aug 2019 18:15:41 GMT",
        "X-Correlation-ID" -> "some-id",
        "X-Forwarded-Host" -> "MD/TP",
        "Content-Type" -> "application/json",
        "Accept" -> "application/json",
        "Authorization" -> "Bearer test1234567"
      )

    val schemaValidator = new JSONSchemaValidator()
    val mockHistDocReqSearchCacheService: HistoricDocumentRequestSearchCacheService =
      mock[HistoricDocumentRequestSearchCacheService]

    val app: Application = application().overrides(
      inject.bind[JSONSchemaValidator].toInstance(schemaValidator),
      inject.bind[HistoricDocumentRequestSearchCacheService].toInstance(mockHistDocReqSearchCacheService)
    ).build()

    val historicDocumentRequestSearchDoc: HistoricDocumentRequestSearch = {
      val searchID: UUID = UUID.randomUUID()
      val resultsFound: String = "inProcess"
      val searchStatusUpdateDate: String = emptyString
      val currentEori: String = "GB123456789012"
      val params: Params = Params("2", "2021", "4", "2021", "DutyDefermentStatement", "1234567")
      val searchRequests: Set[SearchRequest] = Set(
        SearchRequest(
          "GB123456789012", "5b89895-f0da-4472-af5a-d84d340e7mn5", "inProcess", emptyString, emptyString, 0),
        SearchRequest(
          "GB234567890121", "5c79895-f0da-4472-af5a-d84d340e7mn6", "inProcess", emptyString, emptyString, 0)
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
