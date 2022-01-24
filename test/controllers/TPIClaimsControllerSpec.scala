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

import java.time.LocalDate

import domain.tpi01.{CDFPayCase, GetReimbursementClaimsResponse, ResponseCommon, ResponseDetail}
import domain.tpi02.{GetSpecificClaimResponse, Reimbursement, ReturnParameter}
import models.EORI
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
      val cdfPayCases: CDFPayCase = CDFPayCase("4374422408", "NDRC", "Resolved-Completed", "GB138153003838312", "GB138153003838312",
        Some("GB138153003838312"), Some("10.00"), Some("10.00"))
      val responseDetail: ResponseDetail = ResponseDetail(cdfPayClaimsFound = true, Some(Array(cdfPayCases)))

      protected val response: GetReimbursementClaimsResponse = GetReimbursementClaimsResponse(
        ResponseCommon("OK", LocalDate.now().toString, None, None, None),
        Some(responseDetail))

      when(mockTPIClaimsService.getClaims(any))
        .thenReturn(Future.successful(response))

      running(app) {
        val result = route(app, request).value
        status(result) mustBe OK
        contentAsJson(result) mustBe Json.toJson(responseDetail)

      }
    }

    "return 204 status for no claims found" in new Setup {
      val responseDetail: ResponseDetail = ResponseDetail(cdfPayClaimsFound = false, None)

      protected val response: GetReimbursementClaimsResponse = GetReimbursementClaimsResponse(
        ResponseCommon("OK", LocalDate.now().toString, None, None, None),
        Some(responseDetail))

      when(mockTPIClaimsService.getClaims(any))
        .thenReturn(Future.successful(response))

      running(app) {
        val result = route(app, request).value
        status(result) mustBe 204
      }
    }

    "return 503 for any error" in new Setup {
      when(mockTPIClaimsService.getClaims(any))
        .thenReturn(Future.failed(new NotFoundException("ShouldNotReturnThis")))

      running(app) {
        val result = route(app, request).value
        status(result) mustBe SERVICE_UNAVAILABLE
      }
    }
  }

  "getSpecificClaim" should {
    "return 200 status" in new Setup {

      val reimbursement: Reimbursement = Reimbursement("date", "10.00", "10.00")
      val cdfPayCase: domain.tpi02.CDFPayCase = domain.tpi02.CDFPayCase("Resolved-Completed", "4374422408", "GB138153003838312", "GB138153003838312",
        Some("GB138153003838312"), Some("10.00"), Some("10.00"), Some("10.00"), "10.00", "10.00", Some("10.00"), Some(reimbursement))
      val responseDetail: domain.tpi02.ResponseDetail = domain.tpi02.ResponseDetail("MDTP", Some(cdfPayCase))

      protected val specificClaimResponse: GetSpecificClaimResponse = GetSpecificClaimResponse(
        domain.tpi02.ResponseCommon("OK", LocalDate.now().toString, None, None, None), Some(responseDetail))

      when(mockTPIClaimsService.getSpecificClaim(any, any))
        .thenReturn(Future.successful(specificClaimResponse))

      running(app) {
        val result = route(app, requestSpecificClaim).value
        status(result) mustBe OK
        contentAsJson(result) mustBe Json.toJson(responseDetail)
      }
    }

    "return 204 status when no claims found" in new Setup {

      protected val specificClaimResponse: GetSpecificClaimResponse = GetSpecificClaimResponse(
        domain.tpi02.ResponseCommon("OK", LocalDate.now().toString, Some("1682aaa9-d212-46ba-852e-43c2d01faf21"), Some("Invalid CDFPayCaseNumber"),  Some(List(ReturnParameter("POSITION", "FAIL")))), None)

      when(mockTPIClaimsService.getSpecificClaim(any, any))
        .thenReturn(Future.successful(specificClaimResponse))

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

    val traderEORI: EORI = EORI("testEORI")
    val request: FakeRequest[AnyContentAsJson] = FakeRequest("POST", controllers.routes.TPIClaimsController.getReimbursementClaims().url)
      .withJsonBody(Json.parse("""{"eori":"some eori"}"""))

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
  }
}
