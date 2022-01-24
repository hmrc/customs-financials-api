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

import domain.tpi01._
import models.EORI
import play.api.Application
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.Helpers._
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient}
import utils.SpecBase

import scala.concurrent.Future

class Tpi01ConnectorSpec extends SpecBase {

  "getReimbursementClaims" should {
    "return reimbursement claims on a successful response" in new Setup {
      when[Future[GetReimbursementClaimsResponse]](mockHttpClient.POST(any, any, any)(any, any, any, any))
        .thenReturn(Future.successful(response))

      running(app) {
        val result = await(connector.retrieveReimbursementClaims(EORI("GB138153003838312")))
        result mustBe response
      }
    }

//    "return NoAssociatedData error response when responded with no associated data" in new Setup {
//      when[Future[Response]](mockHttpClient.POST(any, any, any)(any, any, any, any))
//        .thenReturn(Future.successful(noDataResponse))
//
//      running(app) {
//        val result = await(connector.retrieveGuaranteeTransactions(request))
//        result mustBe Left(NoAssociatedDataException)
//      }
//    }
//
//    "return ExceededThreshold error response when responded with exceeded threshold" in new Setup {
//      when[Future[GuaranteeTransactionsResponse]](mockHttpClient.POST(any, any, any)(any, any, any, any))
//        .thenReturn(Future.successful(tooMuchDataRequestedResponse))
//
//      running(app) {
//        val result = await(connector.retrieveGuaranteeTransactions(request))
//        result mustBe Left(ExceededThresholdErrorException)
//      }
//    }
  }

  trait Setup {
    implicit val hc: HeaderCarrier = HeaderCarrier()
    val mockHttpClient: HttpClient = mock[HttpClient]

    val response: GetReimbursementClaimsResponse = GetReimbursementClaimsResponse(
        ResponseCommon("OK", LocalDate.now().toString, None, None, None),
        Some(ResponseDetail(cdfPayClaimsFound = true, Some(Array(cdfPayCase))))
    )

    val cdfPayCase: CDFPayCase = CDFPayCase("4374422408", "NDRC", "Resolved-Completed", "GB138153003838312", "GB138153003838312",
      Some("GB138153003838312"), Some("10.00"), Some("10.00"))

    val app: Application = GuiceApplicationBuilder().overrides(
      bind[HttpClient].toInstance(mockHttpClient)
    ).configure(
      "microservice.metrics.enabled" -> false,
      "metrics.enabled" -> false,
      "auditing.enabled" -> false
    ).build()

    val connector: Tpi01Connector = app.injector.instanceOf[Tpi01Connector]
  }
}
