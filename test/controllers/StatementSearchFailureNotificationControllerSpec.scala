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

import models.StatementSearchFailureNotificationMetadata
import models.requests.StatementSearchFailureNotificationRequest
import models.requests.StatementSearchFailureNotificationRequest.ssfnRequestFormat
import play.api.http.Status.NO_CONTENT
import play.api.inject
import play.api.libs.json.JsObject
import play.api.mvc._
import play.api.test.CSRFTokenHelper.CSRFFRequestHeader
import play.api.test.FakeRequest
import play.api.test.Helpers.{route, running, status}
import services.cache.HistoricDocumentRequestSearchCacheService
import utils.{JSONSchemaValidator, SpecBase}

import java.util.UUID

class StatementSearchFailureNotificationControllerSpec extends SpecBase {
  "processNotification" should {
    "return 204 when the request is valid" in new Setup {
      running(app) {
        val response = route(app, validRequest).value
        status(response) mustBe NO_CONTENT
      }
    }

    "send error response when the request is not valid" in new Setup {

    }
  }

  trait Setup {
    val ssfnMeteData = StatementSearchFailureNotificationMetadata(UUID.randomUUID().toString, "NoDocumentsFound")

    val ssfnReq = StatementSearchFailureNotificationRequest(ssfnMeteData)

    val validRequestJSON: JsObject = ssfnRequestFormat.writes(ssfnReq)
    val validRequestWithoutHeaders = FakeRequest(
      "POST",
      routes.StatementSearchFailureNotificationController.processNotification().url)
      .withCSRFToken.asInstanceOf[FakeRequest[AnyContentAsEmpty.type]].withBody(validRequestJSON)

    val validRequest: FakeRequest[JsObject] = validRequestWithoutHeaders
      .withHeaders(
        "Date" -> "Fri, 16 Aug 2019 18:15:41 GMT",
        "X-Correlation-ID" -> "some-id",
        "X-Forwarded-Host" -> "MD/TP",
        "Content-Type" -> "application/json",
        "Accept" -> "application/json",
        "Authorization" -> "Bearer test1234567"
      )

    val schemaValidator = new JSONSchemaValidator()
    val mockHistDocReqSearchCacheService = mock[HistoricDocumentRequestSearchCacheService]

    val app = application().overrides(
      inject.bind[JSONSchemaValidator].toInstance(schemaValidator),
      inject.bind[HistoricDocumentRequestSearchCacheService].toInstance(mockHistDocReqSearchCacheService)
    ).build()
  }

}
