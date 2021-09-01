/*
 * Copyright 2021 HM Revenue & Customs
 *
 */

package models.responses

import models.EORI
import play.api.libs.json.Json

case class CashTransactionsResponse(getCashAccountTransactionListingResponse: GetCashAccountTransactionListingResponse)

case class GetCashAccountTransactionListingResponse(responseCommon: CashTransactionsResponseCommon, responseDetail: Option[CashTransactionsResponseDetail])

case class CashTransactionsResponseCommon(status: String, statusText: Option[String], processingDate: String)

case class CashTransactionsResponseDetail(dailyStatements: Option[Seq[DailyStatementContainer]], pendingTransactions: Option[PendingTransactions])

case class DailyStatementContainer(dailyStatement: DailyStatementDetail)

case class DailyStatementDetail(date: String,
                                openingBalance: String,
                                closingBalance: String,
                                declarations: Option[Seq[DeclarationContainer]],
                                paymentsAndWithdrawals: Option[Seq[PaymentAndWithdrawalContainer]])

case class DeclarationContainer(declaration: DeclarationDetail)

case class PendingTransactions(declarations: Seq[DeclarationContainer])

case class DeclarationDetail(declarationID: String,
                             declarantEORINumber: EORI,
                             declarantReference: Option[String],
                             postingDate: String,
                             amount: String,
                             taxGroups: Seq[TaxGroupContainer])

case class TaxGroupContainer(taxGroup: TaxGroupDetail)

case class TaxGroupDetail(taxGroupDescription: String, amount: String)

case class PaymentAndWithdrawalContainer(paymentAndWithdrawal: PaymentAndWithdrawalDetail)

case class PaymentAndWithdrawalDetail(amount: String, `type`: String, bankAccount: Option[String])

object CashTransactionsResponse {

  implicit val responseCommonFormat = Json.format[CashTransactionsResponseCommon]

  implicit val paymentAndWithdrawalDetailFormat = Json.format[PaymentAndWithdrawalDetail]

  implicit val paymentAndWithdrawalContainerFormat = Json.format[PaymentAndWithdrawalContainer]

  implicit val taxGroupFormat = Json.format[TaxGroupDetail]

  implicit val taxGroupContainerFormat = Json.format[TaxGroupContainer]

  implicit val declarationFormat = Json.format[DeclarationDetail]

  implicit val declarationContainerFormat = Json.format[DeclarationContainer]

  implicit val PendingTransactionsFormat = Json.format[PendingTransactions]

  implicit val dailyStatementDetailFormat = Json.format[DailyStatementDetail]

  implicit val dailyStatementFormat = Json.format[DailyStatementContainer]

  implicit val responseDetailFormat = Json.format[CashTransactionsResponseDetail]

  implicit val getCashAccountTransactionListingResponseFormat = Json.format[GetCashAccountTransactionListingResponse]

  implicit val cashTransactionsResponseFormat = Json.format[CashTransactionsResponse]
}

