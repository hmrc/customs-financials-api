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

import models.{AddressInformation, CompanyInformation, EORI, EmailAddress}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import play.api.{Application, Configuration}
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.Helpers.*
import uk.gov.hmrc.http.client.{HttpClientV2, RequestBuilder}
import uk.gov.hmrc.http.{HeaderCarrier, NotFoundException}
import utils.{SpecBase, WireMockSupportProvider}
import utils.TestData.COUNTRY_CODE_GB
import com.typesafe.config.ConfigFactory
import play.api.libs.json.Json
import com.github.tomakehurst.wiremock.client.WireMock.{get, ok, urlPathMatching}
import com.github.tomakehurst.wiremock.http.RequestMethod.GET

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import utils.TestData.EORI_VALUE_1

class DataStoreConnectorSpec extends SpecBase with WireMockSupportProvider {

  "getVerifiedEmail" should {

    "return the email from the data-store response" in new Setup {

      wireMockServer.stubFor(
        get(urlPathMatching(customDataStoreVerifiedEmailUrl))
          .willReturn(ok(Json.toJson(emailResponse).toString))
      )

      val result: Option[EmailAddress] = await(connector.getVerifiedEmail(EORI(EORI_VALUE_1)))
      result mustBe emailResponse.address

      verifyEndPointUrlHit(customDataStoreVerifiedEmailUrl, GET)
    }

    "return None when an unknown exception happens from the data-store" in new Setup {
      val mockHttpClient: HttpClientV2          = mock[HttpClientV2]
      val requestBuilder: RequestBuilder        = mock[RequestBuilder]
      implicit val headerCarrier: HeaderCarrier = HeaderCarrier()

      when(requestBuilder.withBody(any())(any(), any(), any())).thenReturn(requestBuilder)
      when(requestBuilder.setHeader(any[(String, String)]())).thenReturn(requestBuilder)
      when(mockHttpClient.get(any)(any)).thenReturn(requestBuilder)
      when(requestBuilder.execute(any, any)).thenReturn(Future.failed(new NotFoundException("error")))

      val application: Application = GuiceApplicationBuilder()
        .overrides(
          bind[HttpClientV2].toInstance(mockHttpClient),
          bind[RequestBuilder].toInstance(requestBuilder)
        )
        .configure(
          "microservice.metrics.enabled" -> false,
          "metrics.enabled"              -> false,
          "auditing.enabled"             -> false
        )
        .build()

      val dataStoreConnector: DataStoreConnector = application.injector.instanceOf[DataStoreConnector]

      running(application) {
        val result = await(dataStoreConnector.getVerifiedEmail(EORI(EORI_VALUE_1)))
        result mustBe None
      }
    }
  }

  "getEoriHistory" should {

    "return EORIHistory on a successful response from the data-store" in new Setup {
      wireMockServer.stubFor(
        get(urlPathMatching(customDataStoreEoriHistoryUrl))
          .willReturn(ok(Json.toJson(eoriHistoryResponse).toString))
      )

      val result: Seq[EORI] = await(connector.getEoriHistory(EORI(EORI_VALUE_1)))
      result mustBe Seq(EORI(EORI_VALUE_1))

      verifyEndPointUrlHit(customDataStoreEoriHistoryUrl, GET)
    }

    "return an empty sequence if an exception was thrown from the data-store" in new Setup {
      val mockHttpClient: HttpClientV2          = mock[HttpClientV2]
      val requestBuilder: RequestBuilder        = mock[RequestBuilder]
      implicit val headerCarrier: HeaderCarrier = HeaderCarrier()

      when(requestBuilder.withBody(any())(any(), any(), any())).thenReturn(requestBuilder)
      when(requestBuilder.setHeader(any[(String, String)]())).thenReturn(requestBuilder)
      when(mockHttpClient.get(any)(any)).thenReturn(requestBuilder)
      when(requestBuilder.execute(any, any)).thenReturn(Future.failed(new NotFoundException("error")))

      val application: Application = GuiceApplicationBuilder()
        .overrides(
          bind[HttpClientV2].toInstance(mockHttpClient),
          bind[RequestBuilder].toInstance(requestBuilder)
        )
        .configure(
          "microservice.metrics.enabled" -> false,
          "metrics.enabled"              -> false,
          "auditing.enabled"             -> false
        )
        .build()

      val dataStoreConnector: DataStoreConnector = application.injector.instanceOf[DataStoreConnector]

      running(application) {
        val result = await(dataStoreConnector.getEoriHistory(EORI(EORI_VALUE_1)))
        result mustBe Seq.empty
      }
    }
  }

  "getCompanyName" should {

    "return companyName on a successful response from the data-store" in new Setup {

      wireMockServer.stubFor(
        get(urlPathMatching(customDataStoreCompanyInfoUrl))
          .willReturn(ok(Json.toJson(companyNameResponse).toString))
      )

      val result: Option[String] = await(connector.getCompanyName(EORI(EORI_VALUE_1)))
      result mustBe Some("test_company")

      verifyEndPointUrlHit(customDataStoreCompanyInfoUrl, GET)
    }

    "return companyName when consent returned is other than 1" in new Setup {

      wireMockServer.stubFor(
        get(urlPathMatching(customDataStoreCompanyInfoUrl))
          .willReturn(ok(Json.toJson(companyNameResponse.copy(consent = "2")).toString))
      )

      val result: Option[String] = await(connector.getCompanyName(EORI(EORI_VALUE_1)))
      result mustBe Some("test_company")

      verifyEndPointUrlHit(customDataStoreCompanyInfoUrl, GET)
    }

    "return None when an unknown exception happens from the data-store" in {
      val mockHttpClient: HttpClientV2          = mock[HttpClientV2]
      val requestBuilder: RequestBuilder        = mock[RequestBuilder]
      implicit val headerCarrier: HeaderCarrier = HeaderCarrier()

      when(requestBuilder.withBody(any())(any(), any(), any())).thenReturn(requestBuilder)
      when(requestBuilder.setHeader(any[(String, String)]())).thenReturn(requestBuilder)
      when(mockHttpClient.get(any)(any)).thenReturn(requestBuilder)
      when(requestBuilder.execute(any, any)).thenReturn(Future.failed(new NotFoundException("error")))

      val application: Application = GuiceApplicationBuilder()
        .overrides(
          bind[HttpClientV2].toInstance(mockHttpClient),
          bind[RequestBuilder].toInstance(requestBuilder)
        )
        .configure(
          "microservice.metrics.enabled" -> false,
          "metrics.enabled"              -> false,
          "auditing.enabled"             -> false
        )
        .build()

      val dataStoreConnector: DataStoreConnector = application.injector.instanceOf[DataStoreConnector]

      running(application) {
        dataStoreConnector.getCompanyName(EORI(EORI_VALUE_1)).map { cname =>
          cname mustBe None
        }
      }
    }
  }

  override def config: Configuration = Configuration(
    ConfigFactory.parseString(
      s"""
         |microservice {
         |  services {
         |  customs-data-store {
         |            host = $wireMockHost
         |            port = $wireMockPort
         |        }
         |  }
         |}
         |""".stripMargin
    )
  )

  trait Setup {
    implicit val hc: HeaderCarrier      = HeaderCarrier()
    val customDataStoreVerifiedEmailUrl = "/customs-data-store/eori/someEORI/verified-email"
    val customDataStoreEoriHistoryUrl   = "/customs-data-store/eori/someEORI/eori-history"
    val customDataStoreCompanyInfoUrl   = "/customs-data-store/eori/someEORI/company-information"

    val emailResponse: EmailResponse             = EmailResponse(Some(EmailAddress("some@email.com")), None)
    val eoriHistoryResponse: EoriHistoryResponse = EoriHistoryResponse(Seq(EoriPeriod(EORI(EORI_VALUE_1), None, None)))

    val companyNameResponse: CompanyInformation =
      CompanyInformation("test_company", "1", AddressInformation("1", "Kailash", None, COUNTRY_CODE_GB))

    val app: Application              = application().configure(config).build()
    val connector: DataStoreConnector = app.injector.instanceOf[DataStoreConnector]
  }
}
