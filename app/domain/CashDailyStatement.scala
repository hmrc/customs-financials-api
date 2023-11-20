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
                       importerEori: Option[EORI],
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
