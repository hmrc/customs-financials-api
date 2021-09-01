/*
 * Copyright 2021 HM Revenue & Customs
 *
 */

package models.requests

import models.HistoricalStatementRetrievalInterfaceMetadata
import play.api.libs.json.Json

import java.util.UUID

case class HistoricStatementRequest(HistoricalStatementRetrievalInterfaceMetadata: HistoricalStatementRetrievalInterfaceMetadata)

object HistoricStatementRequest {
  def from(historicDocumentRequest: HistoricDocumentRequest, requestId: UUID): HistoricStatementRequest = {
    HistoricStatementRequest(
      HistoricalStatementRetrievalInterfaceMetadata(
        statementRequestID = requestId.toString,
        eori = historicDocumentRequest.eori,
        statementType = historicDocumentRequest.documentType,
        periodStartYear = historicDocumentRequest.periodStartYear.toString,
        periodStartMonth = zeroPad(historicDocumentRequest.periodStartMonth),
        periodEndYear = historicDocumentRequest.periodEndYear.toString,
        periodEndMonth = zeroPad(historicDocumentRequest.periodEndMonth),
        DAN = historicDocumentRequest.dan
      )
    )
  }

  implicit val historicalStatementRetrievalInterfaceMetadataWrites = Json.writes[HistoricalStatementRetrievalInterfaceMetadata]
  implicit val HistoricStatementRequestWrites = Json.writes[HistoricStatementRequest]

  private def zeroPad(value: Int): String = "%02d".format(value)
}