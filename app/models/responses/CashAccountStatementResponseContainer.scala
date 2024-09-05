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

case class CashAccountStatementResponseContainer(cashAccountStatementResponse: CashAccountStatementResponse)

object CashAccountStatementResponseContainer {
  implicit val cashAccountStatementResponseWrapperFormat: OFormat[CashAccountStatementResponseContainer] =
    Json.format[CashAccountStatementResponseContainer]
}

case class CashAccountStatementResponse(responseCommon: Acc45ResponseCommon)

object CashAccountStatementResponse {
  implicit val cashAccountStatementResponseFormat: OFormat[CashAccountStatementResponse] =
    Json.format[CashAccountStatementResponse]
}

case class Acc45ResponseCommon(status: String,
                               statusText: Option[String],
                               processingDate: String,
                               returnParameters: Option[Seq[ReturnParameter]])

object Acc45ResponseCommon {
  implicit val acc45ResponseCommonFormat: OFormat[Acc45ResponseCommon] = Json.format[Acc45ResponseCommon]
}

case class ReturnParameter(paramName: String, paramValue: String)

object ReturnParameter {
  implicit val returnParameterFormat: OFormat[ReturnParameter] = Json.format[ReturnParameter]
}

case class CashAccountStatementErrorResponse(errorDetail: ErrorDetail)

object CashAccountStatementErrorResponse {
  implicit val cashAccountStatementErrorResponseFormat: OFormat[CashAccountStatementErrorResponse] =
    Json.format[CashAccountStatementErrorResponse]
}