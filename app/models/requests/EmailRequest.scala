/*
 * Copyright 2021 HM Revenue & Customs
 *
 */

package models.requests

import models.EmailAddress
import play.api.libs.json.{Json, OFormat}


case class EmailRequest(to: List[EmailAddress],
                        templateId: String,
                        parameters: Map[String, String],
                        force: Boolean,
                        enrolment: Option[String],
                        eventUrl: Option[String],
                        onSendUrl: Option[String])

object EmailRequest {
  implicit val emailRequestFormat: OFormat[EmailRequest] = Json.format[EmailRequest]
}
