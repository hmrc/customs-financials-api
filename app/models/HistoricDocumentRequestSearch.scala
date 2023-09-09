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

package models

import models.requests.HistoricDocumentRequest
import play.api.libs.json.{Json, OFormat}
import utils.Utils.emptyString

import java.util.UUID

case class HistoricDocumentRequestSearch(searcID: UUID,
                                         userId: String,
                                         resultsFound: String,
                                         searchStatusUpdateDate: String = emptyString,
                                         currentEori: String,
                                         params: Params,
                                         searchRequests: Seq[SearchRequest]) {
  require(List("yes","no","inProcess").contains(resultsFound),  "resultsFound should have a valid value")
}

object HistoricDocumentRequestSearch {
  implicit val historicDocumentRequestSearchFormat: OFormat[HistoricDocumentRequestSearch] =
    Json.format[HistoricDocumentRequestSearch]

  def from(historicDocumentRequests: Set[HistoricDocumentRequest]): HistoricDocumentRequestSearch = {

    HistoricDocumentRequestSearch(
      UUID.randomUUID(),
      FileRole(""),
      2021,
      2,
      2021,
      3,
      Some(""))
  }
}
