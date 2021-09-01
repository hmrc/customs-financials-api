/*
 * Copyright 2021 HM Revenue & Customs
 *
 */

package models.requests

import config.AppConfig
import models.{AccountNumber, requests}
import play.api.libs.json.{Json, OFormat}

case class GuaranteeAccountTransactionsRequest(gan: AccountNumber, openItems: Option[Boolean], dates: Option[RequestDates]) {

  def toRequestDetail()(implicit appConfig: AppConfig): RequestDetail =
    requests.RequestDetail(gan, openItems.getOrElse(appConfig.onlyOpenItems), dates)

}

object GuaranteeAccountTransactionsRequest {
  implicit val guaranteeAccountTransactionRequestFormat: OFormat[GuaranteeAccountTransactionsRequest] = Json.format[GuaranteeAccountTransactionsRequest]
}
