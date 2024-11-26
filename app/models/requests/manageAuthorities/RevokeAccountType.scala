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

package models.requests.manageAuthorities

import models.AccountType
import play.api.libs.json.*

sealed trait RevokeAccountType

case object CdsCashAccount extends RevokeAccountType

case object CdsDutyDefermentAccount extends RevokeAccountType

case object CdsGeneralGuaranteeAccount extends RevokeAccountType

object RevokeAccountType {

  def toAuditLabel(accountType: RevokeAccountType): AccountType = accountType match {
    case CdsCashAccount => AccountType("CDSCash")
    case CdsDutyDefermentAccount => AccountType("DutyDeferment")
    case CdsGeneralGuaranteeAccount => AccountType("GeneralGuarantee")
  }

  implicit val reads: Reads[RevokeAccountType] = (json: JsValue) => {
    json.as[String] match {
      case "CDSCash" => JsSuccess(CdsCashAccount)
      case "DutyDeferment" => JsSuccess(CdsDutyDefermentAccount)
      case "GeneralGuarantee" => JsSuccess(CdsGeneralGuaranteeAccount)
    }
  }

  implicit val writes: Writes[RevokeAccountType] = (obj: RevokeAccountType) => JsString(
    obj match {
      case CdsCashAccount => "CDSCash"
      case CdsDutyDefermentAccount => "DutyDeferment"
      case CdsGeneralGuaranteeAccount => "GeneralGuarantee"
    }
  )

}
