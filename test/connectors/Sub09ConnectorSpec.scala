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
import play.api.Application
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.Helpers.*
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.http.client.{HttpClientV2, RequestBuilder}
import utils.SpecBase
import utils.TestData.COUNTRY_CODE_GB

import scala.concurrent.Future

class Sub09ConnectorSpec extends SpecBase {

  "getSubscriptions" should {
    "return a json on a successful response" in new Setup {
      when(requestBuilder.withBody(any())(any(), any(), any())).thenReturn(requestBuilder)
      when(requestBuilder.setHeader(any[(String, String)]())).thenReturn(requestBuilder)
      when(mockHttpClient.get(any)(any)).thenReturn(requestBuilder)
      when(requestBuilder.execute(any, any)).thenReturn(Future.successful(response))

      running(app) {
        val result = await(connector.getSubscriptions(EORI("someEori")))
        result mustBe response
      }
    }
  }

  trait Setup {
    implicit val hc: HeaderCarrier = HeaderCarrier()
    val mockHttpClient: HttpClientV2 = mock[HttpClientV2]
    val requestBuilder: RequestBuilder = mock[RequestBuilder]
    val responseCommon: ResponseCommon = ResponseCommon("OK", None, "2020-10-05T09:30:47Z", None)
    val cdsEstablishmentAddress: CdsEstablishmentAddress =
      CdsEstablishmentAddress("Example Street", "Example", Some("A00 0AA"), COUNTRY_CODE_GB)

    val vatIds: VatId = VatId(Some("abc"), Some("123"))
    val euVatIds: EUVATNumber = EUVATNumber(Some("def"), Some("456"))
    val xiEoriAddress: PbeAddress = PbeAddress("1 Test street", Some("city A"), Some("county"), None, Some("AA1 1AA"))

    val xiEoriSubscription: XiSubscription = XiSubscription("XI1234567", Some(xiEoriAddress), Some("1"),
      Some("12345"), Some(Array(euVatIds)), "1", Some("abc"))

    val responseDetail: ResponseDetail = ResponseDetail(Some(EORI("someEori")), None, None, "CDSFullName",
      cdsEstablishmentAddress, Some("0"), None, None, Some(Array(vatIds)),
      None, None, None, None, None, None, ETMP_Master_Indicator = true, Some(xiEoriSubscription))

    val response: SubscriptionResponse =
      SubscriptionResponse(SubscriptionDisplayResponse(responseCommon, responseDetail))

    val app: Application = GuiceApplicationBuilder().overrides(
      bind[HttpClientV2].toInstance(mockHttpClient),
      bind[RequestBuilder].toInstance(requestBuilder)
    ).configure(
      "microservice.metrics.enabled" -> false,
      "metrics.enabled" -> false,
      "auditing.enabled" -> false
    ).build()

    val connector: Sub09Connector = app.injector.instanceOf[Sub09Connector]
  }
}
