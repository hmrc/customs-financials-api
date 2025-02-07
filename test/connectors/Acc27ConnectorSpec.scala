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

import play.api.{Application, Configuration}
import play.api.libs.json.{JsObject, Json}
import play.api.test.Helpers.*
import uk.gov.hmrc.http.HeaderCarrier
import utils.{SpecBase, WireMockSupportProvider}
import com.github.tomakehurst.wiremock.client.WireMock.{equalTo, equalToJson, ok, post, urlPathMatching}
import com.github.tomakehurst.wiremock.http.RequestMethod.POST
import config.MetaConfig.Platform.MDTP
import com.typesafe.config.ConfigFactory

import scala.concurrent.Future

class Acc27ConnectorSpec extends SpecBase with WireMockSupportProvider {

  "getAccounts" should {
    "return a json on a successful response" in new Setup {

      wireMockServer.stubFor(
        post(urlPathMatching(getAccountsUrl))
          .withHeader(X_FORWARDED_HOST, equalTo(MDTP))
          .withHeader(CONTENT_TYPE, equalTo("application/json"))
          .withHeader(ACCEPT, equalTo("application/json"))
          .withHeader(AUTHORIZATION, equalTo("Bearer test1234567"))
          .withRequestBody(equalToJson(requestBody.toString))
          .willReturn(ok(Json.obj("someOther" -> "value").toString))
      )

      val result: JsObject = await(connector.getAccounts(requestBody))
      result mustBe Json.obj("someOther" -> "value")

      verifyEndPointUrlHit(getAccountsUrl, POST)
    }
  }

  override def config: Configuration = Configuration(
    ConfigFactory.parseString(
      s"""
         |microservice {
         |  services {
         |  acc27 {
         |            host = $wireMockHost
         |            port = $wireMockPort
         |            context-base = "/customs-financials-hods-stub"
         |            bearer-token = "test1234567"
         |            endpoint = "/accounts/getaccountsandbalances/v1"
         |            serviceName="hods-acc27"
         |        }
         |  }
         |}
         |""".stripMargin
    )
  )

  trait Setup {
    implicit val hc: HeaderCarrier = HeaderCarrier()
    val requestBody: JsObject      = Json.obj("some" -> "value")

    val getAccountsUrl = "/customs-financials-hods-stub/accounts/getaccountsandbalances/v1"

    val app: Application = application().configure(config).build()

    val connector: Acc27Connector = app.injector.instanceOf[Acc27Connector]
  }
}
