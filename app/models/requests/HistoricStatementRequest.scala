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

import models.HistoricalStatementRetrievalInterfaceMetadata
import play.api.libs.json.{JsValue, Json, OWrites, Writes}
import play.api.libs.ws.BodyWritable

case class HistoricStatementRequest(
  HistoricalStatementRetrievalInterfaceMetadata: HistoricalStatementRetrievalInterfaceMetadata
)

object HistoricStatementRequest {
  def from(historicDocumentRequest: HistoricDocumentRequest): HistoricStatementRequest =
    HistoricStatementRequest(
      HistoricalStatementRetrievalInterfaceMetadata(
        statementRequestID = historicDocumentRequest.statementRequestID.toString,
        eori = historicDocumentRequest.eori,
        statementType = historicDocumentRequest.documentType,
        periodStartYear = historicDocumentRequest.periodStartYear.toString,
        periodStartMonth = zeroPad(historicDocumentRequest.periodStartMonth),
        periodEndYear = historicDocumentRequest.periodEndYear.toString,
        periodEndMonth = zeroPad(historicDocumentRequest.periodEndMonth),
        DAN = historicDocumentRequest.dan
      )
    )

  implicit val historicalStatementRetrievalInterfaceMetadataWrites
    : OWrites[HistoricalStatementRetrievalInterfaceMetadata] =
    Json.writes[HistoricalStatementRetrievalInterfaceMetadata]

  implicit val HistoricStatementRequestWrites: OWrites[HistoricStatementRequest] = Json.writes[HistoricStatementRequest]

  implicit def jsonBodyWritable[T](implicit
    writes: Writes[T],
    jsValueBodyWritable: BodyWritable[JsValue]
  ): BodyWritable[T] = jsValueBodyWritable.map(writes.writes)

  private def zeroPad(value: Int): String = "%02d".format(value)
}
