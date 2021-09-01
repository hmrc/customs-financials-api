/*
 * Copyright 2021 HM Revenue & Customs
 *
 */

package models.responses

import domain.AccountWithAuthorities
import models.EORI
import play.api.libs.json.Json

case class StandingAuthoritiesResponse(ownerEori: EORI, accounts: Seq[AccountWithAuthorities])

object StandingAuthoritiesResponse {
  implicit val standingAuthoritiesRequestReads = Json.reads[StandingAuthoritiesResponse]
}
