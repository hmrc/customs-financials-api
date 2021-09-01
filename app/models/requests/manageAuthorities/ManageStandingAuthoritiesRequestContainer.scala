/*
 * Copyright 2021 HM Revenue & Customs
 *
 */

package models.requests.manageAuthorities

import play.api.libs.json.{Json, OWrites}

case class ManageStandingAuthoritiesRequestContainer(manageStandingAuthoritiesRequest: ManageStandingAuthoritiesRequest)

object ManageStandingAuthoritiesRequestContainer {
  implicit val writes: OWrites[ManageStandingAuthoritiesRequestContainer] = Json.writes[ManageStandingAuthoritiesRequestContainer]
}
