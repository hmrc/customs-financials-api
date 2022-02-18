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

case class SCTYCaseDetails(CDFPayCaseNumber: String,
                           declarationID: Option[String],
                           claimStartDate: String,
                           closedDate: Option[String],
                           reasonForSecurity: String,
                           caseStatus: String,
                           declarantEORI: String,
                           importerEORI: String,
                           claimantEORI: Option[String],
                           totalCustomsClaimAmount: Option[String],
                           totalVATClaimAmount: Option[String],
                           declarantReferenceNumber: Option[String]) {

  private def transformedCaseStatus: String =
    caseStatus match {
      case "Open" => "In Progress"
      case "Pending-Approval" =>  "Pending"
      case "Pending-Payment" =>  "Pending"
      case "Partial Refund" =>  "Pending"
      case "Resolved-Refund" =>  "Closed"
      case "Pending-Query" => "Pending"
      case "Resolved-Manual BTA" => "Closed"
      case "Pending-C18" => "Pending"
      case "Closed-C18 Raised" => "Closed"
      case "RTBH Letter Initiated" => "Pending"
      case "Awaiting RTBH Letter Response" => "Pending"
      case "Reminder Letter Initiated" => "Pending"
      case "Reminder Letter Initiated" => "Pending"
      case "Awaiting Reminder Letter Response" => "Pending"
      case "Decision Letter Initiated" => "Pending"
      case "Partial BTA" => "Pending"
      case "Partial BTA/Refund" => "Pending"
      case "Resolved-Auto BTA" => "Closed"
      case "Resolved-Manual BTA/Refund" => "Closed"
      case "Open-Extension Granted" => "In Progress" //Check these
    }

  def toSCTYCaseDetails: SCTYCaseDetails = this.copy(caseStatus = transformedCaseStatus)
}

object SCTYCaseDetails {
  implicit val format: OFormat[SCTYCaseDetails] = Json.format[SCTYCaseDetails]
}

