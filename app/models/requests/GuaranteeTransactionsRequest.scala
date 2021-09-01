/*
 * Copyright 2021 HM Revenue & Customs
 *
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
