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

package services

import config.AppConfig
import domain._
import models.requests.HistoricDocumentRequest
import models.requests.manageAuthorities.{Accounts, GrantAuthorityRequest, RevokeAccountType, RevokeAuthorityRequest}
import models.{AccountNumber, AccountType, EORI, FileRole}
import play.api.http.HeaderNames
import play.api.libs.json.{JsValue, Json, Writes}
import play.api.{Logger, LoggerLike}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.audit.AuditExtensions
import uk.gov.hmrc.play.audit.http.connector.AuditResult.{Disabled, Failure, Success}
import uk.gov.hmrc.play.audit.http.connector.{AuditConnector, AuditResult}
import uk.gov.hmrc.play.audit.model.{DataEvent, ExtendedDataEvent}
import javax.inject.{Inject, Singleton}
import models.dec64.{FileUploadDetail, FileUploadRequest}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class AuditingService @Inject()(appConfig: AppConfig,
                                auditConnector: AuditConnector)(implicit executionContext: ExecutionContext) {

  val log: LoggerLike = Logger(this.getClass)

  val MANAGE_AUTHORITY_AUDIT_TYPE = "ManageAuthority"
  val UPDATE_AUTHORITY_AUDIT_TYPE = "UpdateAuthority"
  val EDIT_AUTHORITY_ACTION = "Update Authority"
  val GRANT_AUTHORITY_ACTION = "Grant Authority"
  val REVOKE_AUTHORITY_ACTION = "Revoke Authority"
  val HISTORIC_STATEMENT_REQUEST_AUDIT_TYPE = "RequestHistoricStatement"
  val HISTORIC_STATEMENT_REQUEST_TRANSACTION_NAME = "Request historic statements"
  val FILE_UPLOAD_REQUEST_TRANSACTION_NAME = "View and amend file upload"
  val FILE_UPLOAD_REQUEST_AUDIT_TYPE = "ViewAmendFileUpload"

  implicit val dataEventWrites: Writes[DataEvent] = Json.writes[DataEvent]
  val referrer: HeaderCarrier => String = _.headers(Seq(HeaderNames.REFERER)).headOption.fold("-")(_._2)

  def auditEditAuthority(grantAuthorityRequest: GrantAuthorityRequest, eori: EORI)(implicit hc: HeaderCarrier): Future[AuditResult] = {
    val accountDetails = convertToAuditRequest(grantAuthorityRequest.accounts).headOption.getOrElse(AccountAuditDetail(AccountType("-"), AccountNumber("-")))

    val auditJson: JsValue = Json.toJson(EditAuthorityRequestAuditDetail(eori,
      grantAuthorityRequest.authority.authorisedEori,
      EDIT_AUTHORITY_ACTION,
      accountDetails.accountType,
      accountDetails.accountNumber,
      grantAuthorityRequest.authority.authorisedFromDate,
      grantAuthorityRequest.authority.authorisedToDate.getOrElse(""),
      grantAuthorityRequest.authority.viewBalance,
      grantAuthorityRequest.authorisedUser.userName,
      grantAuthorityRequest.authorisedUser.userRole))
    audit(AuditModel(EDIT_AUTHORITY_ACTION, auditJson, UPDATE_AUTHORITY_AUDIT_TYPE))
  }

  def auditGrantAuthority(grantAuthorityRequest: GrantAuthorityRequest, eori: EORI)(implicit hc: HeaderCarrier): Future[AuditResult] = {
    val auditJson = Json.toJson(GrantAuthorityRequestAuditDetail(eori,
      grantAuthorityRequest.authority.authorisedEori,
      GRANT_AUTHORITY_ACTION,
      convertToAuditRequest(grantAuthorityRequest.accounts),
      grantAuthorityRequest.authority.authorisedFromDate,
      grantAuthorityRequest.authority.authorisedToDate.getOrElse(""),
      grantAuthorityRequest.authority.viewBalance,
      grantAuthorityRequest.authorisedUser.userName,
      grantAuthorityRequest.authorisedUser.userRole))

    audit(AuditModel(GRANT_AUTHORITY_ACTION, auditJson, MANAGE_AUTHORITY_AUDIT_TYPE))
  }

  def auditRevokeAuthority(revokeAuthorityRequest: RevokeAuthorityRequest, eori: EORI)(implicit hc: HeaderCarrier): Future[AuditResult] = {
    val auditJson = Json.toJson(RevokeAuthorityRequestAuditDetail(eori,
      revokeAuthorityRequest.authorisedEori,
      REVOKE_AUTHORITY_ACTION,
      RevokeAccountType.toAuditLabel(revokeAuthorityRequest.accountType),
      revokeAuthorityRequest.accountNumber,
      revokeAuthorityRequest.authorisedUser.userName,
      revokeAuthorityRequest.authorisedUser.userRole))

    audit(AuditModel(REVOKE_AUTHORITY_ACTION, auditJson, MANAGE_AUTHORITY_AUDIT_TYPE))
  }

  def auditHistoricStatementRequest(historicDocumentRequest: HistoricDocumentRequest)(implicit hc: HeaderCarrier): Future[AuditResult] = {
    import domain.HistoricDocumentRequestAuditDetail._
    val auditJson = Json.toJson(HistoricDocumentRequestAuditDetail(
      historicDocumentRequest.eori,
      historicDocumentRequest.documentType.value match {
        case "C79Certificate" => FileRole("C79Statement")
        case "SecurityStatement" => FileRole("SecuritiesStatement")
        case statementName => FileRole(statementName)
      },
      AccountNumber(historicDocumentRequest.dan),
      historicDocumentRequest.periodStartYear.toString,
      "%02d".format(historicDocumentRequest.periodStartMonth),
      historicDocumentRequest.periodEndYear.toString,
      "%02d".format(historicDocumentRequest.periodEndMonth)))

    audit(AuditModel(HISTORIC_STATEMENT_REQUEST_TRANSACTION_NAME, auditJson, HISTORIC_STATEMENT_REQUEST_AUDIT_TYPE))
  }

  def auditFileUploadRequest(fileUploadRequest: FileUploadRequest)(implicit hc: HeaderCarrier): Future[AuditResult] = {
    val auditJson = Json.toJson(FileUploadRequestAuditDetail(
      fileUploadRequest.id,
      fileUploadRequest.eori.value,
      fileUploadRequest.caseNumber,
      fileUploadRequest.applicationName,
      Properties(fileUploadRequest.uploadedFiles)))

    audit(AuditModel(FILE_UPLOAD_REQUEST_TRANSACTION_NAME, auditJson, FILE_UPLOAD_REQUEST_AUDIT_TYPE))
  }

  private def audit(auditModel: AuditModel)(implicit hc: HeaderCarrier): Future[AuditResult] = {
    val dataEvent = ExtendedDataEvent(
      auditSource = appConfig.appName,
      auditType = auditModel.auditType,
      tags = AuditExtensions.auditHeaderCarrier(hc).toAuditTags(auditModel.transactionName, referrer(hc)),
      detail = auditModel.detail
    )
    log.debug(s"Splunk Audit Event:\n$dataEvent\n")
    auditConnector.sendExtendedEvent(dataEvent)
      .map { auditResult =>
        logAuditResult(auditResult)
        auditResult
      }
  }

  private def convertToAuditRequest(accounts: Accounts): Seq[AccountAuditDetail] = {
    val cash = accounts.cash.map(number => AccountAuditDetail(AccountType("CDSCash"), AccountNumber(number)))
    val dutyDeferment = accounts.dutyDeferments.map(number => AccountAuditDetail(AccountType("DutyDeferment"), AccountNumber(number)))
    val guarantee = accounts.guarantee.map(number => AccountAuditDetail(AccountType("GeneralGuarantee"), AccountNumber(number)))

    val AccountAuditDetails = cash +: dutyDeferment.map(Some(_)) :+ guarantee
    AccountAuditDetails.collect {
      case Some(accountAuditDetail) => accountAuditDetail
    }
  }

  private def logAuditResult(auditResult: AuditResult): Unit = auditResult match {
    case Success =>
      log.debug("Splunk Audit Successful")
    case Failure(err, _) =>
      log.error(s"Splunk Audit Error, message: $err")
    case Disabled =>
      log.debug(s"Auditing Disabled")
  }
}
