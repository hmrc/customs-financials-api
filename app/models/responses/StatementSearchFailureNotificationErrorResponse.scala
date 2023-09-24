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
import uk.gov.hmrc.http.BadRequestException
import utils.Utils.{currentDateTimeAsRFC7231, emptyString, threeColons}

import java.time.LocalDateTime

case class StatementSearchFailureNotificationErrorResponse(errorDetail: ErrorDetail)

object StatementSearchFailureNotificationErrorResponse {
  implicit val ssfnErrorResponseFormat: OFormat[StatementSearchFailureNotificationErrorResponse] =
    Json.format[StatementSearchFailureNotificationErrorResponse]

  def apply(errors: Option[Throwable] = None,
            correlationId: String,
            statementRequestID: Option[String] = None): StatementSearchFailureNotificationErrorResponse = {

    val aggregateErrorMsg = errors.fold[Throwable](
      new BadRequestException(ErrorMessage.badRequestReceived))(identity).getMessage

    val errorMsgList: Seq[String] = formatAggregateErrorMsgForErrorResponse(aggregateErrorMsg)

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

  /**
   * Formats the aggregate error msg into Seq of error msgs for individual fields
   * for error response
   */
  private def formatAggregateErrorMsgForErrorResponse(aggregateErrorMsg: String): Seq[String] = {
    val leftParenthesis = "("
    val parenWithColonSpace = "(: "
    val doubleQuotes = "\""

    if (aggregateErrorMsg.nonEmpty) {
      aggregateErrorMsg.split(threeColons).toSeq.map {
        msgStr => {
          val strAfterQuotesReplacement = msgStr.replace(doubleQuotes, emptyString)

          if (strAfterQuotesReplacement.startsWith(parenWithColonSpace)) {
            val strAfterParenReplacement = strAfterQuotesReplacement.replace(parenWithColonSpace, leftParenthesis)
            strAfterParenReplacement.substring(1, strAfterParenReplacement.length - 1)
          } else
            strAfterQuotesReplacement.substring(1, strAfterQuotesReplacement.length - 1)
        }
      }
    } else Seq(aggregateErrorMsg)
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
