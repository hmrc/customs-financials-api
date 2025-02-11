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
import models.responses.*
import play.api.{Application, Configuration}
import play.api.test.Helpers.*
import uk.gov.hmrc.http.HeaderCarrier
import utils.{SpecBase, WireMockSupportProvider}
import utils.Utils.emptyString
import com.github.tomakehurst.wiremock.client.WireMock.{equalTo, get, ok, urlPathMatching}
import com.github.tomakehurst.wiremock.http.RequestMethod.GET
import play.api.libs.json.Json
import utils.TestData.EORI_VALUE_1
import com.typesafe.config.ConfigFactory

class Sub21ConnectorSpec extends SpecBase with WireMockSupportProvider {

  "getEoriHistory" should {
    "return a json on a successful response" in new Setup {

      wireMockServer.stubFor(
        get(urlPathMatching(sub21CheckEORIValidEndpointUrl))
          .withHeader(AUTHORIZATION, equalTo(AUTH_BEARER_TOKEN_VALUE))
          .withQueryParam(PARAM_NAME_eori, equalTo(EORI_VALUE_1))
          .willReturn(ok(Json.toJson(response).toString))
      )

      val result: HistoricEoriResponse = await(connector.getEORIHistory(EORI(EORI_VALUE_1)))
      result mustBe response

      verifyExactlyOneEndPointUrlHit(sub21CheckEORIValidEndpointUrl, GET)
    }
  }

  override def config: Configuration = Configuration(
    ConfigFactory.parseString(
      s"""
         |microservice {
         |  services {
         |  sub21 {
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

    val sub21CheckEORIValidEndpointUrl = "/customs-financials-hods-stub/subscriptions/geteorihistory/v1"

    val responseCommon: EORIHistoryResponseCommon = EORIHistoryResponseCommon("OK", emptyString)
    val eoriHistory: EORIHistory                  = EORIHistory(EORI("1212"), Some("1211"), Some("12121"))

    val eoriHistoryResponseDetail: EORIHistoryResponseDetail = EORIHistoryResponseDetail(
      Array(eoriHistory).toIndexedSeq
    )

    val response: HistoricEoriResponse =
      HistoricEoriResponse(GetEORIHistoryResponse(responseCommon, eoriHistoryResponseDetail))

    val app: Application          = application().configure(config).build()
    val connector: Sub21Connector = app.injector.instanceOf[Sub21Connector]
  }
}
