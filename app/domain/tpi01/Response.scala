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

package domain.tpi01

import models.{AccountNumber, AccountType, EORI, EmailAddress}
import play.api.libs.json._

case class Response(getCorrespondenceAddressResponse: GetReimbursementClaimsResponse){
  val mdtpError: Boolean = getCorrespondenceAddressResponse
    .responseCommon
    .returnParameters.exists(_.exists(_.paramName == "POSITION"))
}

object Response {
  implicit val format: OFormat[Response] = Json.format[Response]
}

case class GetReimbursementClaimsResponse(responseCommon: ResponseCommon,
                                          responseDetail: Option[ResponseDetail])

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

case class ResponseDetail(cdfPayClaimsFound: Boolean,
                          cdfPayCases: Option[Array[cdfPayCase]])

object ResponseDetail {
  implicit val format: OFormat[ResponseDetail] = Json.format[ResponseDetail]
}

case class cdfPayCase(cdfPayCaseNumber: String,
                      cdfPayService: String,
                      caseStatus: String,
                      declarantEORI: String,
                      importerEORI: String,
                      claimantEORI: Option[String],
                      claimAmountTotal: Option[String],
                      totalCaseReimburseAmnt: Option[String])

object cdfPayCase {
  implicit val format: OFormat[cdfPayCase] = Json.format[cdfPayCase]
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


