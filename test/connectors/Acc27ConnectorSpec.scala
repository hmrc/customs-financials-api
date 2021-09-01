/*
 * Copyright 2021 HM Revenue & Customs
 *
 */

package connectors

import play.api.Application
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.{JsObject, JsValue, Json}
import play.api.test.Helpers._
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient}
import utils.SpecBase

import scala.concurrent.Future

class Acc27ConnectorSpec extends SpecBase {

  "getAccounts" should {
    "return a json on a successful response" in new Setup {
      when[Future[JsValue]](mockHttpClient.POST(any, any, any)(any, any, any, any))
        .thenReturn(Future.successful(Json.obj("someOther" -> "value")))

      running(app) {
        val result = await(connector.getAccounts(requestBody, hc.requestId))
        result mustBe Json.obj("someOther" -> "value")
      }
    }
  }

  trait Setup {
    implicit val hc: HeaderCarrier = HeaderCarrier()
    val mockHttpClient: HttpClient = mock[HttpClient]
    val requestBody: JsObject = Json.obj("some" -> "value")

    val app: Application = GuiceApplicationBuilder().overrides(
      bind[HttpClient].toInstance(mockHttpClient)
    ).configure(
      "microservice.metrics.enabled" -> false,
      "metrics.enabled" -> false,
      "auditing.enabled" -> false
    ).build()

    val connector: Acc27Connector = app.injector.instanceOf[Acc27Connector]
  }
}
