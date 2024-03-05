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

package connectors

import models.EORI
import models.requests.HistoricDocumentRequest
import play.api.Application
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.Helpers._
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient, HttpResponse, NotFoundException}
import utils.SpecBase
import utils.TestData.{FILE_ROLE_C79_CERTIFICATE, MONTH_10, YEAR_2019}
import utils.Utils.emptyString

import scala.concurrent.Future

class Acc24ConnectorSpec extends SpecBase {

  "sendHistoricDocumentRequest" should {
    "return true when a successful request has been made" in new Setup {
      when[Future[HttpResponse]](mockHttpClient.POST(any, any, any)(any, any, any, any))
        .thenReturn(Future.successful(HttpResponse(NO_CONTENT, emptyString)))

      running(app) {
        val result = await(connector.sendHistoricDocumentRequest(historicDocumentRequest))
        result mustBe true
      }
    }

    "return false if any other 2xx status code is returned" in new Setup {
      when[Future[HttpResponse]](mockHttpClient.POST(any, any, any)(any, any, any, any))
        .thenReturn(Future.successful(HttpResponse(OK, emptyString)))

      running(app) {
        val result = await(connector.sendHistoricDocumentRequest(historicDocumentRequest))
        result mustBe false
      }
    }

    "return false if an exception from Acc24 is returned" in new Setup {
      when[Future[HttpResponse]](mockHttpClient.POST(any, any, any)(any, any, any, any))
        .thenReturn(Future.failed(new NotFoundException("error")))

      running(app) {
        val result = await(connector.sendHistoricDocumentRequest(historicDocumentRequest))
        result mustBe false
      }
    }
  }

  trait Setup {
    implicit val hc: HeaderCarrier = HeaderCarrier()
    val mockHttpClient: HttpClient = mock[HttpClient]
    val historicDocumentRequest: HistoricDocumentRequest =
      HistoricDocumentRequest(EORI("someEori"),
        FILE_ROLE_C79_CERTIFICATE,
        YEAR_2019,
        MONTH_10,
        YEAR_2019,
        MONTH_10, Some("dan"))

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
