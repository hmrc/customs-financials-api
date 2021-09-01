/*
 * Copyright 2021 HM Revenue & Customs
 *
 */

package models.responses

import models.EORI
import play.api.libs.json.Json

case class HistoricEoriResponse(getEORIHistoryResponse: GetEORIHistoryResponse)

case class GetEORIHistoryResponse(responseCommon: EORIHistoryResponseCommon,
                                  responseDetail: EORIHistoryResponseDetail)

case class EORIHistoryResponseCommon(status: String,
                                     processingDate: String)

case class EORIHistoryResponseDetail(EORIHistory: Seq[EORIHistory])

case class EORIHistory(EORI: EORI,
                       validFrom: Option[String],
                       validUntil: Option[String])

object HistoricEoriResponse {
  implicit val eoriHistoryFormat = Json.format[EORIHistory]
  implicit val responseDetailFormat = Json.format[EORIHistoryResponseDetail]
  implicit val responseCommonFormat = Json.format[EORIHistoryResponseCommon]
  implicit val getEORIHistoryResponseFormat = Json.format[GetEORIHistoryResponse]
  implicit val historicEoriResponseFormat = Json.format[HistoricEoriResponse]
}
