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

package models.responses

import models.EORI
import play.api.libs.json.{Json, OFormat}

case class CashTransactionsResponse(getCashAccountTransactionListingResponse: GetCashAccountTransactionListingResponse)

case class GetCashAccountTransactionListingResponse(responseCommon: CashTransactionsResponseCommon,
                                                    responseDetail: Option[CashTransactionsResponseDetail])

case class CashTransactionsResponseCommon(status: String,
                                          statusText: Option[String],
                                          processingDate: String,
                                          maxTransactionsExceeded: Option[Boolean])

case class CashTransactionsResponseDetail(dailyStatements: Option[Seq[DailyStatementContainer]],
                                          pendingTransactions: Option[PendingTransactions])

case class DailyStatementContainer(dailyStatement: DailyStatementDetail)

case class DailyStatementDetail(date: String,
                                openingBalance: String,
                                closingBalance: String,
                                declarations: Option[Seq[DeclarationContainer]],
                                paymentsAndWithdrawals: Option[Seq[PaymentAndWithdrawalContainer]])

case class DeclarationContainer(declaration: DeclarationDetail)

case class PendingTransactions(declarations: Seq[DeclarationContainer])

case class DeclarationDetail(declarationID: String,
                             importerEORINumber: Option[EORI],
                             declarantEORINumber: EORI,
                             declarantReference: Option[String],
                             postingDate: String,
                             amount: String,
                             taxGroups: Seq[TaxGroupContainer])

case class TaxGroupContainer(taxGroup: TaxGroupDetail)

case class TaxGroupDetail(taxGroupDescription: String, amount: String)

case class PaymentAndWithdrawalContainer(paymentAndWithdrawal: PaymentAndWithdrawalDetail)

case class PaymentAndWithdrawalDetail(amount: String,
                                      `type`: String,
                                      bankAccount: Option[String])

object CashTransactionsResponse {

  implicit val responseCommonFormat: OFormat[CashTransactionsResponseCommon] =
    Json.format[CashTransactionsResponseCommon]

  implicit val paymentAndWithdrawalDetailFormat: OFormat[PaymentAndWithdrawalDetail] =
    Json.format[PaymentAndWithdrawalDetail]

  implicit val paymentAndWithdrawalContainerFormat: OFormat[PaymentAndWithdrawalContainer] =
    Json.format[PaymentAndWithdrawalContainer]

  implicit val taxGroupFormat: OFormat[TaxGroupDetail] = Json.format[TaxGroupDetail]

  implicit val taxGroupContainerFormat: OFormat[TaxGroupContainer] = Json.format[TaxGroupContainer]

  implicit val declarationFormat: OFormat[DeclarationDetail] = Json.format[DeclarationDetail]

  implicit val declarationContainerFormat: OFormat[DeclarationContainer] = Json.format[DeclarationContainer]

  implicit val PendingTransactionsFormat: OFormat[PendingTransactions] = Json.format[PendingTransactions]

  implicit val dailyStatementDetailFormat: OFormat[DailyStatementDetail] = Json.format[DailyStatementDetail]

  implicit val dailyStatementFormat: OFormat[DailyStatementContainer] = Json.format[DailyStatementContainer]

  implicit val responseDetailFormat: OFormat[CashTransactionsResponseDetail] =
    Json.format[CashTransactionsResponseDetail]

  implicit val getCashAccountTransactionListingResponseFormat: OFormat[GetCashAccountTransactionListingResponse] =
    Json.format[GetCashAccountTransactionListingResponse]

  implicit val cashTransactionsResponseFormat: OFormat[CashTransactionsResponse] = Json.format[CashTransactionsResponse]
}
