/*
 * Copyright 2021 HM Revenue & Customs
 *
 */

package domain

import models.EORI
import play.api.libs.json.Json

case class CashTransactions(pendingTransactions: Seq[Declaration], cashDailyStatements: Seq[CashDailyStatement])

case class CashDailyStatement(date: String,
                              openingBalance: String,
                              closingBalance: String,
                              declarations: Seq[Declaration],
                              otherTransactions: Seq[Transaction])

case class Declaration(movementReferenceNumber: String,
                       declarantEori: EORI,
                       declarantReference: Option[String],
                       date: String,
                       amount: String,
                       taxGroups: Seq[TaxGroup])

case class TaxGroup(taxTypeGroup: String, amount: String)

case class Transaction(amount: String, transactionType: String, bankAccountNumber: Option[String])

object CashDailyStatement {
  implicit val transactionFormat = Json.format[Transaction]
  implicit val taxGroupDetailFormat = Json.format[TaxGroup]
  implicit val declarationFormat = Json.format[Declaration]
  implicit val cashDailyStatementFormat = Json.format[CashDailyStatement]
  implicit val cashTransactionsFormat = Json.format[CashTransactions]
}
