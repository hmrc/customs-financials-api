/*
 * Copyright 2021 HM Revenue & Customs
 *
 */

package connectors

import models.EORI
import models.responses._
import play.api.Application
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.Helpers._
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient}
import utils.SpecBase

import scala.concurrent.Future

class Sub21ConnectorSpec extends SpecBase {

  "getEoriHistory" should {
    "return a json on a successful response" in new Setup {
      when[Future[HistoricEoriResponse]](mockHttpClient.GET(any, any, any)(any, any, any))
        .thenReturn(Future.successful(response))

      running(app) {
        val result = await(connector.getEORIHistory(EORI("someEori")))
        result mustBe response
      }
    }
  }

  trait Setup {
    implicit val hc: HeaderCarrier = HeaderCarrier()
    val mockHttpClient: HttpClient = mock[HttpClient]
    val responseCommon: EORIHistoryResponseCommon = EORIHistoryResponseCommon("OK", "")
    val eoriHistory: EORIHistory = EORIHistory(EORI("1212"), Some("1211"), Some("12121"))
    val eoriHistoryResponseDetail: EORIHistoryResponseDetail = EORIHistoryResponseDetail(Array(eoriHistory))
    val response: HistoricEoriResponse = HistoricEoriResponse(GetEORIHistoryResponse(responseCommon, eoriHistoryResponseDetail))

    val app: Application = GuiceApplicationBuilder().overrides(
      bind[HttpClient].toInstance(mockHttpClient)
    ).configure(
      "microservice.metrics.enabled" -> false,
      "metrics.enabled" -> false,
      "auditing.enabled" -> false
    ).build()

    val connector: Sub21Connector = app.injector.instanceOf[Sub21Connector]
  }

}
