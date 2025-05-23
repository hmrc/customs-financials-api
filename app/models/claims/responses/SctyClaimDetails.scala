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

package models.claims.responses

import play.api.libs.json.{Json, OFormat}

case class SctyClaimDetails(
  CDFPayCaseNumber: String,
  declarationID: Option[String],
  reasonForSecurity: String,
  procedureCode: String,
  caseStatus: String,
  caseSubStatus: Option[String],
  goods: Option[Seq[Goods]],
  declarantEORI: String,
  importerEORI: String,
  claimantEORI: Option[String],
  totalCustomsClaimAmount: Option[String],
  totalVATClaimAmount: Option[String],
  totalClaimAmount: Option[String],
  totalReimbursementAmount: Option[String],
  claimStartDate: String,
  claimantName: Option[String],
  claimantEmailAddress: Option[String],
  closedDate: Option[String],
  reimbursements: Option[Seq[Reimbursement]]
)

object SctyClaimDetails {
  implicit val format: OFormat[SctyClaimDetails] = Json.format[SctyClaimDetails]

  def caseSubStatus(caseStatus: String): Option[String] = caseStatus match {
    case "Resolved-Refund"            => Some("Resolved-Refund")
    case "Resolved-Manual BTA"        => Some("Resolved-Manual BTA")
    case "Closed-C18 Raised"          => Some("Closed-C18 Raised")
    case "Resolved-Auto BTA"          => Some("Resolved-Auto BTA")
    case "Resolved-Manual BTA/Refund" => Some("Resolved-Manual BTA/Refund")
    case "Resolved-Withdrawn"         => Some("Resolved-Withdrawn")
    case _                            => None
  }
}
