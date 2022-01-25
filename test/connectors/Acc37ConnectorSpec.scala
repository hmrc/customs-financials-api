/*
 * Copyright 2022 HM Revenue & Customs
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
import play.api.Application
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.Helpers._
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient}
import utils.SpecBase

import scala.concurrent.Future


class Acc37ConnectorSpec extends SpecBase {

  "updateAccountContactDetails" should {
    "return an acc37 response on a successful api call" in new Setup {
      when[Future[domain.acc37.Response]](mockHttpClient.POST(any, any, any)(any, any, any, any))
        .thenReturn(Future.successful(response))

      running(app) {
        val result = await(connector.updateAccountContactDetails(AccountNumber("dan"), EORI("someEori"), acc37ContactInfo))
        result mustBe response
      }
    }
  }

  trait Setup {
    implicit val hc: HeaderCarrier = HeaderCarrier()
    val mockHttpClient: HttpClient = mock[HttpClient]

    val response: domain.acc37.Response = domain.acc37.Response(
      AmendCorrespondenceAddressResponse(
        ResponseCommon(
          "OK",
          None,
          "",
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
      "GB",
      Some("011111111111"),
      None,
      Some(EmailAddress("somedata@email.com"))
    )

    val app: Application = GuiceApplicationBuilder().overrides(
      bind[HttpClient].toInstance(mockHttpClient)
    ).configure(
      "microservice.metrics.enabled" -> false,
      "metrics.enabled" -> false,
      "auditing.enabled" -> false
    ).build()

    val connector: Acc37Connector = app.injector.instanceOf[Acc37Connector]
  }
}
