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
  import ErrorDetail.errorDetailsFormat
  implicit val ssfnErrorResponseFormat: OFormat[StatementSearchFailureNotificationErrorResponse] =
    Json.format[StatementSearchFailureNotificationErrorResponse]

  def apply(errors: Throwable,
            correlationId: String): StatementSearchFailureNotificationErrorResponse = {

    StatementSearchFailureNotificationErrorResponse(ErrorDetail(
      timestamp = currentDateTimeAsRFC7231(LocalDateTime.now()),
      correlationId = correlationId,
      errorCode = "400",
      errorMessage = "Bad request received",
      sourceFaultDetail = SourceFaultDetail(Seq("BadRequest"))
    ))
  }
}

case class ErrorDetail(timestamp: String,
                       correlationId: String,
                       errorCode: String,
                       errorMessage: String,
                       source: String = "CDS Financials",
                       sourceFaultDetail: SourceFaultDetail)

object ErrorDetail {
  implicit val errorDetailsFormat: OFormat[ErrorDetail] = Json.format[ErrorDetail]
}

case class SourceFaultDetail(detail: Seq[String])

object SourceFaultDetail {
  implicit val errorDetailsFormat: OFormat[SourceFaultDetail] = Json.format[SourceFaultDetail]
}
