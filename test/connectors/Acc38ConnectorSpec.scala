/*
 * Copyright 2021 HM Revenue & Customs
 *
 */

package connectors

import domain.acc38.GetCorrespondenceAddressResponse
import models.{AccountNumber, EORI}
import play.api.Application
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.Helpers._
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient}
import utils.SpecBase

import scala.concurrent.Future


class Acc38ConnectorSpec extends SpecBase {

  "getAccountContactDetails" should {
    "return an acc37 response on a successful api call" in new Setup {
      when[Future[domain.acc38.Response]](mockHttpClient.POST(any, any, any)(any, any, any, any))
        .thenReturn(Future.successful(response))

      running(app) {
        val result = await(connector.getAccountContactDetails(AccountNumber("dan"), EORI("someEori"), hc.requestId))
        result mustBe response
      }
    }
  }

  trait Setup {
    implicit val hc: HeaderCarrier = HeaderCarrier()
    val mockHttpClient: HttpClient = mock[HttpClient]

    val response: domain.acc38.Response = domain.acc38.Response(
      GetCorrespondenceAddressResponse(
        domain.acc38.ResponseCommon(
          "OK",
          None,
          "",
          None),
        None
      )
    )


    val app: Application = GuiceApplicationBuilder().overrides(
      bind[HttpClient].toInstance(mockHttpClient)
    ).configure(
      "microservice.metrics.enabled" -> false,
      "metrics.enabled" -> false,
      "auditing.enabled" -> false
    ).build()

    val connector: Acc38Connector = app.injector.instanceOf[Acc38Connector]
  }
}
