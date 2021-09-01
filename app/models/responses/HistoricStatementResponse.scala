/*
 * Copyright 2021 HM Revenue & Customs
 *
 */

package models.responses

import models.{HistoricStatementErrorDetail, HistoricStatementSourceFaultDetail}
import play.api.libs.json.Json

case class HistoricStatementResponse(errorDetail: Option[HistoricStatementErrorDetail])

object HistoricStatementResponse {
  implicit val HistoricStatementSourceFaultDetailReads = Json.reads[HistoricStatementSourceFaultDetail]
  implicit val HistoricStatementErrorDetailReads = Json.reads[HistoricStatementErrorDetail]
  implicit val HistoricStatementResponseReads = Json.reads[HistoricStatementResponse]
}