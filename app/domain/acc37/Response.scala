/*
 * Copyright 2021 HM Revenue & Customs
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
