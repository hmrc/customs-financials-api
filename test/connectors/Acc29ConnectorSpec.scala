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

import com.typesafe.config.ConfigFactory
import domain.AccountWithAuthorities
import models.EORI
import models.responses.StandingAuthoritiesResponse
import play.api.{Application, Configuration}
import play.api.test.Helpers.*
import uk.gov.hmrc.http.HeaderCarrier
import utils.{SpecBase, WireMockSupportProvider}
import play.api.libs.json.Json
import com.github.tomakehurst.wiremock.http.RequestMethod.POST
import com.github.tomakehurst.wiremock.client.WireMock.{equalTo, matchingJsonPath, ok, post, urlPathMatching}
import config.MetaConfig.Platform.MDTP
import utils.TestData.EORI_VALUE

class Acc29ConnectorSpec extends SpecBase with WireMockSupportProvider {

  "getStandingAuthorities" should {
    "return a list of authorities on a successful response" in new Setup {

      wireMockServer.stubFor(
        post(urlPathMatching(getStandingAuthoritiesUrl))
          .withHeader(X_FORWARDED_HOST, equalTo(MDTP))
          .withHeader(CONTENT_TYPE, equalTo(CONTENT_TYPE_APPLICATION_JSON))
          .withHeader(ACCEPT, equalTo(CONTENT_TYPE_APPLICATION_JSON))
          .withHeader(AUTHORIZATION, equalTo(AUTH_BEARER_TOKEN_VALUE))
          .withRequestBody(matchingJsonPath("$.requestCommon[?(@.regime == 'CDS')]"))
          .withRequestBody(matchingJsonPath("$.requestDetail[?(@.ownerEori == 'testEORI')]"))
          .willReturn(ok(Json.toJson(response).toString))
      )

      val result: Seq[AccountWithAuthorities] = await(connector.getStandingAuthorities(EORI(EORI_VALUE)))
      result mustBe Seq.empty

      verifyExactlyOneEndPointUrlHit(getStandingAuthoritiesUrl, POST)
    }
  }

  override def config: Configuration = Configuration(
    ConfigFactory.parseString(
      s"""
         |microservice {
         |  services {
         |  acc29 {
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

    val getStandingAuthoritiesUrl = "/customs-financials-hods-stub/accounts/getstandingauthoritydetails/v1"

    val response: StandingAuthoritiesResponse = StandingAuthoritiesResponse(EORI(EORI_VALUE), List.empty)

    val app: Application = application().configure(config).build()

    val connector: Acc29Connector = app.injector.instanceOf[Acc29Connector]
  }
}
