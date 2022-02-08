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

import java.time.LocalDate

import domain.tpi02._
import play.api.Application
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.Helpers._
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient}
import utils.SpecBase

import scala.concurrent.Future

class Tpi02ConnectorSpec extends SpecBase {

  "retrieveSpecificClaim" should {
    "return specific claim on a successful response" in new Setup {
      when[Future[Response]](mockHttpClient.POST(any, any, any)(any, any, any, any))
        .thenReturn(Future.successful(response))

      running(app) {
        val result = await(connector.retrieveSpecificClaim("", ""))
        result mustBe response
      }
    }
  }

  trait Setup {
    implicit val hc: HeaderCarrier = HeaderCarrier()
    val mockHttpClient: HttpClient = mock[HttpClient]

    val response: Response = Response(GetSpecificClaimResponse(
        ResponseCommon("OK", LocalDate.now().toString, None, None, None),
        Some(ResponseDetail("MDTP", Some(cdfPayCase))))
    )

    val cdfPayCase: CDFPayCase = CDFPayCase("Resolved-Completed", "4374422408", "GB138153003838312", "GB138153003838312",
      Some("GB138153003838312"), Some("10.00"), Some("10.00"), Some("10.00"), "10.00", "10.00", Some("10.00"), Some(reimbursement))

    val reimbursement: Reimbursement = Reimbursement("date", "10.00", "10.00")

    val app: Application = GuiceApplicationBuilder().overrides(
      bind[HttpClient].toInstance(mockHttpClient)
    ).configure(
      "microservice.metrics.enabled" -> false,
      "metrics.enabled" -> false,
      "auditing.enabled" -> false
    ).build()

    val connector: Tpi02Connector = app.injector.instanceOf[Tpi02Connector]
  }
}