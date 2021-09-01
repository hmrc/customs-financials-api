/*
 * Copyright 2021 HM Revenue & Customs
 *
 */

package models.requests.manageAuthorities

import play.api.libs.json.{Json, OWrites}

case class StandingAuthoritiesRequest(requestCommon: AuthoritiesRequestCommon,
                                      requestDetail: AuthoritiesRequestDetail)

object StandingAuthoritiesRequest {
  implicit val writes: OWrites[StandingAuthoritiesRequest] = Json.writes[StandingAuthoritiesRequest]
}