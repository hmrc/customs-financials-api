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

package models

import play.api.libs.json.Format
import play.api.mvc.PathBindable
import utils.JsonFormatUtils

final case class FileRole(value: String)

object FileRole {
  implicit val format: Format[FileRole] = JsonFormatUtils.stringFormat(FileRole.apply)(_.value)

  implicit def pathBinder(implicit stringBinder: PathBindable[String]): PathBindable[FileRole] = new PathBindable[FileRole] {
    override def bind(key: String, value: String): Either[String, FileRole] = {
      stringBinder.bind(key, value).map(FileRole(_))
    }

    override def unbind(key: String, fileRole: FileRole): String =
      fileRole.value
  }
}
