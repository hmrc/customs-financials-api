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
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import play.api.{Application, Configuration}
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.Helpers.*
import uk.gov.hmrc.http.client.{HttpClientV2, RequestBuilder}
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse, NotFoundException}
import utils.{SpecBase, WireMockSupportProvider}
import utils.Utils.emptyString
import com.typesafe.config.ConfigFactory
import play.api.libs.json.Json
import com.github.tomakehurst.wiremock.client.WireMock.{aResponse, equalToJson, ok, post, serverError, urlPathMatching}
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

    verifyEndPointUrlHit(sendEmailEndpointUrl, POST)
  }

  "return false when the api responds with a successful response that isn't 204" in new Setup {

    wireMockServer.stubFor(
      post(urlPathMatching(sendEmailEndpointUrl))
        .withRequestBody(equalToJson(Json.toJson(request).toString))
        .willReturn(ok(emptyString))
    )

    val result: Boolean = await(connector.sendEmail(request))
    result mustBe false

    verifyEndPointUrlHit(sendEmailEndpointUrl, POST)
  }

  "return false when the api fails" in new Setup {
    val mockHttpClient: HttpClientV2   = mock[HttpClientV2]
    val requestBuilder: RequestBuilder = mock[RequestBuilder]

    when(requestBuilder.withBody(any())(any(), any(), any())).thenReturn(requestBuilder)
    when(requestBuilder.setHeader(any[(String, String)]())).thenReturn(requestBuilder)
    when(mockHttpClient.post(any)(any)).thenReturn(requestBuilder)
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

    val emailThrottlerConnector: EmailThrottlerConnector = application.injector.instanceOf[EmailThrottlerConnector]

    running(application) {
      val result = await(emailThrottlerConnector.sendEmail(request))
      result mustBe false
    }
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
