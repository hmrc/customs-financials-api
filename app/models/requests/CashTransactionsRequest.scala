/*
 * Copyright 2021 HM Revenue & Customs
 *
 */

package models.requests

import play.api.libs.json.Json

case class CashTransactionsRequest(getCashAccountTransactionListingRequest: GetCashAccountTransactionListingRequest)

case class GetCashAccountTransactionListingRequest(requestCommon: CashTransactionsRequestCommon, requestDetail: CashTransactionsRequestDetail)

case class CashTransactionsRequestCommon(originatingSystem: String,
                                         receiptDate: String,
                                         acknowledgementReference: String)

case class CashTransactionsRequestDetail(CAN: String, dates: CashTransactionsRequestDates)

case class CashTransactionsRequestDates(dateFrom: String, dateTo: String)


object CashTransactionsRequest {

  implicit val requestDatesWrites = Json.writes[CashTransactionsRequestDates]

  implicit val requestDetailWrites = Json.writes[CashTransactionsRequestDetail]

  implicit val requestCommonWrites = Json.writes[CashTransactionsRequestCommon]

  implicit val getCashAccountTransactionListingRequestWrites = Json.writes[GetCashAccountTransactionListingRequest]

  implicit val cashTransactionsRequestWrites = Json.writes[CashTransactionsRequest]

}
