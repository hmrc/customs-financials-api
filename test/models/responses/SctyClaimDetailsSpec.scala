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

package models.responses

import models.claims.responses.{Goods, Reimbursement, SctyClaimDetails}
import utils.SpecBase
import utils.Utils.emptyString
import play.api.libs.json.{JsResultException, JsSuccess, Json}

class SctyClaimDetailsSpec extends SpecBase {

  "SctyClaimDetails" should {

    "case class model" should {
      "populated correctly" in new Setup {
        val result: SctyClaimDetails =
          SctyClaimDetails(
            emptyString,
            Option(emptyString),
            emptyString,
            emptyString,
            emptyString,
            Option(emptyString),
            testGoods,
            emptyString,
            emptyString,
            Option(emptyString),
            Option(emptyString),
            Option(emptyString),
            Option(emptyString),
            Option(emptyString),
            emptyString,
            Option(emptyString),
            Option(emptyString),
            Option(emptyString),
            testReimbursement
          )

        result mustBe compareResult
      }
    }

    "caseSubStatus" should {
      "Resolved-Refund" in new Setup {
        val result: Option[String] = SctyClaimDetails.caseSubStatus("Resolved-Refund")
        result mustBe testRefund
      }

      "Resolved-Manual BTA" in new Setup {
        val result: Option[String] = SctyClaimDetails.caseSubStatus("Resolved-Manual BTA")
        result mustBe testManual
      }

      "Closed-C18 Raised" in new Setup {
        val result: Option[String] = SctyClaimDetails.caseSubStatus("Closed-C18 Raised")
        result mustBe testClosedC18
      }

      "Resolved-Auto BTA" in new Setup {
        val result: Option[String] = SctyClaimDetails.caseSubStatus("Resolved-Auto BTA")
        result mustBe testResolvedAuto
      }

      "Resolved-Manual BTA/Refund" in new Setup {
        val result: Option[String] = SctyClaimDetails.caseSubStatus("Resolved-Manual BTA/Refund")
        result mustBe testResolvedManual
      }

      "Resolved-Withdrawn" in new Setup {
        val result: Option[String] = SctyClaimDetails.caseSubStatus("Resolved-Withdrawn")
        result mustBe testResolvedWithdrawn
      }

      "Other" in new Setup {
        val result: Option[String] = SctyClaimDetails.caseSubStatus("Other")
        result mustBe None
      }
    }
  }

  "SctyClaimDetails.format" should {

    "generate correct output for Json Reads" in new Setup {
      import SctyClaimDetails.format

      Json.fromJson(Json.parse(claimsDetailsJsString)) mustBe JsSuccess(compareResult)
    }

    "generate correct output for Json Writes" in new Setup {
      Json.toJson(compareResult) mustBe Json.parse(claimsDetailsJsString)
    }

    "throw exception for invalid Json" in {
      val invalidJson = "{ \"CDFPayCaseNumber\": \"12346\", \"claimantEORI123\": \"GB1234567\" }"

      intercept[JsResultException] {
        Json.parse(invalidJson).as[SctyClaimDetails]
      }
    }
  }

  trait Setup {
    val testRefund: Option[String]            = Some("Resolved-Refund")
    val testManual: Option[String]            = Some("Resolved-Manual BTA")
    val testClosedC18: Option[String]         = Some("Closed-C18 Raised")
    val testResolvedAuto: Option[String]      = Some("Resolved-Auto BTA")
    val testResolvedManual: Option[String]    = Some("Resolved-Manual BTA/Refund")
    val testResolvedWithdrawn: Option[String] = Some("Resolved-Withdrawn")

    val testInProgress = "In Progress"
    val testPending    = "Pending"
    val testClosed     = "Closed"

    val testGoods: Option[Seq[Goods]] =
      Option(Seq(Goods(emptyString, Option(emptyString)), Goods(emptyString, Option(emptyString))))

    val testReimbursement: Option[Seq[Reimbursement]] =
      Option(
        Seq(
          Reimbursement(emptyString, emptyString, emptyString, emptyString),
          Reimbursement(emptyString, emptyString, emptyString, emptyString)
        )
      )

    val compareResult: SctyClaimDetails =
      SctyClaimDetails(
        emptyString,
        Option(emptyString),
        emptyString,
        emptyString,
        emptyString,
        Option(emptyString),
        testGoods,
        emptyString,
        emptyString,
        Option(emptyString),
        Option(emptyString),
        Option(emptyString),
        Option(emptyString),
        Option(emptyString),
        emptyString,
        Option(emptyString),
        Option(emptyString),
        Option(emptyString),
        testReimbursement
      )

    val claimsDetailsJsString: String =
      """{"CDFPayCaseNumber":"",
        |"declarationID":"",
        |"reasonForSecurity":"",
        |"procedureCode":"",
        |"caseStatus":"",
        |"caseSubStatus":"",
        |"goods":[{"itemNumber":"","goodsDescription":""},{"itemNumber":"","goodsDescription":""}],
        |"declarantEORI":"",
        |"importerEORI":"",
        |"claimantEORI":"",
        |"totalCustomsClaimAmount":"",
        |"totalVATClaimAmount":"",
        |"totalClaimAmount":"",
        |"totalReimbursementAmount":"",
        |"claimStartDate":"",
        |"claimantName":"",
        |"claimantEmailAddress":"",
        |"closedDate":"",
        |"reimbursements":[
        |{"reimbursementDate":"","reimbursementAmount":"","taxType":"","reimbursementMethod":""},
        |{"reimbursementDate":"","reimbursementAmount":"","taxType":"","reimbursementMethod":""}
        |]
        |}""".stripMargin
  }
}
