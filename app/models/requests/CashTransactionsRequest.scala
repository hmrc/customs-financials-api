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

package models.requests

import play.api.libs.json.{Json, OWrites}

case class CashTransactionsRequest(getCashAccountTransactionListingRequest: GetCashAccountTransactionListingRequest)

case class GetCashAccountTransactionListingRequest(requestCommon: CashTransactionsRequestCommon,
                                                   requestDetail: CashTransactionsRequestDetail)

case class CashTransactionsRequestCommon(originatingSystem: String,
                                         receiptDate: String,
                                         acknowledgementReference: String)

case class CashTransactionsRequestDetail(CAN: String, dates: CashTransactionsRequestDates)

case class CashTransactionsRequestDates(dateFrom: String, dateTo: String)


object CashTransactionsRequest {

  implicit val requestDatesWrites: OWrites[CashTransactionsRequestDates] = Json.writes[CashTransactionsRequestDates]

  implicit val requestDetailWrites: OWrites[CashTransactionsRequestDetail] = Json.writes[CashTransactionsRequestDetail]

  implicit val requestCommonWrites: OWrites[CashTransactionsRequestCommon] = Json.writes[CashTransactionsRequestCommon]

  implicit val getCashAccountTransactionListingRequestWrites: OWrites[GetCashAccountTransactionListingRequest] =
    Json.writes[GetCashAccountTransactionListingRequest]

  implicit val cashTransactionsRequestWrites: OWrites[CashTransactionsRequest] = Json.writes[CashTransactionsRequest]
}
