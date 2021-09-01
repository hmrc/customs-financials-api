/*
 * Copyright 2021 HM Revenue & Customs
 *
 */

package models

import play.api.libs.json.Format
import play.api.mvc.PathBindable
import utils.JsonFormatUtils

final case class FileRole(value: String)

object FileRole {
  implicit val format: Format[FileRole] = JsonFormatUtils.stringFormat(FileRole.apply)(_.value)

  implicit def pathBinder(implicit stringBinder: PathBindable[String]): PathBindable[FileRole] = new PathBindable[FileRole] {
    override def bind(key: String, value: String): Either[String, FileRole] = {
      stringBinder.bind(key, value).right.map(FileRole(_))
    }

    override def unbind(key: String, fileRole: FileRole): String =
      fileRole.value
  }
}
