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

package domain.tpi01

import play.api.libs.json.{Json, OFormat}

case class NDRCCaseDetails(CDFPayCaseNumber: String,
                           declarationID: Option[String],
                           claimStartDate: String,
                           closedDate: Option[String],
                           caseStatus: String,
                           declarantEORI: String,
                           importerEORI: String,
                           claimantEORI: Option[String],
                           totalCustomsClaimAmount: Option[String],
                           totalVATClaimAmount: Option[String],
                           totalExciseClaimAmount: Option[String],
                           declarantReferenceNumber: Option[String],
                           basisOfClaim: Option[String]) {

  private def transformedCaseStatus: String =
    caseStatus match {
      case "Open" => "In Progress"
      case "Open-Analysis" => "In Progress"
      case "Pending-Approval" =>  "Pending"
      case "Pending-Queried" => "Pending"
      case "Resolved-Withdrawn" => "Closed"
      case "Rejected-Failed Validation" => "Closed"
      case "Resolved-Rejected" => "Closed"
      case "Open-Rework" => "In Progress"
      case "Paused" => "In Progress"
      case "Resolved-No Reply" => "Closed"
      case "RTBH-Sent" => "Closed"
      case "Resolved-Refused" => "Closed"
      case "Pending Payment Confirmation" => "Pending"
      case "Resolved-Approved" => "Closed"
      case "Resolved-Partial Refused" => "Closed"
      case "Pending Decision Letter" => "Pending"
      case "Approved" => "Closed"
      case "Analysis-Rework" => "In Progress"
      case "Rework-Payment Details" => "In Progress"
      case "Reply To RTBH" => "Pending"
    }

  def toNDRCCaseDetails: NDRCCaseDetails = this.copy(caseStatus = transformedCaseStatus)
}

object NDRCCaseDetails {
  implicit val format: OFormat[NDRCCaseDetails] = Json.format[NDRCCaseDetails]
}