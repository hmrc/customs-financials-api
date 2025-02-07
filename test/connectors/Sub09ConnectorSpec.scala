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

import domain.sub09.*
import models.EORI
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import play.api.{Application, Configuration}
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.Helpers.*
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.http.client.{HttpClientV2, RequestBuilder}
import utils.{SpecBase, WireMockSupportProvider}
import utils.TestData.COUNTRY_CODE_GB
import play.api.libs.json.Json
import com.github.tomakehurst.wiremock.client.WireMock.{equalTo, get, matchingJsonPath, ok, urlPathMatching}
import com.github.tomakehurst.wiremock.http.RequestMethod.GET
import config.MetaConfig.Platform.{MDTP, REGIME_CDS}
import utils.TestData.EORI_VALUE_1
import com.typesafe.config.ConfigFactory

import scala.concurrent.Future

class Sub09ConnectorSpec extends SpecBase with WireMockSupportProvider {

  "getSubscriptions" should {
    "return a json on a successful response" in new Setup {

      wireMockServer.stubFor(
        get(urlPathMatching(sub09GetSubscriptionsEndpointUrl))
          .withHeader(X_FORWARDED_HOST, equalTo(MDTP))
          .withHeader(CONTENT_TYPE, equalTo("application/json"))
          .withHeader(ACCEPT, equalTo("application/json"))
          .withHeader(AUTHORIZATION, equalTo("Bearer test1234567"))
          .withQueryParam("EORI", equalTo(EORI_VALUE_1))
          .withQueryParam("regime", equalTo(REGIME_CDS))
          .willReturn(ok(Json.toJson(response).toString))
      )

      val result: SubscriptionResponse = await(connector.getSubscriptions(EORI(EORI_VALUE_1)))
      result mustBe response

      verifyEndPointUrlHit(sub09GetSubscriptionsEndpointUrl, GET)
    }
  }

  override def config: Configuration = Configuration(
    ConfigFactory.parseString(
      s"""
         |microservice {
         |  services {
         |  sub09 {
         |            host = $wireMockHost
         |            port = $wireMockPort
         |        }
         |  }
         |}
         |""".stripMargin
    )
  )

  trait Setup {
    implicit val hc: HeaderCarrier       = HeaderCarrier()
    val sub09GetSubscriptionsEndpointUrl = "/customs-financials-hods-stub/subscriptions/subscriptiondisplay/v1"

    val responseCommon: ResponseCommon                   = ResponseCommon("OK", None, "2020-10-05T09:30:47Z", None)
    val cdsEstablishmentAddress: CdsEstablishmentAddress =
      CdsEstablishmentAddress("Example Street", "Example", Some("A00 0AA"), COUNTRY_CODE_GB)

    val vatIds: VatId             = VatId(Some("abc"), Some("123"))
    val euVatIds: EUVATNumber     = EUVATNumber(Some("def"), Some("456"))
    val xiEoriAddress: PbeAddress = PbeAddress("1 Test street", Some("city A"), Some("county"), None, Some("AA1 1AA"))

    val xiEoriSubscription: XiSubscription = XiSubscription(
      "XI1234567",
      Some(xiEoriAddress),
      Some("1"),
      Some("12345"),
      Some(Array(euVatIds)),
      "1",
      Some("abc")
    )

    val responseDetail: ResponseDetail = ResponseDetail(
      Some(EORI(EORI_VALUE_1)),
      None,
      None,
      "CDSFullName",
      cdsEstablishmentAddress,
      Some("0"),
      None,
      None,
      Some(Array(vatIds)),
      None,
      None,
      None,
      None,
      None,
      None,
      ETMP_Master_Indicator = true,
      Some(xiEoriSubscription)
    )

    val response: SubscriptionResponse =
      SubscriptionResponse(SubscriptionDisplayResponse(responseCommon, responseDetail))

    val app: Application          = application().configure(config).build()
    val connector: Sub09Connector = app.injector.instanceOf[Sub09Connector]
  }
}
