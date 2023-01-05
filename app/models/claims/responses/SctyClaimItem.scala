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

import domain.tpi01.SCTYCaseDetails
import play.api.libs.json.{Json, OFormat}

case class SctyClaimItem(
    CDFPayCaseNumber: String,
    declarationID: Option[String],
    claimStartDate: String,
    closedDate: Option[String],
    reasonForSecurity: String,
    caseStatus: String,
    caseSubStatus: Option[String],
    declarantEORI: String,
    importerEORI: String,
    claimantEORI: Option[String],
    totalCustomsClaimAmount: Option[String],
    totalVATClaimAmount: Option[String],
    declarantReferenceNumber: Option[String]
)

object SctyClaimItem {
  implicit val format: OFormat[SctyClaimItem] = Json.format[SctyClaimItem]

  def fromTpi01Response(caseDetails: SCTYCaseDetails): SctyClaimItem = {
    SctyClaimItem(
      CDFPayCaseNumber = caseDetails.CDFPayCaseNumber,
      declarationID = caseDetails.declarationID,
      claimStartDate = caseDetails.claimStartDate,
      closedDate = caseDetails.closedDate,
      reasonForSecurity = caseDetails.reasonForSecurity,
      caseStatus = SctyClaimDetails.transformedCaseStatus(caseDetails.caseStatus),
      caseSubStatus = SctyClaimDetails.caseSubStatus(caseDetails.caseStatus),
      declarantEORI = caseDetails.declarantEORI,
      importerEORI = caseDetails.importerEORI,
      claimantEORI = caseDetails.claimantEORI,
      totalCustomsClaimAmount = caseDetails.totalCustomsClaimAmount,
      totalVATClaimAmount = caseDetails.totalVATClaimAmount,
      declarantReferenceNumber = caseDetails.declarantReferenceNumber,
    )
  }
}
