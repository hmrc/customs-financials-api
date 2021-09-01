/*
 * Copyright 2021 HM Revenue & Customs
 *
 */

package connectors

import models.EORI
import models.responses.StandingAuthoritiesResponse
import play.api.Application
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.Helpers._
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient}
import utils.SpecBase

import scala.concurrent.Future

class Acc29ConnectorSpec extends SpecBase {

  "getStandingAuthorities" should {
    "return a list of authorities on a successful response" in new Setup {
      when[Future[StandingAuthoritiesResponse]](mockHttpClient.POST(any, any, any)(any, any, any, any))
        .thenReturn(Future.successful(response))

      running(app) {
        val result = await(connector.getStandingAuthorities(EORI("someEori"), hc.requestId))
        result mustBe Seq.empty
      }
    }
  }

  trait Setup {
    implicit val hc: HeaderCarrier = HeaderCarrier()
    val mockHttpClient: HttpClient = mock[HttpClient]

    val response: StandingAuthoritiesResponse = StandingAuthoritiesResponse(EORI("someEORI"), List.empty)

    val app: Application = GuiceApplicationBuilder().overrides(
      bind[HttpClient].toInstance(mockHttpClient)
    ).configure(
      "microservice.metrics.enabled" -> false,
      "metrics.enabled" -> false,
      "auditing.enabled" -> false
    ).build()

    val connector: Acc29Connector = app.injector.instanceOf[Acc29Connector]
  }
}
