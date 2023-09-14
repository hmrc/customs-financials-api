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

package models.responses

import play.api.libs.json.{Json, OFormat}
import utils.Utils.currentDateTimeAsRFC7231

import java.time.LocalDateTime

case class StatementSearchFailureNotificationErrorResponse(errorDetail: ErrorDetail)

object StatementSearchFailureNotificationErrorResponse {
  implicit val ssfnErrorResponseFormat: OFormat[StatementSearchFailureNotificationErrorResponse] =
    Json.format[StatementSearchFailureNotificationErrorResponse]

  def apply(errors: Throwable,
            correlationId: String): StatementSearchFailureNotificationErrorResponse = {

    val defaultErrorDetail = ErrorDetail(
      timestamp = currentDateTimeAsRFC7231(LocalDateTime.now()),
      correlationId = correlationId,
      errorCode = ErrorCode.code400,
      errorMessage = ErrorMessage.invalidMessage,
      source = ErrorSource.jsonValidation,
      sourceFaultDetail = SourceFaultDetail(Seq("Invalid values"))
    )

    StatementSearchFailureNotificationErrorResponse(updateErrorDetails(errors, defaultErrorDetail))
  }

  private def updateErrorDetails(errors: Throwable,
                                 errorDetail: ErrorDetail): ErrorDetail = {
    val aggregateErrorMsg = errors.getMessage
    val errorMsgList: Seq[String] = aggregateErrorMsg.split("\\),").toSeq

    (aggregateErrorMsg, errorMsgList) match {
      case (agMsg, errorList) if isBothSchemaFieldsMissingError(agMsg, errorList) =>
        errorDetail.copy(
          errorMessage = ErrorMessage.badRequestReceived,
          source = ErrorSource.backEnd,
          sourceFaultDetail = SourceFaultDetail(errorMsgList))

      case (agMsg, errorList) if isSingleMandatoryFieldMissingError(agMsg, errorList) =>
        errorDetail.copy(sourceFaultDetail = SourceFaultDetail(errorMsgList))

      case (_, errorList) if errorList.size > 1 =>
        errorDetail.copy(
          errorMessage = ErrorMessage.badRequestReceived,
          source = ErrorSource.backEnd,
          sourceFaultDetail = SourceFaultDetail(errorMsgList))

      case _ => errorDetail.copy(sourceFaultDetail = SourceFaultDetail(errorMsgList))
    }
  }

  private def isBothSchemaFieldsMissingError(aggregateErrorMsg: String,
                                             errorList: Seq[String]): Boolean = {
    aggregateErrorMsg.contains("missing required properties") &&
      aggregateErrorMsg.contains("reason") &&
      aggregateErrorMsg.contains("statementRequestID") &&
      errorList.size == 1
  }

  private def isSingleMandatoryFieldMissingError(aggregateErrorMsg: String,
                                                 errorList: Seq[String]) =
    aggregateErrorMsg.contains("missing required properties") && errorList.size == 1
}

case class ErrorDetail(timestamp: String,
                       correlationId: String,
                       errorCode: String,
                       errorMessage: String,
                       source: String = ErrorSource.cdsFinancials,
                       sourceFaultDetail: SourceFaultDetail)

object ErrorDetail {
  implicit val errorDetailsFormat: OFormat[ErrorDetail] = Json.format[ErrorDetail]
}

case class SourceFaultDetail(detail: Seq[String])

object SourceFaultDetail {
  implicit val errorDetailsFormat: OFormat[SourceFaultDetail] = Json.format[SourceFaultDetail]
}

object ErrorMessage {
  val invalidMessage = "Invalid message"
  val badRequestReceived = "Bad request received"
}

object ErrorSource {
  val jsonValidation = "JSON validation"
  val backEnd = "Backend"
  val cdsFinancials = "CDS Financials"
}

object ErrorCode {
  val code400 = "400"
  val code500 = "500"
}
