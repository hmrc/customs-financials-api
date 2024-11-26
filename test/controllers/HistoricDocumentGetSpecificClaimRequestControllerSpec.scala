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

import config.MetaConfig.Platform.{ENROLMENT_IDENTIFIER, ENROLMENT_KEY}
import connectors.DataStoreConnector
import models.{EORI, FileRole}
import org.mockito.ArgumentMatchers.{any, eq => meq}
import org.mockito.Mockito.{times, verify, when}
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json
import play.api.mvc.AnyContentAsJson
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import play.api.{Application, inject}
import services.*
import services.cache.HistoricDocumentRequestSearchCacheService
import uk.gov.hmrc.auth.core.{Enrolment, EnrolmentIdentifier, Enrolments}
import utils.SpecBase
import utils.TestData.*

import java.time.LocalDate
import scala.concurrent.Future

class HistoricDocumentGetSpecificClaimRequestControllerSpec extends SpecBase {

  "HistoricDocumentRequestController.makeRequest" should {

    "delegate to the service and return 204 (no content)" when {
      "successfully sent the request for C79Certificate" in new Setup {
        when(mockDataStoreService.getEoriHistory(meq(eori))(any)).thenReturn(Future.successful(Nil))
        when(mockHistoricDocumentService.sendHistoricDocumentRequest(any)(any)).thenReturn(Future.successful(true))
        when(mockHistDocReqSearchCacheService.saveHistoricDocumentRequestSearch(any)).thenReturn(
          Future.successful(true))

        running(app) {
          val result = route(app, request).value

          status(result) mustBe NO_CONTENT

          verify(mockDataStoreService, times(1)).getEoriHistory(any)(any)
          verify(mockHistoricDocumentService, times(1)).sendHistoricDocumentRequest(any)(any)
          verify(mockHistDocReqSearchCacheService,
            times(1)).saveHistoricDocumentRequestSearch(any)
        }
      }

      "successfully sent the multiple requests when historic eoris found" in new Setup {
        when(mockDataStoreService.getEoriHistory(meq(eori))(any)).thenReturn(
          Future.successful(Seq(eori, EORI("Historic-EORI1"), EORI("Historic-EORI2"))))
        when(mockHistoricDocumentService.sendHistoricDocumentRequest(any)(any)).thenReturn(Future.successful(true))
        when(mockHistoricDocumentService.sendHistoricDocumentRequest(any)(any)).thenReturn(Future.successful(true))
        when(mockHistoricDocumentService.sendHistoricDocumentRequest(any)(any)).thenReturn(Future.successful(true))
        when(mockHistDocReqSearchCacheService.saveHistoricDocumentRequestSearch(any)).thenReturn(
          Future.successful(true))

        running(app) {
          val result = route(app, request).value

          status(result) mustBe NO_CONTENT

          verify(mockDataStoreService, times(1)).getEoriHistory(any)(any)
          verify(mockHistoricDocumentService, times(3)).sendHistoricDocumentRequest(any)(any)
          verify(mockHistDocReqSearchCacheService,
            times(1)).saveHistoricDocumentRequestSearch(any)
        }
      }

      "successfully sent the request for DutyDefermentStatement" in new Setup {
        when(mockDataStoreService.getEoriHistory(meq(eori))(any)).thenReturn(Future.successful(Nil))
        when(mockHistoricDocumentService.sendHistoricDocumentRequest(any)(any)).thenReturn(Future.successful(true))
        when(mockHistDocReqSearchCacheService.saveHistoricDocumentRequestSearch(any)).thenReturn(
          Future.successful(true))

        val req: FakeRequest[AnyContentAsJson] = FakeRequest(
          POST,
          controllers.routes.HistoricDocumentRequestController.makeRequest().url).withJsonBody(
          Json.toJson(frontEndRequest.copy(documentType = FileRole("DutyDefermentStatement"), dan = Some("dan"))))

        running(app) {
          val result = route(app, req).value
          status(result) mustBe NO_CONTENT
        }
      }
    }

    "return 503 (service unavailable)" when {
      "historic document request fails" in new Setup {
        when(mockDataStoreService.getEoriHistory(meq(eori))(any)).thenReturn(Future.successful(Nil))
        when(mockHistoricDocumentService.sendHistoricDocumentRequest(any)(any)).thenReturn(Future.successful(false))

        running(app) {
          val result = route(app, request).value
          status(result) mustBe SERVICE_UNAVAILABLE
        }
      }

      "historic document request fails for one of the historic eoris" in new Setup {
        when(mockDataStoreService.getEoriHistory(meq(eori))(any)).thenReturn(
          Future.successful(Seq(EORI("Historic-EORI1"), EORI("Historic-EORI2"))))
        when(mockHistoricDocumentService.sendHistoricDocumentRequest(any)(any)).thenReturn(Future.successful(false))
        when(mockHistoricDocumentService.sendHistoricDocumentRequest(any)(any)).thenReturn(Future.successful(true))
        when(mockHistoricDocumentService.sendHistoricDocumentRequest(any)(any)).thenReturn(Future.successful(false))

        running(app) {
          val result = route(app, request).value

          status(result) mustBe SERVICE_UNAVAILABLE

          verify(mockDataStoreService, times(1)).getEoriHistory(any)(any)
          verify(mockHistoricDocumentService, times(3)).sendHistoricDocumentRequest(any)(any)
        }
      }
    }
  }

  trait Setup {
    val mockAuthConnector: CustomAuthConnector = mock[CustomAuthConnector]
    val mockHistoricDocumentService: HistoricDocumentService = mock[HistoricDocumentService]
    val mockDataStoreService: DataStoreConnector = mock[DataStoreConnector]
    val mockHistDocReqSearchCacheService: HistoricDocumentRequestSearchCacheService =
      mock[HistoricDocumentRequestSearchCacheService]

    val frontEndRequest: RequestForHistoricDocuments = RequestForHistoricDocuments(
      FileRole("C79Certificate"),
      LocalDate.of(YEAR_2019, MONTH_1, DAY_15),
      LocalDate.of(YEAR_2019, MONTH_3, DAY_16),
      None
    )

    val eori: EORI = EORI(EORI_VALUE)
    val enrolments: Enrolments = Enrolments(
      Set(Enrolment(ENROLMENT_KEY, Seq(EnrolmentIdentifier(ENROLMENT_IDENTIFIER, eori.value)), "activated")))

    when(mockAuthConnector.authorise[Enrolments](any, any)(any, any)).thenReturn(Future.successful(enrolments))
    val app: Application = GuiceApplicationBuilder().overrides(
      inject.bind[CustomAuthConnector].toInstance(mockAuthConnector),
      inject.bind[DataStoreConnector].toInstance(mockDataStoreService),
      inject.bind[HistoricDocumentService].toInstance(mockHistoricDocumentService),
      inject.bind[HistoricDocumentRequestSearchCacheService].toInstance(mockHistDocReqSearchCacheService)
    ).configure(
      "microservice.metrics.enabled" -> false,
      "metrics.enabled" -> false,
      "auditing.enabled" -> false
    ).build()

    val request: FakeRequest[AnyContentAsJson] = FakeRequest(
      "POST",
      routes.HistoricDocumentRequestController.makeRequest().url).withJsonBody(Json.toJson(frontEndRequest))
  }
}
