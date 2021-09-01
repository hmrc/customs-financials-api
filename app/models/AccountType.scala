/*
 * Copyright 2021 HM Revenue & Customs
 *
 */

package models

import play.api.libs.json.Format
import utils.JsonFormatUtils

final case class AccountType(value: String)

object AccountType {
  implicit val format: Format[AccountType] = JsonFormatUtils.stringFormat(AccountType.apply)(_.value)
}
