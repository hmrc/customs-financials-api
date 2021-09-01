/*
 * Copyright 2021 HM Revenue & Customs
 *
 */

package models.requests.manageAuthorities

import models.EORI
import play.api.libs.json.{Json, OWrites}

case class AuthoritiesRequestDetail(ownerEori: EORI)

object AuthoritiesRequestDetail {
  implicit val writes: OWrites[AuthoritiesRequestDetail] = Json.writes[AuthoritiesRequestDetail]
}