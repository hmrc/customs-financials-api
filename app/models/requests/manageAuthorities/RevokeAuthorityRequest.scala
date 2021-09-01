/*
 * Copyright 2021 HM Revenue & Customs
 *
 */

package models.requests.manageAuthorities

import models.{AccountNumber, EORI}
import play.api.libs.json.{Json, OFormat}

case class RevokeAuthorityRequest(
                                   accountNumber: AccountNumber,
                                   accountType: RevokeAccountType,
                                   authorisedEori: EORI,
                                   authorisedUser: AuthorisedUser
                                 )

object RevokeAuthorityRequest {

  implicit val format: OFormat[RevokeAuthorityRequest] = Json.format[RevokeAuthorityRequest]
}