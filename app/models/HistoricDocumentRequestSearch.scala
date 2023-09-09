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

case class HistoricDocumentRequestSearch(searchID: UUID,
                                         userId: String,
                                         resultsFound: String,
                                         searchStatusUpdateDate: String = emptyString,
                                         currentEori: String,
                                         params: Params,
                                         searchRequests: Set[SearchRequest]) {
  require(List("yes", "no", "inProcess").contains(resultsFound), "resultsFound should have a valid value")
  require(searchRequests.nonEmpty, "searchRequests is empty")
}

object HistoricDocumentRequestSearch {
  implicit val historicDocumentRequestSearchFormat: OFormat[HistoricDocumentRequestSearch] =
    Json.format[HistoricDocumentRequestSearch]

  def from(historicDocumentRequests: Set[HistoricDocumentRequest],
           userId: String,
           requestEori: String): HistoricDocumentRequestSearch = {

    val headHistDocRequest = historicDocumentRequests.head
    val params = Params(
      headHistDocRequest.periodStartMonth.toString,
      headHistDocRequest.periodStartYear.toString,
      headHistDocRequest.periodEndMonth.toString,
      headHistDocRequest.periodEndYear.toString,
      headHistDocRequest.documentType.value,
      headHistDocRequest.dan.getOrElse(emptyString))

    val searchDocReqs = historicDocumentRequests.map {
      histDoc =>
        SearchRequest(
          histDoc.eori.value,
          UUID.randomUUID().toString,
          "inProcess",
          emptyString,
          emptyString,
          0)
    }

    HistoricDocumentRequestSearch(
      UUID.randomUUID(),
      userId,
      "inProcess",
      emptyString,
      requestEori,
      params,
      searchDocReqs)
  }
}
