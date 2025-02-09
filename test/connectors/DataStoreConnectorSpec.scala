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
import play.api.{Application, Configuration}
import play.api.test.Helpers.*
import uk.gov.hmrc.http.HeaderCarrier
import utils.{SpecBase, WireMockSupportProvider}
import utils.TestData.COUNTRY_CODE_GB
import com.typesafe.config.ConfigFactory
import play.api.libs.json.Json
import com.github.tomakehurst.wiremock.client.WireMock.{aResponse, get, ok, urlPathMatching}
import com.github.tomakehurst.wiremock.http.Fault
import com.github.tomakehurst.wiremock.http.RequestMethod.GET

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

      verifyExactlyOneEndPointUrlHit(customDataStoreVerifiedEmailUrl, GET)
    }

    "return None when an unknown exception happens from the data-store" in new Setup {
      wireMockServer.stubFor(
        get(urlPathMatching(customDataStoreVerifiedEmailUrl))
          .willReturn(aResponse().withFault(Fault.CONNECTION_RESET_BY_PEER))
      )

      val result: Option[EmailAddress] = await(connector.getVerifiedEmail(EORI(EORI_VALUE_1)))
      result mustBe empty

      verifyEndPointUrlHit(customDataStoreVerifiedEmailUrl, GET)
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

      verifyExactlyOneEndPointUrlHit(customDataStoreEoriHistoryUrl, GET)
    }

    "return an empty sequence if connection is rest while calling api" in new Setup {
      wireMockServer.stubFor(
        get(urlPathMatching(customDataStoreEoriHistoryUrl))
          .willReturn(aResponse().withFault(Fault.CONNECTION_RESET_BY_PEER))
      )

      val result: Seq[EORI] = await(connector.getEoriHistory(EORI(EORI_VALUE_1)))
      result mustBe empty

      verifyEndPointUrlHit(customDataStoreEoriHistoryUrl, GET)
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

      verifyExactlyOneEndPointUrlHit(customDataStoreCompanyInfoUrl, GET)
    }

    "return companyName when consent returned is other than 1" in new Setup {

      wireMockServer.stubFor(
        get(urlPathMatching(customDataStoreCompanyInfoUrl))
          .willReturn(ok(Json.toJson(companyNameResponse.copy(consent = "2")).toString))
      )

      val result: Option[String] = await(connector.getCompanyName(EORI(EORI_VALUE_1)))
      result mustBe Some("test_company")

      verifyExactlyOneEndPointUrlHit(customDataStoreCompanyInfoUrl, GET)
    }

    "return None when an unknown exception happens from the data-store" in new Setup {

      wireMockServer.stubFor(
        get(urlPathMatching(customDataStoreCompanyInfoUrl))
          .willReturn(aResponse().withFault(Fault.CONNECTION_RESET_BY_PEER))
      )

      val result: Option[String] = await(connector.getCompanyName(EORI(EORI_VALUE_1)))
      result mustBe empty
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
