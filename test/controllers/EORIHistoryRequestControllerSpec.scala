/*
 * Copyright 2021 HM Revenue & Customs
 *
 */

package controllers

import connectors.Sub21Connector
import models.EORI
import models.responses._
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.api.{Application, inject}
import uk.gov.hmrc.http.UpstreamErrorResponse
import utils.SpecBase

import scala.concurrent.Future

class EORIHistoryRequestControllerSpec extends SpecBase {

  "EORIHistoryRequestController.get" should {
    "validate the EORI and return 200 status code" in new Setup {
      val responseCommon: EORIHistoryResponseCommon = EORIHistoryResponseCommon("OK", "")
      val eoriHistory: EORIHistory = EORIHistory(EORI("1212"), Some("1211"), Some("12121"))
      val eoriHistoryResponseDetail: EORIHistoryResponseDetail = EORIHistoryResponseDetail(Array(eoriHistory))
      val response: HistoricEoriResponse = HistoricEoriResponse(GetEORIHistoryResponse(responseCommon, eoriHistoryResponseDetail))

      when(mockSub21Connector.getEORIHistory(any))
        .thenReturn(Future.successful(response))

      running(app) {
        val result = route(app, request).value
        status(result) mustBe OK
      }
    }

    "return 404 for invalid EORI" in new Setup {
      when(mockSub21Connector.getEORIHistory(any))
        .thenReturn(Future.failed(UpstreamErrorResponse("failed", 404, 404, Map.empty)))

      running(app) {
        val result = route(app, request).value
        status(result) mustBe NOT_FOUND
      }
    }
  }

  trait Setup {
    val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest(GET, "/customs-financials-api/eori/testEORI/validate")
    val mockAuthConnector: CustomAuthConnector = mock[CustomAuthConnector]
    val mockSub21Connector: Sub21Connector = mock[Sub21Connector]

    val app: Application = GuiceApplicationBuilder().overrides(
      inject.bind[Sub21Connector].toInstance(mockSub21Connector)
    ).configure(
      "microservice.metrics.enabled" -> false,
      "metrics.enabled" -> false,
      "auditing.enabled" -> false
    ).build()
  }
}
