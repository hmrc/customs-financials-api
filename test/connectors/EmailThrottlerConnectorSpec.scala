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

import models.requests.EmailRequest
import play.api.{Application, Configuration}
import play.api.test.Helpers.*
import uk.gov.hmrc.http.HeaderCarrier
import utils.{SpecBase, WireMockSupportProvider}
import utils.Utils.emptyString
import com.typesafe.config.ConfigFactory
import play.api.libs.json.Json
import com.github.tomakehurst.wiremock.client.WireMock.{aResponse, equalToJson, ok, post, urlPathMatching}
import com.github.tomakehurst.wiremock.http.Fault
import com.github.tomakehurst.wiremock.http.RequestMethod.POST

import scala.concurrent.Future

class EmailThrottlerConnectorSpec extends SpecBase with WireMockSupportProvider {

  "return true when the api responds with 202" in new Setup {
    wireMockServer.stubFor(
      post(urlPathMatching(sendEmailEndpointUrl))
        .withRequestBody(equalToJson(Json.toJson(request).toString))
        .willReturn(aResponse().withStatus(ACCEPTED).withBody(emptyString))
    )

    val result: Boolean = await(connector.sendEmail(request))
    result mustBe true

    verifyExactlyOneEndPointUrlHit(sendEmailEndpointUrl, POST)
  }

  "return false when the api responds with a successful response that isn't 204" in new Setup {

    wireMockServer.stubFor(
      post(urlPathMatching(sendEmailEndpointUrl))
        .withRequestBody(equalToJson(Json.toJson(request).toString))
        .willReturn(ok(emptyString))
    )

    val result: Boolean = await(connector.sendEmail(request))
    result mustBe false

    verifyExactlyOneEndPointUrlHit(sendEmailEndpointUrl, POST)
  }

  "return false when the api fails due to connection reset" in new Setup {
    wireMockServer.stubFor(
      post(urlPathMatching(sendEmailEndpointUrl))
        .withRequestBody(equalToJson(Json.toJson(request).toString))
        .willReturn(aResponse().withFault(Fault.CONNECTION_RESET_BY_PEER))
    )

    val result: Boolean = await(connector.sendEmail(request))
    result mustBe false

    verifyEndPointUrlHit(sendEmailEndpointUrl, POST)
  }

  override def config: Configuration = Configuration(
    ConfigFactory.parseString(
      s"""
         |microservice {
         |  services {
         |  customs-financials-email-throttler {
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
    val sendEmailEndpointUrl       = "/customs-financials-email-throttler/enqueue-email"

    val request: EmailRequest = EmailRequest(List.empty, emptyString, Map.empty, force = true, None, Some("eori"), None)

    val app: Application                   = application().configure(config).build()
    val connector: EmailThrottlerConnector = app.injector.instanceOf[EmailThrottlerConnector]
  }
}
