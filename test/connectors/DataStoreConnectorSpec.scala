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

import models.{EORI, EmailAddress, CompanyInformation, AddressInformation}
import play.api.Application
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.Helpers._
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient, NotFoundException}
import utils.SpecBase
import scala.concurrent.ExecutionContext.Implicits.global
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

  "getCompanyName" should {

    "return companyName on a successful response from the data-store" in new Setup {
      when[Future[CompanyInformation]](mockHttpClient.GET(any, any, any)(any, any, any))
        .thenReturn(Future.successful(companyNameResponse))
      running(app) {
        connector.getCompanyName(EORI("someEori")).map {
          cname => cname mustBe Some("test_company")
        }
      }
    }

    "return None when consent returned is other than 1" in new Setup {
      when[Future[CompanyInformation]](mockHttpClient.GET(any, any, any)(any, any, any))
        .thenReturn(Future.successful(companyNameResponse.copy(consent = "2")))
      running(app) {
        connector.getCompanyName(EORI("someEori")).map {
          cname => cname mustBe None
        }
      }
    }

    "return None when an unknown exception happens from the data-store" in new Setup {
      when[Future[CompanyInformation]](mockHttpClient.GET(any, any, any)(any, any, any))
        .thenReturn(Future.failed(new NotFoundException("error")))
      running(app) {
        connector.getCompanyName(EORI("someEori")).map {
          cname => cname mustBe None
        }
      }
    }
  }

  trait Setup {
    implicit val hc: HeaderCarrier = HeaderCarrier()
    val mockHttpClient: HttpClient = mock[HttpClient]

    val emailResponse: EmailResponse = EmailResponse(Some(EmailAddress("some@email.com")), None)
    val eoriHistoryResponse: EoriHistoryResponse = EoriHistoryResponse(Seq(EoriPeriod(EORI("someEori"), None, None)))

    val companyNameResponse: CompanyInformation =
      CompanyInformation("test_company", "1", AddressInformation("1", "Kailash", None, "GB"))

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
