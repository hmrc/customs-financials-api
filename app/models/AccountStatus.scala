/*
 * Copyright 2021 HM Revenue & Customs
 *
 */

package models

import play.api.libs.json.Format
import utils.JsonFormatUtils

final case class AccountStatus(value: String)

object AccountStatus {
  implicit val format: Format[AccountStatus] = JsonFormatUtils.stringFormat(AccountStatus.apply)(_.value)
}
