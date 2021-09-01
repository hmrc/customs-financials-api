/*
 * Copyright 2021 HM Revenue & Customs
 *
 */

package connectors

import domain.acc37.{AmendCorrespondenceAddressResponse, ContactDetails, ResponseCommon}
import models.{AccountNumber, EORI, EmailAddress}
import play.api.Application
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.Helpers._
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient}
import utils.SpecBase

import scala.concurrent.Future


class Acc37ConnectorSpec extends SpecBase {

  "updateAccountContactDetails" should {
    "return an acc37 response on a successful api call" in new Setup {
      when[Future[domain.acc37.Response]](mockHttpClient.POST(any, any, any)(any, any, any, any))
        .thenReturn(Future.successful(response))

      running(app) {
        val result = await(connector.updateAccountContactDetails(AccountNumber("dan"), EORI("someEori"), acc37ContactInfo, hc.requestId))
        result mustBe response
      }
    }
  }

  trait Setup {
    implicit val hc: HeaderCarrier = HeaderCarrier()
    val mockHttpClient: HttpClient = mock[HttpClient]

    val response: domain.acc37.Response = domain.acc37.Response(
      AmendCorrespondenceAddressResponse(
        ResponseCommon(
          "OK",
          None,
          "",
          None
        )
      )
    )

    val acc37ContactInfo: ContactDetails = domain.acc37.ContactDetails(
      Some("John Doe"),
      "Jone Doe Lane",
      Some("Docks"),
      None,
      Some("Docks"),
      Some("DDD 111"),
      "GB",
      Some("011111111111"),
      None,
      Some(EmailAddress("somedata@email.com"))
    )

    val app: Application = GuiceApplicationBuilder().overrides(
      bind[HttpClient].toInstance(mockHttpClient)
    ).configure(
      "microservice.metrics.enabled" -> false,
      "metrics.enabled" -> false,
      "auditing.enabled" -> false
    ).build()

    val connector: Acc37Connector = app.injector.instanceOf[Acc37Connector]
  }
}
