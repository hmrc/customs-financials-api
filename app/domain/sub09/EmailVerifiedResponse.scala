/*
 * Copyright 2021 HM Revenue & Customs
 *
 */

package domain.sub09

import models.EmailAddress
import play.api.libs.json.Json

case class EmailVerifiedResponse(verifiedEmail: Option[EmailAddress])

object EmailVerifiedResponse {
  implicit val format = Json.format[EmailVerifiedResponse]
}
