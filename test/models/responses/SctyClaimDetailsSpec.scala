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

class SctyClaimDetailsSpec extends SpecBase {

  "SctyClaimDetails" should {

    "case class model" should {
      "populated correctly" in new Setup {
        val result: SctyClaimDetails =
          SctyClaimDetails("", Option(""), "", "", "", Option(""),
          testGoods, "", "", Option(""), Option(""), Option(""),
          Option(""), Option(""), "", Option(""), Option(""), Option(""), testReimbursement)

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

  trait Setup {
    val testRefund: Option[String] = Some("Resolved-Refund")
    val testManual: Option[String] = Some("Resolved-Manual BTA")
    val testClosedC18: Option[String]  = Some("Closed-C18 Raised")
    val testResolvedAuto: Option[String]  = Some("Resolved-Auto BTA")
    val testResolvedManual: Option[String]  = Some("Resolved-Manual BTA/Refund")
    val testResolvedWithdrawn: Option[String]  = Some("Resolved-Withdrawn")

    val testInProgress = "In Progress"
    val testPending = "Pending"
    val testClosed = "Closed"

    val testGoods: Option[Seq[Goods]] =
      Option(Seq(
        Goods("", Option("")),
        Goods("", Option(""))))

    val testReimbursement: Option[Seq[Reimbursement]] =
      Option(Seq(
        Reimbursement("", "", "", ""),
        Reimbursement("", "", "", "")))

    val compareResult: SctyClaimDetails = SctyClaimDetails("", Option(""), "", "", "", Option(""),
      testGoods, "", "", Option(""), Option(""), Option(""),
      Option(""), Option(""), "", Option(""), Option(""), Option(""), testReimbursement)
  }
}
