/*
 * Copyright 2021 HM Revenue & Customs
 *
 */

package models.requests.manageAuthorities

import models.EORI
import play.api.libs.json.{Json, OWrites}

case class Authority(authorisedEori: EORI,
                     authorisedFromDate: Option[String],
                     authorisedToDate: Option[String],
                     viewBalance: Option[Boolean])

object Authority {
  implicit val writes: OWrites[Authority] = Json.writes[Authority]
}
