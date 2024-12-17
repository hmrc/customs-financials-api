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

package models.requests

import models.{EORI, FileRole, HistoricDocumentRequestSearch, SearchRequest}
import play.api.libs.json.*

import java.util.UUID

case class HistoricDocumentRequest(
  eori: EORI,
  documentType: FileRole,
  periodStartYear: Int,
  periodStartMonth: Int,
  periodEndYear: Int,
  periodEndMonth: Int,
  dan: Option[String],
  statementRequestID: UUID = UUID.randomUUID()
)

object HistoricDocumentRequest {
  implicit val historicDocumentRequestFormat: OFormat[HistoricDocumentRequest] = Json.format[HistoricDocumentRequest]

  def apply(
    statementRequestID: String,
    histDocRequestSearch: HistoricDocumentRequestSearch
  ): HistoricDocumentRequest = {
    val searchReqForStatReqId: SearchRequest =
      histDocRequestSearch.searchRequests
        .find(sr => sr.statementRequestId == statementRequestID)
        .getOrElse(
          throw new RuntimeException(s"SearchRequest is not found for statementRequestId :: $statementRequestID")
        )

    val params = histDocRequestSearch.params

    HistoricDocumentRequest(
      eori = EORI(searchReqForStatReqId.eoriNumber),
      documentType = FileRole(params.accountType),
      periodStartYear = params.periodStartYear.toInt,
      periodStartMonth = params.periodStartMonth.toInt,
      periodEndYear = params.periodEndYear.toInt,
      periodEndMonth = params.periodEndMonth.toInt,
      dan = if (params.dan.isEmpty) None else Some(params.dan),
      statementRequestID = UUID.fromString(statementRequestID)
    )
  }
}
