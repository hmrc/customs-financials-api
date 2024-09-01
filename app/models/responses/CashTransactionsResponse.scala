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
import play.api.libs.json._

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

case class EoriData(eoriNumber: String, name: String)

object EoriData {
  implicit val format: OFormat[EoriData] = Json.format[EoriData]
}

case class Declaration(declarationID: String,
                       declarantEORINumber: String,
                       declarantRef: Option[String] = None,
                       c18OrOverpaymentReference: Option[String] = None,
                       importersEORINumber: String,
                       postingDate: String,
                       acceptanceDate: String,
                       amount: Double)

object Declaration {
  implicit val format: OFormat[Declaration] = Json.format[Declaration]
}

case class TaxGroup(taxGroupDescription: String, amount: Double)

object TaxGroup {
  implicit val format: OFormat[TaxGroup] = Json.format[TaxGroup]
}

case class TaxType(reasonForSecurity: Option[String] = None,
                   taxTypeID: String,
                   amount: Double)

object TaxType {
  implicit val format: OFormat[TaxType] = Json.format[TaxType]
}

object PaymentType extends Enumeration {
  type PaymentType = Value

  val Payment, Withdrawal, Transfer = Value

  implicit val paymentTypeReads: Reads[PaymentType.Value] = JsPath.read[String].map(strVal => PaymentType.withName(strVal))
  implicit val paymentTypeWrites: Writes[PaymentType.Value] = Writes { value => JsString(value.toString) }

  implicit val format: Format[PaymentType.Value] = Format(paymentTypeReads, paymentTypeWrites)
}

case class PaymentsWithdrawalsAndTransfer(valueDate: String,
                                          postingDate: String,
                                          paymentReference: String,
                                          amount: Double,
                                          `type`: PaymentType.Value,
                                          bankAccount: Option[String] = None,
                                          sortCode: Option[String] = None)

object PaymentsWithdrawalsAndTransfer {
  implicit val format: OFormat[PaymentsWithdrawalsAndTransfer] = Json.format[PaymentsWithdrawalsAndTransfer]
}

case class CashAccountTransactionSearchResponseDetail(can: String,
                                                      eoriDetails: Seq[EoriData],
                                                      declarations: Option[Seq[Declaration]],
                                                      taxGroups: Option[Seq[TaxGroup]],
                                                      taxTypes: Seq[TaxType],
                                                      paymentsWithdrawalsAndTransfers: Option[Seq[PaymentsWithdrawalsAndTransfer]] = None)

object CashAccountTransactionSearchResponseDetail {
  implicit val format: OFormat[CashAccountTransactionSearchResponseDetail] =
    Json.format[CashAccountTransactionSearchResponseDetail]
}

case class CashAccountTransactionSearchResponse(responseCommon: CashTransactionsResponseCommon,
                                                responseDetail: Option[CashAccountTransactionSearchResponseDetail] = None)

object CashAccountTransactionSearchResponse {
  implicit val format: OFormat[CashAccountTransactionSearchResponse] = Json.format[CashAccountTransactionSearchResponse]
}

case class CashAccountTransactionSearchResponseWrapper(cashAccountTransactionSearchResponse: CashAccountTransactionSearchResponse)

object CashAccountTransactionSearchResponseWrapper {
  implicit val format: OFormat[CashAccountTransactionSearchResponseWrapper] =
    Json.format[CashAccountTransactionSearchResponseWrapper]
}
