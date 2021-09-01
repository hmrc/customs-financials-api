/*
 * Copyright 2021 HM Revenue & Customs
 *
 */

package models

import play.api.libs.json.Format
import utils.JsonFormatUtils

final case class AccountNumber(value: String)

object AccountNumber {
  def apply(value: Option[String]): AccountNumber =
    value match {
      case Some(value) => AccountNumber(value)
      case None => AccountNumber("")
    }


  implicit val format: Format[AccountNumber] = JsonFormatUtils.stringFormat(AccountNumber.apply)(_.value)
}
