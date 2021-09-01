/*
 * Copyright 2021 HM Revenue & Customs
 *
 */

package models.requests

import models.{AccountNumber, EORI, EmailAddress}
import play.api.libs.json.{Format, Json}

case class UpdateContactDetailsRequest(dan: AccountNumber,
                                       eori: EORI,
                                       name: Option[String],
                                       addressLine1: String,
                                       addressLine2: Option[String],
                                       addressLine3: Option[String],
                                       addressLine4: Option[String],
                                       postCode: Option[String],
                                       countryCode: Option[String],
                                       telephone: Option[String],
                                       fax: Option[String],
                                       email: Option[EmailAddress])

object UpdateContactDetailsRequest {
  implicit val format: Format[UpdateContactDetailsRequest] = Json.format[UpdateContactDetailsRequest]
}

