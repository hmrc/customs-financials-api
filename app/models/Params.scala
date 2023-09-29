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

import play.api.libs.json.{Json, OFormat}

case class Params(periodStartMonth: String,
                  periodStartYear: String,
                  periodEndMonth: String,
                  periodEndYear: String,
                  accountType: String,
                  dan: String) {
  require(
    AccountTypeForParams.fromString(accountType).nonEmpty,
    "invalid value for accountType," +
      " valid values are C79Certificate, PostponedVATStatement, " +
      "SecurityStatement, DutyDefermentStatement")
}

object Params {
  implicit val paramsFormat: OFormat[Params] = Json.format[Params]
}

object AccountTypeForParams extends Enumeration {
  type AccountTypeForParams = Value
  val C79Certificate, PostponedVATStatement, SecurityStatement, DutyDefermentStatement = Value

  def fromString(value: String): Option[AccountTypeForParams] = {
    values.find(_.toString.toLowerCase == value.toLowerCase)
  }
}
