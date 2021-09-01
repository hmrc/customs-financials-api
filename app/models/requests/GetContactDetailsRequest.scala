/*
 * Copyright 2021 HM Revenue & Customs
 *
 */

package models.requests

import models.{AccountNumber, EORI}
import play.api.libs.json.{Format, Json}

case class GetContactDetailsRequest(dan: AccountNumber, eori: EORI)

object GetContactDetailsRequest {
  implicit val format: Format[GetContactDetailsRequest] = Json.format[GetContactDetailsRequest]
}
