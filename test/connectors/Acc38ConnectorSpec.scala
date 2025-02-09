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

import domain.acc38.{GetCorrespondenceAddressResponse, Response}
import models.{AccountNumber, EORI}
import play.api.{Application, Configuration}
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.Helpers.*
import play.api.libs.json.Json
import uk.gov.hmrc.http.HeaderCarrier
import com.typesafe.config.ConfigFactory
import utils.{SpecBase, WireMockSupportProvider}
import com.github.tomakehurst.wiremock.client.WireMock.{equalTo, matchingJsonPath, ok, post, urlPathMatching}
import com.github.tomakehurst.wiremock.http.RequestMethod.POST
import config.MetaConfig.Platform.MDTP
import utils.TestData.EORI_VALUE
import utils.Utils.emptyString



class Acc38ConnectorSpec extends SpecBase with WireMockSupportProvider {

  "getAccountContactDetails" should {
    "return an acc37 response on a successful api call" in new Setup {

      wireMockServer.stubFor(
        post(urlPathMatching(acc38DutyDefermentContactDetailsEndpointUrl))
          .withHeader(X_FORWARDED_HOST, equalTo(MDTP))
          .withHeader(CONTENT_TYPE, equalTo("application/json"))
          .withHeader(ACCEPT, equalTo("application/json"))
          .withHeader(AUTHORIZATION, equalTo("Bearer test1234567"))
          .withRequestBody(
            matchingJsonPath("$.getCorrespondenceAddressRequest[?(@.requestCommon.originatingSystem == 'Digital')]")
          )
          .withRequestBody(
            matchingJsonPath("$.getCorrespondenceAddressRequest[?(@.requestDetail.eori == 'testEORI')]")
          )
          .willReturn(ok(Json.toJson(response).toString))
      )

      val result: Response = await(connector.getAccountContactDetails(AccountNumber("dan"), EORI(EORI_VALUE)))
      result mustBe response

      verifyExactlyOneEndPointUrlHit(acc38DutyDefermentContactDetailsEndpointUrl, POST)
    }
  }

  override def config: Configuration = Configuration(
    ConfigFactory.parseString(
      s"""
         |microservice {
         |  services {
         |  acc38 {
         |            host = $wireMockHost
         |            port = $wireMockPort
         |        }
         |  }
         |}
         |""".stripMargin
    )
  )

  trait Setup {
    implicit val hc: HeaderCarrier                  = HeaderCarrier()
    val acc38DutyDefermentContactDetailsEndpointUrl =
      "/customs-financials-hods-stub/accounts/getcorrespondenceaddress/v1"

    val response: domain.acc38.Response = domain.acc38.Response(
      GetCorrespondenceAddressResponse(
        domain.acc38.ResponseCommon("OK", None, emptyString, None),
        None
      )
    )

    val app: Application = application().configure(config).build()

    val connector: Acc38Connector = app.injector.instanceOf[Acc38Connector]
  }
}
