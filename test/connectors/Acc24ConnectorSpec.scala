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
import models.requests.{HistoricDocumentRequest, HistoricStatementRequest}
import play.api.{Application, Configuration}
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.Helpers.*
import play.api.libs.json.Json
import uk.gov.hmrc.http.HeaderCarrier
import utils.SpecBase
import utils.TestData.{FILE_ROLE_C79_CERTIFICATE, MONTH_10, YEAR_2019}
import utils.Utils.emptyString
import utils.WireMockSupportProvider
import uk.gov.hmrc.http.HttpReads.Implicits.*
import com.github.tomakehurst.wiremock.client.WireMock.{equalTo, noContent, ok, post, serverError, urlPathMatching}
import com.github.tomakehurst.wiremock.http.RequestMethod.POST
import com.typesafe.config.ConfigFactory

import scala.concurrent.Future
import config.MetaConfig.Platform.MDTP

class Acc24ConnectorSpec extends SpecBase with WireMockSupportProvider {

  "sendHistoricDocumentRequest" should {
    "return true when a successful request has been made" in new Setup {

      wireMockServer.stubFor(
        post(urlPathMatching(historicStatementEndPointUrl))
          .withHeader(X_FORWARDED_HOST, equalTo(MDTP))
          .withHeader(CONTENT_TYPE, equalTo("application/json"))
          .withHeader(ACCEPT, equalTo("application/json"))
          .withHeader(AUTHORIZATION, equalTo("Bearer test1234567"))
          .willReturn(noContent)
      )

      val result: Boolean = await(connector.sendHistoricDocumentRequest(historicDocumentRequest))
      result mustBe true

      verifyEndPointUrlHit(historicStatementEndPointUrl, POST)
    }

    "return false if any other 2xx status code is returned" in new Setup {

      wireMockServer.stubFor(
        post(urlPathMatching(historicStatementEndPointUrl))
          .withHeader(X_FORWARDED_HOST, equalTo(MDTP))
          .withHeader(CONTENT_TYPE, equalTo("application/json"))
          .withHeader(ACCEPT, equalTo("application/json"))
          .withHeader(AUTHORIZATION, equalTo("Bearer test1234567"))
          .willReturn(ok)
      )

      val result: Boolean = await(connector.sendHistoricDocumentRequest(historicDocumentRequest))
      result mustBe false

      verifyEndPointUrlHit(historicStatementEndPointUrl, POST)
    }

    "return false if an exception from Acc24 is returned" in new Setup {

      wireMockServer.stubFor(
        post(urlPathMatching(historicStatementEndPointUrl))
          .withHeader(X_FORWARDED_HOST, equalTo(MDTP))
          .withHeader(CONTENT_TYPE, equalTo("application/json"))
          .withHeader(ACCEPT, equalTo("application/json"))
          .withHeader(AUTHORIZATION, equalTo("Bearer test1234567"))
          .willReturn(serverError)
      )

      val result: Boolean = await(connector.sendHistoricDocumentRequest(historicDocumentRequest))
      result mustBe false

      verifyEndPointUrlHit(historicStatementEndPointUrl, POST)
    }
  }

  override def config: Configuration = Configuration(
    ConfigFactory.parseString(
      s"""
         |microservice {
         |  services {
         |  acc24 {
         |            host = $wireMockHost
         |            port = $wireMockPort
         |            context-base = "/customs-financials-hods-stub"
         |            bearer-token = "test1234567"
         |            serviceName="hods-acc24"
         |        }
         |  }
         |}
         |""".stripMargin
    )
  )

  trait Setup {
    implicit val hc: HeaderCarrier = HeaderCarrier()

    val historicStatementEndPointUrl = "/customs-financials-hods-stub/accounts/cmdghistoricalstatementretrieval/v1"

    val historicDocumentRequest: HistoricDocumentRequest =
      HistoricDocumentRequest(
        EORI("someEori"),
        FILE_ROLE_C79_CERTIFICATE,
        YEAR_2019,
        MONTH_10,
        YEAR_2019,
        MONTH_10,
        Some("dan")
      )

    val app: Application = application().configure(config).build()

    val connector: Acc24Connector = app.injector.instanceOf[Acc24Connector]
  }
}
