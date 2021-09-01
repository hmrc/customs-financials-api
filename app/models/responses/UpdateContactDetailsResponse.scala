/*
 * Copyright 2021 HM Revenue & Customs
 *
 */

package models.responses

import play.api.libs.json.{Json, OWrites}

case class UpdateContactDetailsResponse(success: Boolean)
object UpdateContactDetailsResponse {
  implicit val writes: OWrites[UpdateContactDetailsResponse] = Json.writes[UpdateContactDetailsResponse]
}
