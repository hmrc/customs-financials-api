/*
 * Copyright 2021 HM Revenue & Customs
 *
 */

package domain

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

case class HistoricDocumentRequestAuditDetail(eori : EORI,
                                              documentType : FileRole,
                                              accountNumber : AccountNumber,
                                              periodStartYear : String,
                                              periodStartMonth :String,
                                              periodEndYear : String,
                                              periodEndMonth : String)

object HistoricDocumentRequestAuditDetail {
  implicit val historicDocumentRequestAuditDetailWrites: OWrites[HistoricDocumentRequestAuditDetail] = Json.writes[HistoricDocumentRequestAuditDetail]
}
