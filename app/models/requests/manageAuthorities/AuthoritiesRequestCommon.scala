/*
 * Copyright 2021 HM Revenue & Customs
 *
 */

package models.requests.manageAuthorities

import play.api.libs.json.{Json, OWrites}

case class AuthoritiesRequestCommon(regime: String, receiptDate: String, acknowledgementReference: String, originatingSystem: String)

object AuthoritiesRequestCommon {
  implicit val writes: OWrites[AuthoritiesRequestCommon] = Json.writes[AuthoritiesRequestCommon]
}