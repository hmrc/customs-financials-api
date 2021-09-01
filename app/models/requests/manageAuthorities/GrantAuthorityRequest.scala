/*
 * Copyright 2021 HM Revenue & Customs
 *
 */

package models.requests.manageAuthorities

import domain.StandingAuthority
import play.api.libs.json.{Json, OFormat}

case class GrantAuthorityRequest(
                                  accounts: Accounts,
                                  authority: StandingAuthority,
                                  authorisedUser: AuthorisedUser,
                                  editRequest: Boolean
                                )

object GrantAuthorityRequest {
  implicit val accountsFormats: OFormat[Accounts] = Json.format[Accounts]
  implicit val grantAuthorityRequestFormat: OFormat[GrantAuthorityRequest] = Json.format[GrantAuthorityRequest]
}