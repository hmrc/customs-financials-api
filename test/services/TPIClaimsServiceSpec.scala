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

import connectors.{Tpi01Connector, Tpi02Connector}
import domain._
import domain.tpi01._
import domain.tpi02.GetSpecificCaseResponse
import models.EORI
import models.claims.responses.{ClaimsResponse, NdrcClaimItem, SctyClaimItem, SpecificClaimResponse}
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.Helpers.running
import play.api.{Application, inject}
import uk.gov.hmrc.http.HeaderCarrier
import utils.SpecBase

import java.time.LocalDate
import scala.concurrent.{ExecutionContext, Future}

class TPIClaimsServiceSpec extends SpecBase {

  val caseStatusMappings = Seq(
    ("Open", "In Progress", None, "Open", "In Progress", None),
    ("Open-Analysis", "In Progress", None, "Pending-Approval", "Pending", None),
    ("Pending-Approval", "In Progress", None, "Pending-Payment", "Pending", None),
    ("Pending-Queried", "Pending", None, "Partial Refund", "Pending", None),
    ("Resolved-Withdrawn", "Closed", Some("Withdrawn"), "Resolved-Refund", "Closed", Some("Closed")),
    ("Rejected-Failed Validation", "Closed", Some("Failed Validation"), "Pending-Query", "Pending", None),
    ("Resolved-Rejected", "Closed", Some("Rejected"), "Resolved-Manual BTA", "Closed", Some("Closed")),
    ("Open-Rework", "In Progress", None, "Pending-C18", "Pending", None),
    ("Paused", "In Progress", None, "Closed-C18 Raised", "Closed", Some("Closed")),
    ("Resolved-No Reply", "Closed", Some("No Reply"), "RTBH Letter Initiated", "Pending", None),
    ("Resolved-Refused", "Closed", Some("Refused"), "Awaiting RTBH Letter Response", "Pending", None),
    ("Pending Payment Confirmation", "In Progress", None, "Reminder Letter Initiated", "Pending", None),
    ("Resolved-Approved", "Closed", Some("Approved"), "Awaiting Reminder Letter Response", "Pending", None),
    ("Resolved-Partial Refused", "Closed", Some("Partial Refused"), "Decision Letter Initiated", "Pending", None),
    ("Pending Decision Letter", "In Progress", None, "Partial BTA", "Pending", None),
    ("Approved", "In Progress", None, "Partial BTA/Refund", "Pending", None),
    ("Analysis-Rework", "In Progress", None, "Resolved-Auto BTA", "Closed", Some("Closed")),
    ("Rework-Payment Details", "In Progress", None, "Resolved-Manual BTA/Refund", "Closed", Some("Closed")),
    ("Pending-RTBH", "In Progress", None, "Open-Extension Granted", "In Progress", None),
    ("RTBH Sent", "Pending", None, "Open", "In Progress", None),
    ("Reply To RTBH", "Pending", None, "Pending-Approval", "Pending", None),
    ("Pending-Compliance Recommendation", "In Progress", None, "Pending-Payment", "Pending", None),
    ("Pending-Compliance Check Query", "Pending", None, "Partial Refund", "Pending", None),
    ("Pending-Compliance Check", "In Progress", None, "Resolved-Refund", "Closed", Some("Closed"))
  )

  "TPIClaimsService" when {

    "calling get Claims" should {
      caseStatusMappings.foreach { case (ndrcStatus, ndrcTransformedStatus, ndrcSubStatus, sctyStatus, sctyTransformedStatus, stcySubStatus) =>
        s"successfully returns CDFPayCaseDetail with NDRC claim status $ndrcStatus and SCTY claim status $sctyStatus" in new Setup {

          val ndrcCaseDetails: NDRCCaseDetails = NDRCCaseDetails(CDFPayCaseNumber = "NDRC-2109", declarationID = Some("21LLLLLLLLLLLLLLL9"),
            claimStartDate = "20211120", closedDate = Some("00000000"), caseStatus = ndrcStatus, declarantEORI = "GB744638982000",
            importerEORI = "GB744638982000", claimantEORI = Some("GB744638982000"), totalCustomsClaimAmount = Some("3000.20"),
            totalVATClaimAmount = Some("784.66"), totalExciseClaimAmount = Some("1200.00"), declarantReferenceNumber = Some("KWMREF1"),
            basisOfClaim = Some("Duplicate Entry"))

          val ndrcClaimItem: NdrcClaimItem = NdrcClaimItem(CDFPayCaseNumber = "NDRC-2109", declarationID = Some("21LLLLLLLLLLLLLLL9"),
            claimStartDate = "20211120", closedDate = Some("00000000"), caseStatus = ndrcTransformedStatus, caseSubStatus = ndrcSubStatus, declarantEORI = "GB744638982000",
            importerEORI = "GB744638982000", claimantEORI = Some("GB744638982000"), totalCustomsClaimAmount = Some("3000.20"),
            totalVATClaimAmount = Some("784.66"), totalExciseClaimAmount = Some("1200.00"), declarantReferenceNumber = Some("KWMREF1"),
            basisOfClaim = Some("Duplicate Entry"))

          val sctyCaseDetails: SCTYCaseDetails = SCTYCaseDetails(CDFPayCaseNumber = "SEC-2109", declarationID = Some("21LLLLLLLLLL12345"),
            claimStartDate = "20210320", closedDate = Some("00000000"), reasonForSecurity = "ACS", caseStatus = sctyStatus,
            declarantEORI = "GB744638982000", importerEORI = "GB744638982000", claimantEORI = Some("GB744638982000"),
            totalCustomsClaimAmount = Some("12000.56"), totalVATClaimAmount = Some("3412.01"), declarantReferenceNumber = Some("broomer007"))

          val sctyClaimItem: SctyClaimItem = SctyClaimItem(CDFPayCaseNumber = "SEC-2109", declarationID = Some("21LLLLLLLLLL12345"),
            claimStartDate = "20210320", closedDate = Some("00000000"), reasonForSecurity = "ACS", caseStatus = sctyTransformedStatus, caseSubStatus = stcySubStatus,
            declarantEORI = "GB744638982000", importerEORI = "GB744638982000", claimantEORI = Some("GB744638982000"),
            totalCustomsClaimAmount = Some("12000.56"), totalVATClaimAmount = Some("3412.01"), declarantReferenceNumber = Some("broomer007"))

          val responseDetail: ResponseDetail = ResponseDetail(NDRCCasesFound = true, SCTYCasesFound = true,
            Some(CDFPayCase(NDRCCaseTotal = Some("1"), NDRCCases = Some(Seq(ndrcCaseDetails)),
              SCTYCaseTotal = Some("1"), SCTYCases = Some(Seq(sctyCaseDetails)))))

          val response: Response = Response(GetReimbursementClaimsResponse(ResponseCommon("OK",
            LocalDate.now().toString, None, None, None), Some(responseDetail)))

          when(mockTpi01Connector.retrievePostClearanceCases(any, any))
            .thenReturn(Future.successful(response))

          running(app) {
            val result = await(service.getClaims(EORI("Trader EORI"), "A"))
            result mustBe Some(ClaimsResponse(Seq(sctyClaimItem), Seq(ndrcClaimItem)))
          }
        }
      }
    }

    "calling get Specific Claims" should {
      "successfully returns NDRC CDFPayCase" in new Setup {
        val responseSpecificClaim: tpi02.Response = tpi02.Response(GetSpecificCaseResponse(
          tpi02.ResponseCommon("OK", LocalDate.now().toString, None, None, None),
          Some(tpi02.ResponseDetail("NDRC", CDFPayCaseFound = true, Some(ndrcCase), None))
        ))

        when(mockTpi02Connector.retrieveSpecificClaim(any, any))
          .thenReturn(Future.successful(responseSpecificClaim))

        running(app) {
          val result = await(service.getSpecificClaim("CDFPayService", "CDFPayCaseNumber"))
          result mustBe Some(SpecificClaimResponse("NDRC", CDFPayCaseFound = true, Some(ndrcClaimDetails.copy(caseStatus = "Closed")), None))
        }
      }

      "successfully returns SCTY CDFPayCase" in new Setup {
        val responseSpecificClaim: tpi02.Response = tpi02.Response(GetSpecificCaseResponse(
          tpi02.ResponseCommon("OK", LocalDate.now().toString, None, None, None),
          Some(tpi02.ResponseDetail("SCTY", CDFPayCaseFound = true, None, Some(sctyCase)))
        ))

        when(mockTpi02Connector.retrieveSpecificClaim(any, any))
          .thenReturn(Future.successful(responseSpecificClaim))

        running(app) {
          val result = await(service.getSpecificClaim("CDFPayService", "CDFPayCaseNumber"))
          result mustBe Some(SpecificClaimResponse("SCTY", CDFPayCaseFound = true, None, Some(sctyClaimDetails.copy(caseStatus = "Closed"))))
        }
      }

      "return None if no declarationId present" in new Setup {
        val responseSpecificClaim: tpi02.Response = tpi02.Response(GetSpecificCaseResponse(
          tpi02.ResponseCommon("OK", LocalDate.now().toString, None, None, None),
          Some(tpi02.ResponseDetail("NDRC", CDFPayCaseFound = true, Some(ndrcCase.copy(ndrcCase.NDRCDetail.copy(declarationID = None), ndrcCase.NDRCAmounts)), None))
        ))

        when(mockTpi02Connector.retrieveSpecificClaim(any, any))
          .thenReturn(Future.successful(responseSpecificClaim))

        running(app) {
          val result = await(service.getSpecificClaim("CDFPayService", "CDFPayCaseNumber"))
          result mustBe None
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
