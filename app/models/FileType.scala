/*
 * Copyright 2021 HM Revenue & Customs
 *
 */

package models

import play.api.libs.json.Format
import utils.JsonFormatUtils

final case class FileType(value: String)

object FileType {
  implicit val format: Format[FileType] = JsonFormatUtils.stringFormat(FileType.apply)(_.value)
}
