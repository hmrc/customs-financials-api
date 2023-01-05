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

import models.AccountNumber
import play.api.libs.json._

import java.time.LocalDate

case class GuaranteeTransactionsRequest(getGGATransactionListing: GGATransactionListing)

case class GGATransactionListing(requestCommon: RequestCommon, requestDetail: RequestDetail)

case class RequestParameters(paramName: String, paramValue: String)

case class RequestCommon(receiptDate: String,
                         acknowledgementReference: String, requestParameters: RequestParameters)

case class RequestDetail(gan: AccountNumber, openItems: Boolean, dates: Option[RequestDates])

case class RequestDates(dateFrom: LocalDate, dateTo: LocalDate)

object RequestDates {
  implicit val requestDates: OFormat[RequestDates] = Json.format[RequestDates]
}


object GuaranteeTransactionsRequest {

  implicit val requestDetailWrites = Json.writes[RequestDetail]

  implicit val requestParametersWrites = Json.writes[RequestParameters]

  implicit val requestCommonWrites = Json.writes[RequestCommon]

  implicit val ggaTransactionListingWrites = Json.writes[GGATransactionListing]

  implicit val guaranteeTransactionsRequestWrites = Json.writes[GuaranteeTransactionsRequest]

}
