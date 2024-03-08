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

import config.MetaConfig.Platform.EXPIRE_TIME_STAMP_SECONDS
import models.requests.HistoricDocumentRequest
import play.api.libs.json._
import uk.gov.hmrc.mongo.play.json.formats.MongoJavatimeFormats
import utils.Utils.{emptyString, zeroPad}

import java.time.temporal.ChronoUnit
import java.time.{Instant, LocalDateTime, ZoneOffset}
import java.util.UUID

case class HistoricDocumentRequestSearch(searchID: UUID,
                                         resultsFound: SearchResultStatus.SearchResultStatus,
                                         searchStatusUpdateDate: String = emptyString,
                                         currentEori: String,
                                         params: Params,
                                         searchRequests: Set[SearchRequest],
                                         expireAt: Option[LocalDateTime] = None) {
  require(searchRequests.nonEmpty, "searchRequests is empty")
}

object HistoricDocumentRequestSearch {
  implicit val dateFormats: Format[Instant] = MongoJavatimeFormats.instantFormat

  implicit val expireAtDateTimeFormat: Format[LocalDateTime] = Format[LocalDateTime](
    Reads[LocalDateTime](js =>
      js.validate[Long] match {
        case JsSuccess(epoc, _) => JsSuccess(Instant.ofEpochMilli(epoc).atOffset(ZoneOffset.UTC).toLocalDateTime)
        case _ =>
          JsSuccess(Instant.now()
            .plus(EXPIRE_TIME_STAMP_SECONDS, ChronoUnit.SECONDS).atOffset(ZoneOffset.UTC).toLocalDateTime)
      }
    ),
    Writes[LocalDateTime](d =>
      JsNumber(d.toInstant(ZoneOffset.UTC).toEpochMilli)
    )
  )

  implicit val historicDocumentRequestSearchFormat: OFormat[HistoricDocumentRequestSearch] =
    Json.format[HistoricDocumentRequestSearch]

  def from(historicDocumentRequests: Set[HistoricDocumentRequest],
           requestEori: String): HistoricDocumentRequestSearch = {

    val headHistDocRequest = historicDocumentRequests.head
    val params = Params(
      zeroPad(headHistDocRequest.periodStartMonth),
      headHistDocRequest.periodStartYear.toString,
      zeroPad(headHistDocRequest.periodEndMonth),
      headHistDocRequest.periodEndYear.toString,
      headHistDocRequest.documentType.value,
      headHistDocRequest.dan.getOrElse(emptyString))

    val searchDocReqs = historicDocumentRequests.map {
      histDoc =>
        SearchRequest(
          histDoc.eori.value,
          histDoc.statementRequestID.toString,
          SearchResultStatus.inProcess,
          emptyString,
          emptyString,
          0)
    }

    HistoricDocumentRequestSearch(
      UUID.randomUUID(),
      SearchResultStatus.inProcess,
      emptyString,
      requestEori,
      params,
      searchDocReqs)
  }
}
