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

import models.requests
import play.api.libs.json.*
import play.api.libs.ws.BodyWritable

case class CashTransactionsRequest(getCashAccountTransactionListingRequest: GetCashAccountTransactionListingRequest)

case class GetCashAccountTransactionListingRequest(
  requestCommon: CashTransactionsRequestCommon,
  requestDetail: CashTransactionsRequestDetail
)

case class CashTransactionsRequestCommon(
  originatingSystem: String,
  receiptDate: String,
  acknowledgementReference: String
)

case class CashTransactionsRequestDetail(CAN: String, dates: CashTransactionsRequestDates)

case class CashTransactionsRequestDates(dateFrom: String, dateTo: String)

object CashTransactionsRequest {

  implicit val requestDatesWrites: OWrites[CashTransactionsRequestDates] = Json.writes[CashTransactionsRequestDates]

  implicit val requestDetailWrites: OWrites[CashTransactionsRequestDetail] = Json.writes[CashTransactionsRequestDetail]

  implicit val requestCommonWrites: OWrites[CashTransactionsRequestCommon] = Json.writes[CashTransactionsRequestCommon]

  implicit val getCashAccountTransactionListingRequestWrites: OWrites[GetCashAccountTransactionListingRequest] =
    Json.writes[GetCashAccountTransactionListingRequest]

  implicit val cashTransactionsRequestWrites: OWrites[CashTransactionsRequest] = Json.writes[CashTransactionsRequest]

  implicit def jsonBodyWritable[T](implicit
    writes: Writes[T],
    jsValueBodyWritable: BodyWritable[JsValue]
  ): BodyWritable[T] = jsValueBodyWritable.map(writes.writes)
}

object SearchType extends Enumeration {
  type SearchType = Value

  val P, D = Value

  implicit val searchTypeReads: Reads[SearchType.Value]   = JsPath.read[String].map(strVal => SearchType.withName(strVal))
  implicit val searchTypeWrites: Writes[SearchType.Value] = Writes(value => JsString(value.toString))

  implicit val searchTypeFormat: Format[requests.SearchType.Value] = Format(searchTypeReads, searchTypeWrites)
}

object ParamName extends Enumeration {
  type ParamName = Value

  val MRN, UCR = Value

  implicit val paramNameReads: Reads[ParamName.Value]   = JsPath.read[String].map(strVal => ParamName.withName(strVal))
  implicit val paramNameWrites: Writes[ParamName.Value] = Writes(value => JsString(value.toString))

  implicit val paramNameFormat: Format[requests.ParamName.Value] = Format(paramNameReads, paramNameWrites)
}

case class DeclarationDetails(paramName: ParamName.Value, paramValue: String)

object DeclarationDetails {
  implicit val format: OFormat[DeclarationDetails] = Json.format[DeclarationDetails]
}

case class CashAccountPaymentDetails(amount: Double, dateFrom: Option[String] = None, dateTo: Option[String] = None)

object CashAccountPaymentDetails {
  implicit val format: OFormat[CashAccountPaymentDetails] = Json.format[CashAccountPaymentDetails]
}

case class CashAccountTransactionSearchRequestDetails(
  can: String,
  ownerEORI: String,
  searchType: SearchType.Value,
  declarationDetails: Option[DeclarationDetails] = None,
  cashAccountPaymentDetails: Option[CashAccountPaymentDetails] = None
)

object CashAccountTransactionSearchRequestDetails {

  implicit val format: OFormat[CashAccountTransactionSearchRequestDetails] =
    Json.format[CashAccountTransactionSearchRequestDetails]
}

case class CashAccountTransactionSearchRequest(
  requestCommon: CashTransactionsRequestCommon,
  requestDetail: CashAccountTransactionSearchRequestDetails
)

object CashAccountTransactionSearchRequest {
  implicit val requestCommonWrites: OFormat[CashTransactionsRequestCommon]              = Json.format[CashTransactionsRequestCommon]
  implicit val requestDetailFormat: OFormat[CashAccountTransactionSearchRequestDetails] =
    Json.format[CashAccountTransactionSearchRequestDetails]

  implicit val format: OFormat[CashAccountTransactionSearchRequest] = Json.format[CashAccountTransactionSearchRequest]
}

case class CashAccountTransactionSearchRequestContainer(
  cashAccountTransactionSearchRequest: CashAccountTransactionSearchRequest
)

object CashAccountTransactionSearchRequestContainer {
  implicit val format: OFormat[CashAccountTransactionSearchRequestContainer] =
    Json.format[CashAccountTransactionSearchRequestContainer]

  implicit def jsonBodyWritable[T](implicit
    writes: Writes[T],
    jsValueBodyWritable: BodyWritable[JsValue]
  ): BodyWritable[T] = jsValueBodyWritable.map(writes.writes)
}
