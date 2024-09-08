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
            errorCode: String = ErrorCode.code400,
            correlationId: String,
            statementRequestID: Option[String] = None,
            errorDetailMsg: String = emptyString): StatementSearchFailureNotificationErrorResponse = {

    val aggregateErrorMsg = errors.fold[Throwable](
      new BadRequestException(ErrorMessage.badRequestReceived))(identity).getMessage

    val errorMsgList: Seq[String] = formatAggregateErrorMsgForErrorResponse(aggregateErrorMsg)

    val errorDetail = ErrorDetail(
      timestamp = currentDateTimeAsRFC7231(LocalDateTime.now()),
      correlationId = correlationId,
      errorCode = errorCode,
      errorMessage = statementRequestID.fold(ErrorMessage.badRequestReceived)(_ =>
        if (errorCode == ErrorCode.code500) ErrorMessage.technicalError else ErrorMessage.invalidStatementReqId),
      source = ErrorSource.cdsFinancials,
      sourceFaultDetail = SourceFaultDetail(
        retrieveErrorMsgList(errorCode, statementRequestID, errorDetailMsg, errorMsgList))
    )

    StatementSearchFailureNotificationErrorResponse(errorDetail)
  }

  private def retrieveErrorMsgList(errorCode: String,
                                   statementRequestID: Option[String],
                                   errorDetailMsg: String,
                                   errorMsgList: Seq[String]): Seq[String] = {
    statementRequestID.fold(errorMsgList)(stReqId => Seq(
      if (errorCode == ErrorCode.code500) {
        checkErrorDetailsMsg(errorDetailMsg, stReqId)
      } else {
        ErrorMessage.invalidStatementReqIdDetail(stReqId)
      })
    )
  }

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
          } else {
            strAfterQuotesReplacement.substring(1, strAfterQuotesReplacement.length - 1)
          }
        }
      }
    } else {
      Seq(aggregateErrorMsg)
    }
  }

  private def checkErrorDetailsMsg(errorDetailMsg: String,
                                   stReqId: String): String = {
    if (errorDetailMsg.isEmpty) {
      ErrorMessage.technicalErrorDetail(stReqId)
    }
    else {
      errorDetailMsg
    }
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

case class ErrorDetailContainer(errorDetail: ErrorDetail)

object ErrorDetailContainer {
  implicit val format: OFormat[ErrorDetailContainer] = Json.format[ErrorDetailContainer]
}

case class SourceFaultDetail(detail: Seq[String])

object SourceFaultDetail {
  implicit val errorDetailsFormat: OFormat[SourceFaultDetail] = Json.format[SourceFaultDetail]
}

object ErrorMessage {
  val badRequestReceived = "Bad request received"
  val invalidStatementReqId = "Invalid statementRequestId"
  val technicalError = "Technical error"

  def invalidStatementReqIdDetail: String => String =
    statementReqId => s"statementRequestId : $statementReqId is not recognised"

  def technicalErrorDetail: String => String =
    statementReqId => s"Technical error occurred while processing the statementRequestId : $statementReqId"

  def failureRetryCountErrorDetail: String => String =
    statementReqId =>
      s"Failure retry count has reached its maximum permitted value for statementRequestId : $statementReqId"
}

object ErrorSource {
  val cdsFinancials = "CDS Financials"
  val backEnd = "Backend"
  val etmp = "ETMP"
  val mdtp = "MDTP"
}

object ErrorCode {
  val code400 = "400"
  val code500 = "500"
}

object EtmpErrorCode {
  val code001 = "001"
  val code002 = "002"
  val code003 = "003"
  val code004 = "004"
  val code005 = "005"

  val INVALID_CASH_ACCOUNT_STATUS_TEXT = "001-Invalid Cash Account"
}

object SourceFaultDetailMsg {
  val REQUEST_SCHEMA_VALIDATION_ERROR = "Failure while validating request against schema"
  val SUCCESS_RESPONSE_SCHEMA_VALIDATION_ERROR = "Failure while validating response against schema"
  val ETMP_FAILURE = "Failure while calling ETMP"
  val BACK_END_FAILURE = "Failure in backend System"
  val SERVER_CONNECTION_ERROR = "Error connecting to the server"
}
