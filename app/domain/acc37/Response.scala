/*
 * Copyright 2021 HM Revenue & Customs
 *
 */

package domain.acc37

import play.api.libs.json.{Json, OFormat}

case class Response(amendCorrespondenceAddressResponse: AmendCorrespondenceAddressResponse){
  val mdtpError: Boolean = amendCorrespondenceAddressResponse
    .responseCommon
    .returnParameters.exists(_.exists(_.paramName == "POSITION"))
}

case class AmendCorrespondenceAddressResponse(responseCommon: ResponseCommon)

case class ResponseCommon(status: String,
                          statusText: Option[String],
                          processingDate: String,
                          returnParameters: Option[Array[ReturnParameter]])

case class ReturnParameter(paramName: String, paramValue: String)

object ReturnParameter {
  implicit val returnParameter: OFormat[ReturnParameter] = Json.format[ReturnParameter]
}

object ResponseCommon {
  implicit val responseCommonFormat: OFormat[ResponseCommon] = Json.format[ResponseCommon]
}

object AmendCorrespondenceAddressResponse {
  implicit val updateResponseFormat: OFormat[AmendCorrespondenceAddressResponse] = Json.format[AmendCorrespondenceAddressResponse]
}

object Response {
  implicit val responseFormat: OFormat[Response] = Json.format[Response]
}

case class ErrorResponse(errorDetail: ErrorDetail)

case class ErrorDetail(timestamp: String,
                       correlationId: String,
                       errorCode: String,
                       errorMessage: String,
                       source: String,
                       sourceFaultDetail: SourceFaultDetail)

case class SourceFaultDetail(detail: Array[String])

object ErrorResponse {
  implicit val errorResponseFormat: OFormat[ErrorResponse] = Json.format[ErrorResponse]
}

object ErrorDetail {
  implicit val errorDetailFormat: OFormat[ErrorDetail] = Json.format[ErrorDetail]
}

object SourceFaultDetail {
  implicit val errorDetailFormat: OFormat[SourceFaultDetail] = Json.format[SourceFaultDetail]
}
