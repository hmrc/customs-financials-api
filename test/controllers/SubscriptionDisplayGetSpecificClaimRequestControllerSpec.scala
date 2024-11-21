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

package controllers

import connectors.Sub09Connector
import domain.sub09.*
import models.EORI
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import play.api.{Application, inject}
import uk.gov.hmrc.http.UpstreamErrorResponse
import utils.SpecBase
import utils.TestData.COUNTRY_CODE_GB

import scala.concurrent.Future

class SubscriptionDisplayGetSpecificClaimRequestControllerSpec extends SpecBase {

  "SubscriptionRequestController.get" should {

    "validate the EORI and return 200 status code" in new Setup {
      val responseCommon: ResponseCommon =
        ResponseCommon("OK", Some("Processed successfully"), "2020-10-05T09:30:47Z", None)

      val cdsEstablishmentAddress: CdsEstablishmentAddress =
        CdsEstablishmentAddress("Example Street", "Example", Some("A00 0AA"), COUNTRY_CODE_GB)

      val vatIds: VatId = VatId(Some("abc"), Some("123"))
      val euVatIds: EUVATNumber = EUVATNumber(Some("def"), Some("456"))

      val xiEoriAddress: PbeAddress = PbeAddress("1 Test street", Some("city A"), Some("county"), None, Some("AA1 1AA"))
      val xiEoriSubscription: XiSubscription =
        XiSubscription("XI1234567",
          Some(xiEoriAddress),
          Some("1"),
          Some("12345"),
          Some(Array(euVatIds)),
          "1",
          Some("abc"))

      val responseDetail: ResponseDetail = ResponseDetail(Some(EORI("someEori")), None, None, "CDSFullName",
        cdsEstablishmentAddress, Some("0"), None, None, Some(Array(vatIds)),
        None, None, None, None, None, None, ETMP_Master_Indicator = true, Some(xiEoriSubscription))

      val response: SubscriptionResponse =
        SubscriptionResponse(SubscriptionDisplayResponse(responseCommon, responseDetail))

      when(mockSub09Connector.getSubscriptions(any))
        .thenReturn(Future.successful(response))

      running(app) {
        val result = route(app, request).value
        status(result) mustBe OK
      }
    }

    "return 404 for invalid EORI" in new Setup {
      when(mockSub09Connector.getSubscriptions(any))
        .thenReturn(Future.failed(UpstreamErrorResponse("failed", NOT_FOUND, NOT_FOUND, Map.empty)))

      running(app) {
        val result = route(app, request).value
        status(result) mustBe NOT_FOUND
      }
    }
  }

  trait Setup {
    val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest(GET, "/customs-financials-api/eori/testEORI/validate")
    val mockAuthConnector: CustomAuthConnector = mock[CustomAuthConnector]
    val mockSub09Connector: Sub09Connector = mock[Sub09Connector]

    val app: Application = GuiceApplicationBuilder().overrides(
      inject.bind[Sub09Connector].toInstance(mockSub09Connector)
    ).configure(
      "microservice.metrics.enabled" -> false,
      "metrics.enabled" -> false,
      "auditing.enabled" -> false
    ).build()
  }
}
