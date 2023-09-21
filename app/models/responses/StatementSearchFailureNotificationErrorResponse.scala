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
            correlationId: String,
            statementRequestID: Option[String] = None): StatementSearchFailureNotificationErrorResponse = {

    val aggregateErrorMsg = errors.getMessage
    val errorMsgList: Seq[String] = aggregateErrorMsg.split("\\),").toSeq

    val errorDetail = ErrorDetail(
      timestamp = currentDateTimeAsRFC7231(LocalDateTime.now()),
      correlationId = correlationId,
      errorCode = ErrorCode.code400,
      errorMessage = statementRequestID.fold(ErrorMessage.badRequestReceived)(_ => ErrorMessage.invalidStatementReqId),
      source = ErrorSource.cdsFinancials,
      sourceFaultDetail = SourceFaultDetail(
        statementRequestID.fold(errorMsgList)(stReqId => Seq(ErrorMessage.invalidStatementReqIdDetail(stReqId))))
    )

    StatementSearchFailureNotificationErrorResponse(errorDetail)
  }
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
  val missingReqProps = "missing required properties"
  val invalidStatementReqId = "Invalid statementRequestId"
  def invalidStatementReqIdDetail: String => String =
    statementReqId =>  s"statementRequestId : $statementReqId is not recognised"
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
