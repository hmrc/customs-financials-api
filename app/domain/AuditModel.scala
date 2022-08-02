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

package domain

import domain.acc40.{CashAccount, DutyDefermentAccount, GeneralGuaranteeAccount}
import models.dec64.UploadedFile
import models.{AccountNumber, AccountType, EORI, FileRole}
import play.api.libs.json.{JsValue, Json, OWrites}


case class AuditModel(transactionName: String, detail: JsValue, auditType: String)

case class GrantAuthorityRequestAuditDetail(ownerEORI: EORI,
                                            authorisedEORI: EORI,
                                            action: String,
                                            accounts: Seq[AccountAuditDetail],
                                            startDate: String,
                                            endDate: String,
                                            seeBalance: Boolean,
                                            authoriserName: String,
                                            authoriserJobRole: String)

case class EditAuthorityRequestAuditDetail(ownerEORI: EORI,
                                           authorisedEORI: EORI,
                                           action: String,
                                           accountType: AccountType,
                                           accountNumber: AccountNumber,
                                           startDate: String,
                                           endDate: String,
                                           seeBalance: Boolean,
                                           authoriserName: String,
                                           authoriserJobRole: String)

case class AccountAuditDetail(accountType: AccountType,
                              accountNumber: AccountNumber)

object GrantAuthorityRequestAuditDetail {
  implicit val AccountAuditDetail: OWrites[AccountAuditDetail] = Json.writes[AccountAuditDetail]
  implicit val GrantAuthorityRequestAuditDetailWrites: OWrites[GrantAuthorityRequestAuditDetail] = Json.writes[GrantAuthorityRequestAuditDetail]
}

object EditAuthorityRequestAuditDetail {
  implicit val EditAuthorityRequestAuditDetailWrites: OWrites[EditAuthorityRequestAuditDetail] = Json.writes[EditAuthorityRequestAuditDetail]
}

case class RevokeAuthorityRequestAuditDetail(ownerEORI: EORI,
                                             authorisedEORI: EORI,
                                             action: String,
                                             accountType: AccountType,
                                             accountNumber: AccountNumber,
                                             authoriserName: String,
                                             authoriserJobRole: String)

object RevokeAuthorityRequestAuditDetail {
  implicit val RevokeAuthorityRequestAuditDetailWrites: OWrites[RevokeAuthorityRequestAuditDetail] = Json.writes[RevokeAuthorityRequestAuditDetail]
}

case class HistoricDocumentRequestAuditDetail(eori: EORI,
                                              documentType: FileRole,
                                              accountNumber: AccountNumber,
                                              periodStartYear: String,
                                              periodStartMonth: String,
                                              periodEndYear: String,
                                              periodEndMonth: String)

object HistoricDocumentRequestAuditDetail {
  implicit val historicDocumentRequestAuditDetailWrites: OWrites[HistoricDocumentRequestAuditDetail] = Json.writes[HistoricDocumentRequestAuditDetail]
}

case class FileUploadRequestAuditDetail(eori: String,
                                        caseNumber: String,
                                        applicationName: String,
                                        properties: Properties)

object FileUploadRequestAuditDetail {
  implicit val fileUploadRequestAuditDetailWrites: OWrites[FileUploadRequestAuditDetail] = Json.writes[FileUploadRequestAuditDetail]
}

case class RequestAuthAuditDetail(requestingEori: String,
                                  seachType: String,
                                  searchID: String,
                                  numberOfAuthorities: Option[String],
                                  companyName: String,
                                  dutyDefermentAccounts: Option[Seq[DutyDefermentAccount]],
                                  generalGuaranteeAccounts: Option[Seq[GeneralGuaranteeAccount]],
                                  cdsCashAccounts: Option[Seq[CashAccount]]
)

object RequestAuthAuditDetail {
  implicit val requestAuthAuditDetailsWrites: OWrites[RequestAuthAuditDetail] = Json.writes[RequestAuthAuditDetail]
}

case class RequestAuthCSVAuditDetail(
  requestingEori: String,
  requestAcceptedDate: String
)

object RequestAuthCSVAuditDetail {
  implicit val requestAuthCSVAuditDetailsWrites: OWrites[RequestAuthCSVAuditDetail] =
    Json.writes[RequestAuthCSVAuditDetail]
}

case class RequestDisplayStandingAuthCSVAuditDetail(
  Eori: String,
  isHistoric: Boolean,
  fileName: String,
  fileRole: String,
  fileType: String
)

object RequestDisplayStandingAuthCSVAuditDetail {
  implicit val requestStandingAuthCSVAuditDetailsWrites: OWrites[RequestDisplayStandingAuthCSVAuditDetail] =
    Json.writes[RequestDisplayStandingAuthCSVAuditDetail]
}

case class Properties(uploadedFiles: Seq[UploadedFile])

object Properties {
  implicit val PropertiesWrites: OWrites[Properties] = Json.writes[Properties]
}