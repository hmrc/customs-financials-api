/*
 * Copyright 2021 HM Revenue & Customs
 *
 */

package models.requests.manageAuthorities

import models.AccountType
import play.api.libs.json._

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