/*
 * Copyright 2021 HM Revenue & Customs
 *
 */

package domain.acc37

import models.requests.UpdateContactDetailsRequest
import models.{AccountNumber, AccountType, EORI, EmailAddress}
import play.api.libs.json.{Json, OFormat}

case class Request(amendCorrespondenceAddressRequest: AmendCorrespondenceAddressRequest)

object Request {
  implicit val format: OFormat[Request] = Json.format[Request]
}

case class AmendCorrespondenceAddressRequest(requestCommon: RequestCommon,
                                             requestDetail: RequestDetail)

object AmendCorrespondenceAddressRequest {
  implicit val format: OFormat[AmendCorrespondenceAddressRequest] = Json.format[AmendCorrespondenceAddressRequest]
}

case class RequestCommon(originatingSystem: String,
                         receiptDate: String,
                         acknowledgementReference: String)

object RequestCommon {
  implicit val format: OFormat[RequestCommon] = Json.format[RequestCommon]
}

case class RequestDetail(eori: EORI,
                         accountDetails: AccountDetails,
                         contactDetails: ContactDetails,
                         reasonForChange: Option[String])

object RequestDetail {
  implicit val format: OFormat[RequestDetail] = Json.format[RequestDetail]
}

case class AccountDetails(accountType: AccountType, accountNumber: AccountNumber)

object AccountDetails {
  implicit val format: OFormat[AccountDetails] = Json.format[AccountDetails]
}

case class ContactDetails(contactName: Option[String],
                          addressLine1: String,
                          addressLine2: Option[String],
                          addressLine3: Option[String],
                          addressLine4: Option[String],
                          postCode: Option[String],
                          countryCode: String,
                          telephone: Option[String],
                          faxNumber: Option[String],
                          email: Option[EmailAddress])

object ContactDetails {

  def fromRequest(request: UpdateContactDetailsRequest): ContactDetails = {
    ContactDetails(
      request.name,
      request.addressLine1,
      request.addressLine2,
      request.addressLine3,
      request.addressLine4,
      request.postCode,
      request.countryCode.getOrElse(""),
      request.telephone,
      request.fax,
      request.email
    )
  }

  implicit val format: OFormat[ContactDetails] = Json.format[ContactDetails]
}
