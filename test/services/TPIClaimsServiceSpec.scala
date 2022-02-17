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

package services

import java.time.LocalDate
import connectors.{Tpi01Connector, Tpi02Connector}
import domain._
import domain.tpi01._
import domain.tpi02.{GetSpecificClaimResponse, Reimbursement}
import models.EORI
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.Helpers.running
import play.api.{Application, inject}
import uk.gov.hmrc.http.HeaderCarrier
import utils.SpecBase
import scala.concurrent.{ExecutionContext, Future}

class TPIClaimsServiceSpec extends SpecBase {

  "TPIClaimsService" when {

    "calling get Claims" should {
      "successfully returns CDFPayCaseDetail" in new Setup {

        val ndrcCaseDetails: NDRCCaseDetails = NDRCCaseDetails(CDFPayCaseNumber = "NDRC-2109", declarationID = Some("21LLLLLLLLLLLLLLL9"),
          claimStartDate = "20211120", closedDate = Some("00000000"), caseStatus = "Open", declarantEORI = "GB744638982000",
          importerEORI = "GB744638982000", claimantEORI = Some("GB744638982000"), totalCustomsClaimAmount = Some("3000.20"),
          totalVATClaimAmount = Some("784.66"), totalExciseClaimAmount = Some("1200.00"), declarantReferenceNumber = Some("KWMREF1"),
          basisOfClaim = Some("Duplicate Entry"))

        val sctyCaseDetails: SCTYCaseDetails = SCTYCaseDetails(CDFPayCaseNumber = "SEC-2109", declarationID = Some("21LLLLLLLLLL12345"),
          claimStartDate = "20210320", closedDate = Some("00000000"), reasonForSecurity = "ACS", caseStatus = "Open",
          declarantEORI = "GB744638982000", importerEORI = "GB744638982000", claimantEORI = Some("GB744638982000"),
          totalCustomsClaimAmount = Some("12000.56"), totalVATClaimAmount = Some("3412.01"), declarantReferenceNumber = Some("broomer007"))

        val responseDetail: ResponseDetail = ResponseDetail(NDRCCasesFound = true, SCTYCasesFound= true,
          Some(CDFPayCase(NDRCCaseTotal = Some("1"), NDRCCases = Some(Seq(ndrcCaseDetails)),
            SCTYCaseTotal = Some("1"), SCTYCases = Some(Seq(sctyCaseDetails)))))

        val response: Response = Response(GetReimbursementClaimsResponse(ResponseCommon("OK",
          LocalDate.now().toString, None, None, None), Some(responseDetail)))

        when(mockTpi01Connector.retrievePostClearanceCases(any, any))
          .thenReturn(Future.successful(response))

        running(app) {
          val result = await(service.getClaims(EORI("Trader EORI"), "A"))
          result mustBe Some(responseDetail)
        }
      }
    }

    "calling get Specific Claims" should {
      "successfully returns CDFPayCase" in new Setup {
        val cdfPayCase: tpi02.CDFPayCase = tpi02.CDFPayCase("Open", "4374422408", "GB138153003838312", "GB138153003838312",
          Some("GB138153003838312"), Some("10.00"), Some("10.00"), Some("10.00"), "10.00", "10.00", Some("10.00"), Some(Reimbursement("date", "10.00", "10.00")))

        val getSpecificClaimResponse: tpi02.CDFPayCase = tpi02.CDFPayCase("In Progress", "4374422408", "GB138153003838312", "GB138153003838312",
          Some("GB138153003838312"), Some("10.00"), Some("10.00"), Some("10.00"), "10.00", "10.00", Some("10.00"), Some(Reimbursement("date", "10.00", "10.00")))

        val responseSpecificClaim: tpi02.Response = tpi02.Response(GetSpecificClaimResponse(
          tpi02.ResponseCommon("OK", LocalDate.now().toString, None, None, None),
          Some(tpi02.ResponseDetail("MDTP", Some(cdfPayCase)))
        ))

        when(mockTpi02Connector.retrieveSpecificClaim(any, any))
          .thenReturn(Future.successful(responseSpecificClaim))

        running(app) {
          val result = await(service.getSpecificClaim("CDFPayService", "CDFPayCaseNumber"))
          result mustBe Some(getSpecificClaimResponse)
        }
      }
    }
  }

  trait Setup {
    implicit val hc: HeaderCarrier = HeaderCarrier()
    implicit val executionContext: ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global

    val eori: EORI = EORI("testEORI")
    val mockTpi01Connector: Tpi01Connector = mock[Tpi01Connector]
    val mockTpi02Connector: Tpi02Connector = mock[Tpi02Connector]

    val app: Application = GuiceApplicationBuilder().overrides(
      inject.bind[Tpi01Connector].toInstance(mockTpi01Connector),
      inject.bind[Tpi02Connector].toInstance(mockTpi02Connector)
    ).configure(
      "microservice.metrics.enabled" -> false,
      "metrics.enabled" -> false,
      "auditing.enabled" -> false
    ).build()

    val service: TPIClaimsService = app.injector.instanceOf[TPIClaimsService]
  }
}
