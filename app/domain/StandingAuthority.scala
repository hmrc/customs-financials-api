/*
 * Copyright 2021 HM Revenue & Customs
 *
 */

package domain

import models.EORI
import play.api.libs.json.{Json, OFormat}

case class StandingAuthority(authorisedEori: EORI,
                             authorisedFromDate: String,
                             authorisedToDate: Option[String],
                             viewBalance: Boolean)

object StandingAuthority {
  implicit val standingAuthorityFormat: OFormat[StandingAuthority] = Json.format[StandingAuthority]
}
