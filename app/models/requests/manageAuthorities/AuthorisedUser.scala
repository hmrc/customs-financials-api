/*
 * Copyright 2021 HM Revenue & Customs
 *
 */

package models.requests.manageAuthorities

import play.api.libs.json.{Json, OFormat}

case class AuthorisedUser(userName: String, userRole: String)

object AuthorisedUser {
  implicit val format: OFormat[AuthorisedUser] = Json.format[AuthorisedUser]
}