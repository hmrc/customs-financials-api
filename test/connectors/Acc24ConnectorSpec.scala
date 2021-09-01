/*
 * Copyright 2021 HM Revenue & Customs
 *
 */

package connectors

import models.requests.HistoricDocumentRequest
import models.{EORI, FileRole}
import play.api.Application
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.Helpers._
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient, HttpResponse, NotFoundException}
import utils.SpecBase

import scala.concurrent.Future

class Acc24ConnectorSpec extends SpecBase {

  "sendHistoricDocumentRequest" should {
    "return true when a successful request has been made" in new Setup {
      when[Future[HttpResponse]](mockHttpClient.POST(any, any, any)(any, any, any, any))
        .thenReturn(Future.successful(HttpResponse(NO_CONTENT, "")))

      running(app) {
        val result = await(connector.sendHistoricDocumentRequest(historicDocumentRequest, hc.requestId))
        result mustBe true
      }
    }

    "return false if any other 2xx status code is returned" in new Setup {
      when[Future[HttpResponse]](mockHttpClient.POST(any, any, any)(any, any, any, any))
        .thenReturn(Future.successful(HttpResponse(OK, "")))

      running(app) {
        val result = await(connector.sendHistoricDocumentRequest(historicDocumentRequest, hc.requestId))
        result mustBe false
      }
    }

    "return false if an exception from Acc24 is returned" in new Setup {
      when[Future[HttpResponse]](mockHttpClient.POST(any, any, any)(any, any, any, any))
        .thenReturn(Future.failed(new NotFoundException("error")))

      running(app) {
        val result = await(connector.sendHistoricDocumentRequest(historicDocumentRequest, hc.requestId))
        result mustBe false
      }
    }
  }

  trait Setup {
    implicit val hc: HeaderCarrier = HeaderCarrier()
    val mockHttpClient: HttpClient = mock[HttpClient]
    val historicDocumentRequest: HistoricDocumentRequest =
      HistoricDocumentRequest(EORI("someEori"), FileRole("C79Certificate"), 10, 10, 10, 10, Some("dan"))

    val app: Application = GuiceApplicationBuilder().overrides(
      bind[HttpClient].toInstance(mockHttpClient)
    ).configure(
      "microservice.metrics.enabled" -> false,
      "metrics.enabled" -> false,
      "auditing.enabled" -> false
    ).build()

    val connector: Acc24Connector = app.injector.instanceOf[Acc24Connector]
  }

}
