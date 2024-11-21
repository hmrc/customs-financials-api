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

import domain.acc37.{AmendCorrespondenceAddressResponse, ContactDetails, ResponseCommon}
import models.{AccountNumber, EORI, EmailAddress}
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
import utils.Utils.emptyString

import scala.concurrent.Future


class Acc37ConnectorSpec extends SpecBase {

  "updateAccountContactDetails" should {
    "return an acc37 response on a successful api call" in new Setup {
      when(requestBuilder.withBody(any())(any(), any(), any())).thenReturn(requestBuilder)
      when(requestBuilder.setHeader(any[(String, String)]())).thenReturn(requestBuilder)
      when(mockHttpClient.post(any)(any)).thenReturn(requestBuilder)
      when(requestBuilder.execute(any, any)).thenReturn(Future.successful(response))

      running(app) {
        val result =
          await(connector.updateAccountContactDetails(AccountNumber("dan"), EORI("someEori"), acc37ContactInfo))

        result mustBe response
      }
    }
  }

  trait Setup {
    implicit val hc: HeaderCarrier = HeaderCarrier()
    val mockHttpClient: HttpClientV2 = mock[HttpClientV2]
    val requestBuilder: RequestBuilder = mock[RequestBuilder]

    val response: domain.acc37.Response = domain.acc37.Response(
      AmendCorrespondenceAddressResponse(
        ResponseCommon(
          "OK",
          None,
          emptyString,
          None
        )
      )
    )

    val acc37ContactInfo: ContactDetails = domain.acc37.ContactDetails(
      Some("John Doe"),
      "Jone Doe Lane",
      Some("Docks"),
      None,
      Some("Docks"),
      Some("DDD 111"),
      COUNTRY_CODE_GB,
      Some("011111111111"),
      None,
      Some(EmailAddress("somedata@email.com"))
    )

    val app: Application = GuiceApplicationBuilder().overrides(
      bind[HttpClientV2].toInstance(mockHttpClient),
      bind[RequestBuilder].toInstance(requestBuilder)
    ).configure(
      "microservice.metrics.enabled" -> false,
      "metrics.enabled" -> false,
      "auditing.enabled" -> false
    ).build()

    val connector: Acc37Connector = app.injector.instanceOf[Acc37Connector]
  }
}
