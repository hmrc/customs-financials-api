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

import domain.tpi01._
import models.EORI
import play.api.Application
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.Helpers._
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient}
import utils.SpecBase

import java.time.LocalDate
import scala.concurrent.Future

class Tpi01ConnectorSpec extends SpecBase {

  "retrievePostClearanceCases" should {
    "return cases on a successful response" in new Setup {
      when[Future[Response]](mockHttpClient.POST(any, any, any)(any, any, any, any))
        .thenReturn(Future.successful(response))

      running(app) {
        val result = await(connector.retrievePostClearanceCases(EORI("GB138153003838312"), "A"))
        result mustBe response
      }
    }
  }

  trait Setup {
    implicit val hc: HeaderCarrier = HeaderCarrier()
    val mockHttpClient: HttpClient = mock[HttpClient]

    val app: Application = GuiceApplicationBuilder().overrides(
      bind[HttpClient].toInstance(mockHttpClient)
    ).configure(
      "microservice.metrics.enabled" -> false,
      "metrics.enabled" -> false,
      "auditing.enabled" -> false
    ).build()

    val connector: Tpi01Connector = app.injector.instanceOf[Tpi01Connector]

    val ndrcCaseDetails: NDRCCaseDetails = NDRCCaseDetails(CDFPayCaseNumber = "NDRC-2109", declarationID = Some("21LLLLLLLLLLLLLLL9"),
      claimStartDate = "20211120", closedDate = Some("00000000"), caseStatus = "Open", caseSubStatus = Some("Open"), declarantEORI = "GB744638982000",
      importerEORI = "GB744638982000", claimantEORI = Some("GB744638982000"), totalCustomsClaimAmount = Some("3000.20"),
      totalVATClaimAmount = Some("784.66"), totalExciseClaimAmount = Some("1200.00"), declarantReferenceNumber = Some("KWMREF1"),
      basisOfClaim = Some("Duplicate Entry"))

    val sctyCaseDetails: SCTYCaseDetails = SCTYCaseDetails(CDFPayCaseNumber = "SEC-2109", declarationID = Some("21LLLLLLLLLL12345"),
      claimStartDate = "20210320", closedDate = Some("00000000"), reasonForSecurity = "ACS", caseStatus = "Open", caseSubStatus = Some("Open"),
      declarantEORI = "GB744638982000", importerEORI = "GB744638982000", claimantEORI = Some("GB744638982000"),
      totalCustomsClaimAmount = Some("12000.56"), totalVATClaimAmount = Some("3412.01"), declarantReferenceNumber = Some("broomer007"))

    val response: Response = Response(GetReimbursementClaimsResponse(
        ResponseCommon("OK", LocalDate.now().toString, None, None, None),
        Some(ResponseDetail(NDRCCasesFound = true, SCTYCasesFound= true,
          Some(CDFPayCase(NDRCCaseTotal = Some("1"), NDRCCases = Some(Seq(ndrcCaseDetails)),
            SCTYCaseTotal = Some("1"), SCTYCases = Some(Seq(sctyCaseDetails))))))
    ))
  }
}
