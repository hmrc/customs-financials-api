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

package domain.tpi02.ndrc

import domain.tpi02.Reimbursement
import play.api.libs.json.{Json, OFormat}

case class NDRCDetail(
                       CDFPayCaseNumber: String,
                       declarationID: Option[String],
                       claimType: String, //C285 or C&E1179
                       caseType: String, //if (C285) Individual, Bulk, CMA, C18 || if (C&E1179) Individual, Bulk, CMA
                       caseStatus: String,
                       descOfGoods: Option[String],
                       descOfRejectedGoods: Option[String],
                       declarantEORI: String,
                       importerEORI: String,
                       claimantEORI: Option[String],
                       basisOfClaim: Option[String],
                       claimStartDate: String,
                       claimantName: Option[String],
                       claimantEmailAddress: Option[String],
                       closedDate: Option[String],
                       MRNDetails: Option[Seq[ProcedureDetail]],
                       entryDetails: Option[Seq[EntryDetail]],
                       reimbursement: Option[Seq[Reimbursement]]
                     )

object NDRCDetail {
  implicit val format: OFormat[NDRCDetail] = Json.format[NDRCDetail]
}
