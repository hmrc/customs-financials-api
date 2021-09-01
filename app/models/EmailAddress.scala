/*
 * Copyright 2021 HM Revenue & Customs
 *
 */

package models

import play.api.libs.json.Format
import utils.JsonFormatUtils

final case class EmailAddress(value: String)

object EmailAddress {
  implicit val format: Format[EmailAddress] = JsonFormatUtils.stringFormat(EmailAddress.apply)(_.value)
}
