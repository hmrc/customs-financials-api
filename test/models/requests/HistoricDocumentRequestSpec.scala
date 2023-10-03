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

import models._
import utils.SpecBase
import utils.Utils.emptyString

import java.util.UUID

class HistoricDocumentRequestSpec extends SpecBase {

  "apply" should {
    "create the object correctly" in new Setup {
      HistoricDocumentRequest(incomingStatementReqId, historicDocumentRequestSearchDoc) mustBe
        HistoricDocumentRequest(
          eori = EORI(eoriNumber),
          documentType = FileRole(accountType),
          periodStartMonth = 2,
          periodStartYear = 2021,
          periodEndMonth = 4,
          periodEndYear = 2021,
          dan = Some(dan),
          statementRequestID = UUID.fromString(incomingStatementReqId)
        )
    }
  }

  trait Setup {
    val incomingStatementReqId: String = UUID.randomUUID().toString
    val eoriNumber = "GB123456789012"
    val accountType = "DutyDefermentStatement"
    val dan = "1234567"
    val searchID: UUID = UUID.randomUUID()
    val resultsFound: SearchResultStatus.Value = SearchResultStatus.inProcess
    val searchStatusUpdateDate: String = emptyString
    val currentEori: String = "GB123456789012"
    val params: Params = Params("2", "2021", "4", "2021", "DutyDefermentStatement", dan)
    val searchRequests: Set[SearchRequest] = Set(
      SearchRequest(
        eoriNumber, incomingStatementReqId, SearchResultStatus.inProcess, emptyString, emptyString, 0),
      SearchRequest(
        "GB234567890121", "5c79895-f0da-4472-af5a-d84d340e7mn6", SearchResultStatus.inProcess,
        emptyString, emptyString, 0)
    )

    val historicDocumentRequestSearchDoc: HistoricDocumentRequestSearch =
      HistoricDocumentRequestSearch(searchID,
        resultsFound,
        searchStatusUpdateDate,
        currentEori,
        params,
        searchRequests)
  }
}
