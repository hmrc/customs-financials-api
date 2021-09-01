/*
 * Copyright 2021 HM Revenue & Customs
 *
 */

package connectors

import models.{EORI, EmailAddress}
import play.api.Application
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.Helpers._
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient, NotFoundException}
import utils.SpecBase

import scala.concurrent.Future

class DataStoreConnectorSpec extends SpecBase {
  "getVerifiedEmail" should {
    "return the email from the data-store response" in new Setup {
      when[Future[EmailResponse]](mockHttpClient.GET(any, any, any)(any, any, any))
        .thenReturn(Future.successful(emailResponse))

      running(app) {
        val result = await(connector.getVerifiedEmail(EORI("someEori")))
        result mustBe emailResponse.address
      }
    }

    "return None when an unknown exception happens from the data-store" in new Setup {
      when[Future[EmailResponse]](mockHttpClient.GET(any, any, any)(any, any, any))
        .thenReturn(Future.failed(new NotFoundException("error")))

      running(app) {
        val result = await(connector.getVerifiedEmail(EORI("someEori")))
        result mustBe None
      }
    }
  }

  "getEoriHistory" should {
    "return EORIHistory on a successful response from the data-store" in new Setup {
      when[Future[EoriHistoryResponse]](mockHttpClient.GET(any, any, any)(any, any, any))
        .thenReturn(Future.successful(eoriHistoryResponse))

      running(app) {
        val result = await(connector.getEoriHistory(EORI("someEori")))
        result mustBe Seq(EORI("someEori"))
      }
    }

    "return an empty sequence if an exception was thrown from the data-store" in new Setup {
      when[Future[EoriHistoryResponse]](mockHttpClient.GET(any, any, any)(any, any, any))
        .thenReturn(Future.failed(new NotFoundException("error")))

      running(app) {
        val result = await(connector.getEoriHistory(EORI("someEori")))
        result mustBe Seq.empty
      }
    }
  }

  trait Setup {
    implicit val hc: HeaderCarrier = HeaderCarrier()
    val mockHttpClient: HttpClient = mock[HttpClient]

    val emailResponse: EmailResponse = EmailResponse(Some(EmailAddress("some@email.com")), None)
    val eoriHistoryResponse: EoriHistoryResponse = EoriHistoryResponse(Seq(EoriPeriod(EORI("someEori"), None, None)))

    val app: Application = GuiceApplicationBuilder().overrides(
      bind[HttpClient].toInstance(mockHttpClient)
    ).configure(
      "microservice.metrics.enabled" -> false,
      "metrics.enabled" -> false,
      "auditing.enabled" -> false
    ).build()

    val connector: DataStoreConnector = app.injector.instanceOf[DataStoreConnector]
  }
}
