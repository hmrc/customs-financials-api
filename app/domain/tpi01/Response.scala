/*
 * Copyright 2022 HM Revenue & Customs
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

package domain.tpi01

import play.api.libs.json._

case class Response(getPostClearanceCasesResponse: GetReimbursementClaimsResponse)

object Response {
  implicit val format: OFormat[Response] = Json.format[Response]
}

case class GetReimbursementClaimsResponse(responseCommon: ResponseCommon,
                                          responseDetail: Option[ResponseDetail]){
  val mdtpError: Boolean = responseCommon
    .returnParameters.exists(_.exists(_.paramName == "POSITION"))
}

object GetReimbursementClaimsResponse {
  implicit val format: OFormat[GetReimbursementClaimsResponse] = Json.format[GetReimbursementClaimsResponse]
}

case class ReturnParameter(paramName: String, paramValue: String)

object ReturnParameter {
  implicit val format: OFormat[ReturnParameter] = Json.format[ReturnParameter]
}

case class ResponseCommon(status: String,
                          processingDate: String,
                          correlationId: Option[String],
                          errorMessage: Option[String],
                          returnParameters: Option[List[ReturnParameter]])

object ResponseCommon {
  implicit val format: OFormat[ResponseCommon] = Json.format[ResponseCommon]
}

case class ResponseDetail(NDRCCasesFound: Boolean,
                          SCTYCasesFound: Boolean,
                          CDFPayCase: Option[CDFPayCase]) {

  def generateClaimsResponse: JsObject = {
    val scty = CDFPayCase.flatMap(_.SCTYCases).getOrElse(Seq.empty)
    val ndrc = CDFPayCase.flatMap(_.NDRCCases).getOrElse(Seq.empty)

    Json.obj("claims" ->
      Json.obj("sctyClaims" -> scty,
        "ndrcClaims" -> ndrc))
  }
}

object ResponseDetail {
  implicit val format: OFormat[ResponseDetail] = Json.format[ResponseDetail]
}

case class CDFPayCase(NDRCCaseTotal: Option[String],
                      NDRCCases: Option[Seq[NDRCCaseDetails]],
                      SCTYCaseTotal: Option[String],
                      SCTYCases: Option[Seq[SCTYCaseDetails]])

object CDFPayCase {
  implicit val format: OFormat[CDFPayCase] = Json.format[CDFPayCase]
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
