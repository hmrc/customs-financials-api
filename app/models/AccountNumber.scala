/*
 * Copyright 2022 HM Revenue & Customs
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
