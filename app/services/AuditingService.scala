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

package services

import config.AppConfig
import domain._
import domain.acc40.ResponseDetail
import models.requests.{CashAccountStatementRequestDetail, CashAccountTransactionSearchRequestDetails, HistoricDocumentRequest}
import models.requests.manageAuthorities._
import models._
import play.api.http.HeaderNames
import play.api.libs.json.{JsValue, Json}
import play.api.{Logger, LoggerLike}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.audit.AuditExtensions
import uk.gov.hmrc.play.audit.http.connector.AuditResult.{Disabled, Failure, Success}
import uk.gov.hmrc.play.audit.http.connector.{AuditConnector, AuditResult}
import uk.gov.hmrc.play.audit.model.ExtendedDataEvent
import utils.Utils.{emptyString, hyphen}

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class AuditingService @Inject()(appConfig: AppConfig,
                                auditConnector: AuditConnector)(implicit executionContext: ExecutionContext) {

  val log: LoggerLike = Logger(this.getClass)

  private val MANAGE_AUTHORITY_AUDIT_TYPE = "ManageAuthority"
  private val UPDATE_AUTHORITY_AUDIT_TYPE = "UpdateAuthority"
  private val EDIT_AUTHORITY_ACTION = "Update Authority"
  private val GRANT_AUTHORITY_ACTION = "Grant Authority"
  private val REVOKE_AUTHORITY_ACTION = "Revoke Authority"
  private val HISTORIC_STATEMENT_REQUEST_AUDIT_TYPE = "RequestHistoricStatement"
  private val HISTORIC_STATEMENT_REQUEST_TRANSACTION_NAME = "Request historic statements"
  private val REQUEST_STANDING_AUTHORITIES_NAME = "Request Authorities CSV"
  private val REQUEST_STANDING_AUTHORITIES_TYPE = "RequestAuthoritiesCSV"
  private val REQUEST_AUTHORITIES_NAME = "Request Authorities"
  private val REQUEST_AUTHORITIES_TYPE = "RequestAuthorities"
  private val DISPLAY_STANDING_AUTHORITIES_NAME = "Display Authorities CSV"
  private val DISPLAY_STANDING_AUTHORITIES_TYPE = "DisplayStandingAuthoritiesCSV"
  private val CASH_ACCOUNT_TRANSACTIONS_SEARCH_TRANSACTION_NAME = "Search cash account transactions"
  private val CASH_ACCOUNT_TRANSACTIONS_SEARCH_AUDIT_TYPE = "SearchCashAccountTransactions"

  private val referrer: HeaderCarrier => String = _.headers(Seq(HeaderNames.REFERER)).headOption.fold(hyphen)(_._2)

  def auditEditAuthority(grantAuthorityRequest: GrantAuthorityRequest,
                         eori: EORI)(implicit hc: HeaderCarrier): Future[AuditResult] = {
    val accountDetails =
      convertToAuditRequest(
        grantAuthorityRequest.accounts).headOption
        .getOrElse(AccountAuditDetail(AccountType(hyphen), AccountNumber(hyphen)))

    val auditJson: JsValue = Json.toJson(EditAuthorityRequestAuditDetail(eori,
      grantAuthorityRequest.authority.authorisedEori,
      EDIT_AUTHORITY_ACTION,
      accountDetails.accountType,
      accountDetails.accountNumber,
      grantAuthorityRequest.authority.authorisedFromDate,
      grantAuthorityRequest.authority.authorisedToDate.getOrElse(emptyString),
      grantAuthorityRequest.authority.viewBalance,
      grantAuthorityRequest.authorisedUser.userName,
      grantAuthorityRequest.authorisedUser.userRole))

    audit(AuditModel(EDIT_AUTHORITY_ACTION, auditJson, UPDATE_AUTHORITY_AUDIT_TYPE))
  }

  def auditGrantAuthority(grantAuthorityRequest: GrantAuthorityRequest,
                          eori: EORI)(implicit hc: HeaderCarrier): Future[AuditResult] = {
    val auditJson = Json.toJson(GrantAuthorityRequestAuditDetail(eori,
      grantAuthorityRequest.authority.authorisedEori,
      GRANT_AUTHORITY_ACTION,
      convertToAuditRequest(grantAuthorityRequest.accounts),
      grantAuthorityRequest.authority.authorisedFromDate,
      grantAuthorityRequest.authority.authorisedToDate.getOrElse(emptyString),
      grantAuthorityRequest.authority.viewBalance,
      grantAuthorityRequest.authorisedUser.userName,
      grantAuthorityRequest.authorisedUser.userRole))

    audit(AuditModel(GRANT_AUTHORITY_ACTION, auditJson, MANAGE_AUTHORITY_AUDIT_TYPE))
  }

  def auditRevokeAuthority(revokeAuthorityRequest: RevokeAuthorityRequest,
                           eori: EORI)(implicit hc: HeaderCarrier): Future[AuditResult] = {
    val auditJson = Json.toJson(RevokeAuthorityRequestAuditDetail(eori,
      revokeAuthorityRequest.authorisedEori,
      REVOKE_AUTHORITY_ACTION,
      RevokeAccountType.toAuditLabel(revokeAuthorityRequest.accountType),
      revokeAuthorityRequest.accountNumber,
      revokeAuthorityRequest.authorisedUser.userName,
      revokeAuthorityRequest.authorisedUser.userRole))

    audit(AuditModel(REVOKE_AUTHORITY_ACTION, auditJson, MANAGE_AUTHORITY_AUDIT_TYPE))
  }

  def auditRequestAuthCSVStatementRequest(response: acc41.ResponseDetail,
                                          request: domain.acc41.RequestDetail)
                                         (implicit hc: HeaderCarrier): Future[AuditResult] = {

    val auditJson = Json.toJson(RequestAuthCSVAuditDetail(
      request.requestingEORI.value,
      response.requestAcceptedDate.get))

    audit(AuditModel(REQUEST_STANDING_AUTHORITIES_NAME, auditJson, REQUEST_STANDING_AUTHORITIES_TYPE))
  }

  def auditDisplayAuthCSVStatementRequest(notification: Notification,
                                          fileType: FileType)
                                         (implicit hc: HeaderCarrier): Future[AuditResult] = {

    val auditJson = Json.toJson(RequestDisplayStandingAuthCSVAuditDetail(
      Eori = notification.eori.toString,
      isHistoric = notification.metadata.contains("statementRequestID"),
      fileName = notification.fileName,
      fileRole = notification.fileRole.toString,
      fileType = fileType.toString))

    audit(AuditModel(DISPLAY_STANDING_AUTHORITIES_NAME, auditJson, DISPLAY_STANDING_AUTHORITIES_TYPE))
  }

  def auditRequestAuthStatementRequest(response: ResponseDetail,
                                       request: domain.acc40.RequestDetail)
                                      (implicit hc: HeaderCarrier): Future[AuditResult] = {

    val auditJson = Json.toJson(RequestAuthAuditDetail(
      request.requestingEORI.value,
      request.searchType,
      request.searchID.value,
      response.numberOfAuthorities,
      response.dutyDefermentAccounts,
      response.generalGuaranteeAccounts,
      response.cdsCashAccounts))

    audit(AuditModel(REQUEST_AUTHORITIES_NAME, auditJson, REQUEST_AUTHORITIES_TYPE))
  }

  def auditHistoricStatementRequest(historicDocumentRequest: HistoricDocumentRequest)
                                   (implicit hc: HeaderCarrier): Future[AuditResult] = {
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

  def auditCashAccountTransactionsSearch(cashAccountTransSearchRequest: CashAccountTransactionSearchRequestDetails)
                                        (implicit hc: HeaderCarrier): Future[AuditResult] = {
    val auditJson = Json.toJson(cashAccountTransSearchRequest)

    audit(
      AuditModel(
        CASH_ACCOUNT_TRANSACTIONS_SEARCH_TRANSACTION_NAME,
        auditJson,
        CASH_ACCOUNT_TRANSACTIONS_SEARCH_AUDIT_TYPE)
    )
  }

  def auditCashAccountStatementsRequest(cashAccountStatementRequest: CashAccountStatementRequestDetail)
                                       (implicit hc: HeaderCarrier): Future[AuditResult] = {
    val auditJson = Json.toJson(cashAccountStatementRequest)

    audit(
      AuditModel(
        CASH_ACCOUNT_TRANSACTIONS_SEARCH_TRANSACTION_NAME,
        auditJson,
        CASH_ACCOUNT_TRANSACTIONS_SEARCH_AUDIT_TYPE)
    )
  }

  private def audit(auditModel: AuditModel)(implicit hc: HeaderCarrier): Future[AuditResult] = {
    val dataEvent = ExtendedDataEvent(
      auditSource = appConfig.appName,
      auditType = auditModel.auditType,
      tags = AuditExtensions.auditHeaderCarrier(hc).toAuditTags(auditModel.transactionName, referrer(hc)),
      detail = auditModel.detail)

    log.debug(s"Splunk Audit Event:\n$dataEvent\n")

    auditConnector.sendExtendedEvent(dataEvent)
      .map { auditResult =>
        logAuditResult(auditResult)
        auditResult
      }
  }

  private def convertToAuditRequest(accounts: Accounts): Seq[AccountAuditDetail] = {
    val cash = accounts.cash.map(number => AccountAuditDetail(AccountType("CDSCash"), AccountNumber(number)))

    val dutyDeferment =
      accounts.dutyDeferments.map(number => AccountAuditDetail(AccountType("DutyDeferment"), AccountNumber(number)))

    val guarantee =
      accounts.guarantee.map(number => AccountAuditDetail(AccountType("GeneralGuarantee"), AccountNumber(number)))

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
