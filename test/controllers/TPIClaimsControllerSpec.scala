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

package controllers

import domain._
import domain.tpi01._
import models.EORI
import models.claims.responses.{ClaimsResponse, NdrcClaimItem, SctyClaimItem, SpecificClaimResponse}
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json
import play.api.mvc.AnyContentAsJson
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.api.{Application, inject}
import services.TPIClaimsService
import uk.gov.hmrc.http.NotFoundException
import utils.SpecBase

import scala.concurrent.Future

class TPIClaimsControllerSpec extends SpecBase {

  "getReimbursementClaims" should {
    "return 200 status" in new Setup {
      val ndrcCaseDetails: NdrcClaimItem = NdrcClaimItem(CDFPayCaseNumber = "NDRC-2109", declarationID = Some("21LLLLLLLLLLLLLLL9"),
        claimStartDate = "20211120", closedDate = Some("00000000"), caseStatus = "In Progress", caseSubStatus = None, declarantEORI = "GB744638982000",
        importerEORI = "GB744638982000", claimantEORI = Some("GB744638982000"), totalCustomsClaimAmount = Some("3000.20"),
        totalVATClaimAmount = Some("784.66"), totalExciseClaimAmount = Some("1200.00"), declarantReferenceNumber = Some("KWMREF1"),
        basisOfClaim = Some("Duplicate Entry"))

      val sctyCaseDetails: SctyClaimItem = SctyClaimItem(CDFPayCaseNumber = "SEC-2109", declarationID = Some("21LLLLLLLLLL12345"),
        claimStartDate = "20210320", closedDate = Some("00000000"), reasonForSecurity = "ACS", caseStatus = "In Progress", caseSubStatus = None,
        declarantEORI = "GB744638982000", importerEORI = "GB744638982000", claimantEORI = Some("GB744638982000"),
        totalCustomsClaimAmount = Some("12000.56"), totalVATClaimAmount = Some("3412.01"), declarantReferenceNumber = Some("broomer007"))

      val responseDetail: ClaimsResponse = ClaimsResponse(sctyClaims = Seq(sctyCaseDetails), ndrcClaims = Seq(ndrcCaseDetails))


      when(mockTPIClaimsService.getClaims(any, any))
        .thenReturn(Future.successful(Some(responseDetail)))

      running(app) {
        val result = route(app, request).value
        status(result) mustBe OK
        contentAsJson(result) mustBe Json.obj("claims" -> Json.obj("sctyClaims" -> Seq(sctyResponse), "ndrcClaims" -> Seq(ndrcResponse)))

      }
    }


    "return 200 with no associated data found" in new Setup {

      val responseDetail: ClaimsResponse = ClaimsResponse(sctyClaims = Nil, ndrcClaims = Nil)

      when(mockTPIClaimsService.getClaims(any, any))
        .thenReturn(Future.successful(Some(responseDetail)))

      running(app) {
        val result = route(app, request).value
        status(result) mustBe 200
        contentAsJson(result) mustBe Json.obj("claims" -> Json.obj("sctyClaims" -> Seq.empty[SCTYCaseDetails], "ndrcClaims" -> Seq.empty[NDRCCaseDetails]))
      }
    }

    "return 500 for no response" in new Setup {
      when(mockTPIClaimsService.getClaims(any, any))
        .thenReturn(Future.successful(None))

      running(app) {
        val result = route(app, request).value
        status(result) mustBe INTERNAL_SERVER_ERROR
      }
    }

    "return 503 for any error" in new Setup {
      when(mockTPIClaimsService.getClaims(any, any))
        .thenReturn(Future.failed(new NotFoundException("ShouldNotReturnThis")))

      running(app) {
        val result = route(app, request).value
        status(result) mustBe SERVICE_UNAVAILABLE
      }
    }
  }

  "getSpecificClaim" should {
    "return 200 with cdfPayCase when claim found " in new Setup {

      val response =
        SpecificClaimResponse("NDRC", CDFPayCaseFound = true, Some(ndrcClaimDetails), None)

      when(mockTPIClaimsService.getSpecificClaim(any, any))
        .thenReturn(Future.successful(Some(response)))

      running(app) {
        val result = route(app, requestSpecificClaim).value
        status(result) mustBe OK
        contentAsJson(result) mustBe Json.toJson(response)
      }
    }

    "return 204 status when no claims found" in new Setup {
      when(mockTPIClaimsService.getSpecificClaim(any, any))
        .thenReturn(Future.successful(None))

      running(app) {
        val result = route(app, requestSpecificClaim).value
        status(result) mustBe NO_CONTENT
      }
    }

    "return 503 for any error" in new Setup {
      when(mockTPIClaimsService.getSpecificClaim(any, any))
        .thenReturn(Future.failed(new NotFoundException("ShouldNotReturnThis")))

      running(app) {
        val result = route(app, requestSpecificClaim).value
        status(result) mustBe SERVICE_UNAVAILABLE
      }
    }
  }

  trait Setup {
    val eori: EORI = EORI("testEORI")
    val request: FakeRequest[AnyContentAsJson] = FakeRequest("POST", controllers.routes.TPIClaimsController.getReimbursementClaims().url)
      .withJsonBody(Json.parse("""{"eori":"some eori", "appType":"A"}"""))

    val requestSpecificClaim: FakeRequest[AnyContentAsJson] = FakeRequest("POST", controllers.routes.TPIClaimsController.getSpecificClaim().url)
      .withJsonBody(Json.parse("""{"cdfPayService":"mtdp", "cdfPayCaseNumber":"abc"}"""))

    val mockTPIClaimsService: TPIClaimsService = mock[TPIClaimsService]

    val app: Application = GuiceApplicationBuilder().overrides(
      inject.bind[TPIClaimsService].toInstance(mockTPIClaimsService)
    ).configure(
      "microservice.metrics.enabled" -> false,
      "metrics.enabled" -> false,
      "auditing.enabled" -> false
    ).build()

    val ndrcResponse: NdrcClaimItem = NdrcClaimItem(CDFPayCaseNumber = "NDRC-2109", declarationID = Some("21LLLLLLLLLLLLLLL9"),
      claimStartDate = "20211120", closedDate = Some("00000000"), caseStatus = "In Progress", caseSubStatus = None, declarantEORI = "GB744638982000",
      importerEORI = "GB744638982000", claimantEORI = Some("GB744638982000"), totalCustomsClaimAmount = Some("3000.20"),
      totalVATClaimAmount = Some("784.66"), totalExciseClaimAmount = Some("1200.00"), declarantReferenceNumber = Some("KWMREF1"),
      basisOfClaim = Some("Duplicate Entry"))

    val sctyResponse: SctyClaimItem = SctyClaimItem(CDFPayCaseNumber = "SEC-2109", declarationID = Some("21LLLLLLLLLL12345"),
      claimStartDate = "20210320", closedDate = Some("00000000"), reasonForSecurity = "ACS", caseStatus = "In Progress",  caseSubStatus = None,
      declarantEORI = "GB744638982000", importerEORI = "GB744638982000", claimantEORI = Some("GB744638982000"),
      totalCustomsClaimAmount = Some("12000.56"), totalVATClaimAmount = Some("3412.01"), declarantReferenceNumber = Some("broomer007"))
  }
}
