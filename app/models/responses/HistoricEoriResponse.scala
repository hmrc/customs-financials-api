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
