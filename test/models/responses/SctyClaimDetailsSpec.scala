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
        val result = SctyClaimDetails("", Option(""), "", "", "", Option(""),
          testGoods, "", "", Option(""), Option(""), Option(""),
          Option(""), Option(""), "", Option(""), Option(""), Option(""), testReimbursement)

        result mustBe compareResult
      }
    }

    "caseSubStatus" should {
      "Resolved-Refund" in new Setup {
        val result = SctyClaimDetails.caseSubStatus("Resolved-Refund")
        result mustBe testRefund
      }

      "Resolved-Manual BTA" in new Setup {
        val result = SctyClaimDetails.caseSubStatus("Resolved-Manual BTA")
        result mustBe testManual
      }

      "Closed-C18 Raised" in new Setup {
        val result = SctyClaimDetails.caseSubStatus("Closed-C18 Raised")
        result mustBe testClosedC18
      }

      "Resolved-Auto BTA" in new Setup {
        val result = SctyClaimDetails.caseSubStatus("Resolved-Auto BTA")
        result mustBe testResolvedAuto
      }

      "Resolved-Manual BTA/Refund" in new Setup {
        val result = SctyClaimDetails.caseSubStatus("Resolved-Manual BTA/Refund")
        result mustBe testResolvedManual
      }

      "Resolved-Withdrawn" in new Setup {
        val result = SctyClaimDetails.caseSubStatus("Resolved-Withdrawn")
        result mustBe testResolvedWithdrawn
      }

      "Other" in new Setup {
        val result = SctyClaimDetails.caseSubStatus("Other")
        result mustBe None
      }
    }

    "transformedCaseStatus" should {
      "Open convert to in progress" in new Setup {
        val result = SctyClaimDetails.transformedCaseStatus("Open")
        result mustBe testInProgress
      }

      "Pending-Approval convert to pending" in new Setup {
        val result = SctyClaimDetails.transformedCaseStatus("Pending-Approval")
        result mustBe testPending
      }

      "Pending-Payment convert to pending" in new Setup {
        val result = SctyClaimDetails.transformedCaseStatus("Pending-Payment")
        result mustBe testPending
      }

      "Partial Refund convert to pending" in new Setup {
        val result = SctyClaimDetails.transformedCaseStatus("Partial Refund")
        result mustBe testPending
      }

      "Resolved-Refund convert to closed" in new Setup {
        val result = SctyClaimDetails.transformedCaseStatus("Resolved-Refund")
        result mustBe testClosed
      }

      "Pending-Query convert to pending" in new Setup {
        val result = SctyClaimDetails.transformedCaseStatus("Pending-Query")
        result mustBe testPending
      }

      "Resolved-Manual BTA convert to closed" in new Setup {
        val result = SctyClaimDetails.transformedCaseStatus("Resolved-Manual BTA")
        result mustBe testClosed
      }

      "Pending-C18 convert to pending" in new Setup {
        val result = SctyClaimDetails.transformedCaseStatus("Pending-C18")
        result mustBe testPending
      }

      "Closed-C18 Raised convert to closed" in new Setup {
        val result = SctyClaimDetails.transformedCaseStatus("Closed-C18 Raised")
        result mustBe testClosed
      }

      "RTBH Letter Initiated convert to pending" in new Setup {
        val result = SctyClaimDetails.transformedCaseStatus("RTBH Letter Initiated")
        result mustBe testPending
      }

      "Awaiting RTBH Letter Response convert to pending" in new Setup {
        val result = SctyClaimDetails.transformedCaseStatus("Awaiting RTBH Letter Response")
        result mustBe testPending
      }

      "Reminder Letter Initiated convert to pending" in new Setup {
        val result = SctyClaimDetails.transformedCaseStatus("Reminder Letter Initiated")
        result mustBe testPending
      }

      "Awaiting Reminder Letter Response convert to pending" in new Setup {
        val result = SctyClaimDetails.transformedCaseStatus("Awaiting Reminder Letter Response")
        result mustBe testPending
      }

      "Decision Letter Initiated convert to pending" in new Setup {
        val result = SctyClaimDetails.transformedCaseStatus("Decision Letter Initiated")
        result mustBe testPending
      }

      "Partial BTA convert to pending" in new Setup {
        val result = SctyClaimDetails.transformedCaseStatus("Partial BTA")
        result mustBe testPending
      }

      "Partial BTA/Refund convert to pending" in new Setup {
        val result = SctyClaimDetails.transformedCaseStatus("Partial BTA/Refund")
        result mustBe testPending
      }

      "Resolved-Auto BTA convert to closed" in new Setup {
        val result = SctyClaimDetails.transformedCaseStatus("Resolved-Auto BTA")
        result mustBe testClosed
      }

      "Resolved-Manual BTA/Refund convert to closed" in new Setup {
        val result = SctyClaimDetails.transformedCaseStatus("Resolved-Manual BTA/Refund")
        result mustBe testClosed
      }

      "Open-Extension Grantedconvert to inProgress" in new Setup {
        val result = SctyClaimDetails.transformedCaseStatus("Open-Extension Granted")
        result mustBe testInProgress
      }

      "Resolved-Withdrawn convert to closed" in new Setup {
        val result = SctyClaimDetails.transformedCaseStatus("Resolved-Withdrawn")
        result mustBe testClosed
      }
    }
  }

  trait Setup {

    val testRefund = Some("Resolved-Refund")
    val testManual = Some("Resolved-Manual BTA")
    val testClosedC18 = Some("Closed-C18 Raised")
    val testResolvedAuto = Some("Resolved-Auto BTA")
    val testResolvedManual = Some("Resolved-Manual BTA/Refund")
    val testResolvedWithdrawn = Some("Resolved-Withdrawn")

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

    val compareResult = SctyClaimDetails("", Option(""), "", "", "", Option(""),
      testGoods, "", "", Option(""), Option(""), Option(""),
      Option(""), Option(""), "", Option(""), Option(""), Option(""), testReimbursement)
  }
}
