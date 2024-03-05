/*
 * Copyright 2023 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package domain.acc38

import config.MetaConfig.RETURN_PARAM_POSITION
import models.{AccountNumber, AccountType, EORI, EmailAddress}
import play.api.libs.json._

case class Response(getCorrespondenceAddressResponse: GetCorrespondenceAddressResponse) {
  val mdtpError: Boolean = getCorrespondenceAddressResponse
    .responseCommon
    .returnParameters.exists(_.exists(_.paramName == RETURN_PARAM_POSITION))
}

object Response {
  implicit val format: OFormat[Response] = Json.format[Response]
}

case class GetCorrespondenceAddressResponse(responseCommon: ResponseCommon,
                                            responseDetail: Option[ResponseDetail])

object GetCorrespondenceAddressResponse {
  implicit val format: OFormat[GetCorrespondenceAddressResponse] = Json.format[GetCorrespondenceAddressResponse]
}

case class ReturnParameter(paramName: String, paramValue: String)

object ReturnParameter {
  implicit val format: OFormat[ReturnParameter] = Json.format[ReturnParameter]
}

case class ResponseCommon(status: String,
                          statusText: Option[String],
                          processingDate: String,
                          returnParameters: Option[List[ReturnParameter]])

object ResponseCommon {
  implicit val format: OFormat[ResponseCommon] = Json.format[ResponseCommon]
}

case class ResponseDetail(eori: EORI,
                          accountDetails: AccountDetails,
                          contactDetails: ContactDetails)

object ResponseDetail {
  implicit val format: OFormat[ResponseDetail] = Json.format[ResponseDetail]
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
  implicit val format: OFormat[ContactDetails] = Json.format[ContactDetails]
}

case class ErrorResponse(errorDetail: ErrorDetail)

object ErrorResponse {
  implicit val format: OFormat[ErrorResponse] = Json.format[ErrorResponse]
}

case class ErrorDetail(timestamp: String,
                       correlationId: String,
                       errorCode: String,
                       errorMessage: String,
                       source: String,
                       sourceFaultDetail: SourceFaultDetail)

object ErrorDetail {
  implicit val format: OFormat[ErrorDetail] = Json.format[ErrorDetail]
}

case class SourceFaultDetail(detail: Array[String])

object SourceFaultDetail {
  implicit val format: OFormat[SourceFaultDetail] = Json.format[SourceFaultDetail]
}
