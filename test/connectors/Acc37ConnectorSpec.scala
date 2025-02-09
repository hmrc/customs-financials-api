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

import domain.acc37.{AmendCorrespondenceAddressResponse, ContactDetails, Response, ResponseCommon}
import models.{AccountNumber, EORI, EmailAddress}
import play.api.{Application, Configuration}
import play.api.libs.json.Json
import play.api.test.Helpers.*
import uk.gov.hmrc.http.HeaderCarrier
import utils.{SpecBase, WireMockSupportProvider}
import com.typesafe.config.ConfigFactory
import utils.TestData.COUNTRY_CODE_GB
import utils.Utils.emptyString
import com.github.tomakehurst.wiremock.client.WireMock.{equalTo, matchingJsonPath, ok, post, urlPathMatching}
import com.github.tomakehurst.wiremock.http.RequestMethod.POST
import config.MetaConfig.Platform.MDTP
import utils.TestData.EORI_VALUE

class Acc37ConnectorSpec extends SpecBase with WireMockSupportProvider {

  "updateAccountContactDetails" should {
    "return an acc37 response on a successful api call" in new Setup {

      wireMockServer.stubFor(
        post(urlPathMatching(acc37UpdateAccountContactDetailsEndpointUrl))
          .withHeader(X_FORWARDED_HOST, equalTo(MDTP))
          .withHeader(CONTENT_TYPE, equalTo("application/json"))
          .withHeader(ACCEPT, equalTo("application/json"))
          .withHeader(AUTHORIZATION, equalTo("Bearer test1234567"))
          .withRequestBody(
            matchingJsonPath("$.amendCorrespondenceAddressRequest[?(@.requestCommon.originatingSystem == 'Digital')]")
          )
          .withRequestBody(
            matchingJsonPath("$.amendCorrespondenceAddressRequest[?(@.requestDetail.eori == 'testEORI')]")
          )
          .willReturn(ok(Json.toJson(response).toString))
      )

      val result: Response =
        await(connector.updateAccountContactDetails(AccountNumber("dan"), EORI(EORI_VALUE), acc37ContactInfo))

      result mustBe response

      verifyExactlyOneEndPointUrlHit(acc37UpdateAccountContactDetailsEndpointUrl, POST)
    }
  }

  override def config: Configuration = Configuration(
    ConfigFactory.parseString(
      s"""
         |microservice {
         |  services {
         |  acc37 {
         |            host = $wireMockHost
         |            port = $wireMockPort
         |        }
         |  }
         |}
         |""".stripMargin
    )
  )

  trait Setup {
    implicit val hc: HeaderCarrier = HeaderCarrier()

    val acc37UpdateAccountContactDetailsEndpointUrl =
      "/customs-financials-hods-stub/accounts/amendcorrespondenceaddress/v1"

    val response: domain.acc37.Response = domain.acc37.Response(
      AmendCorrespondenceAddressResponse(
        ResponseCommon(
          "OK",
          None,
          emptyString,
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
      COUNTRY_CODE_GB,
      Some("011111111111"),
      None,
      Some(EmailAddress("somedata@email.com"))
    )

    val app: Application          = application().configure(config).build()
    val connector: Acc37Connector = app.injector.instanceOf[Acc37Connector]
  }
}
