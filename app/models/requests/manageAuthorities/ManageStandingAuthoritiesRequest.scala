/*
 * Copyright 2021 HM Revenue & Customs
 *
 */

package models.requests.manageAuthorities

import play.api.libs.json.{Json, OWrites}

case class ManageStandingAuthoritiesRequest(requestCommon: AuthoritiesRequestCommon,
                                            requestDetail: ManageStandingAuthoritiesRequestDetail)

object ManageStandingAuthoritiesRequest {
  implicit val writes: OWrites[ManageStandingAuthoritiesRequest] = Json.writes[ManageStandingAuthoritiesRequest]
}
