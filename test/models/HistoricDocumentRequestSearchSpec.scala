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
import utils.SpecBase

class HistoricDocumentRequestSearchSpec extends SpecBase {
"from" should {
  "return the correct HistoricDocumentRequestSearchSpec object" in {

    val historicDocumentRequests = Set(
      HistoricDocumentRequest(EORI("GB123456789012"), FileRole(""), 2021, 2, 2021, 4, Some("1234567")),
      HistoricDocumentRequest(EORI("GB234567890121"), FileRole(""), 2021, 2, 2021, 4, Some("1234567")))

    val actualHistDocReqSearch = HistoricDocumentRequestSearch.from(historicDocumentRequests)
    val expected = HistoricDocumentRequestSearch()

    actualHistDocReqSearch.userId mustBe expected.userId
    actualHistDocReqSearch.resultsFound mustBe expected.resultsFound
    actualHistDocReqSearch.params mustBe expected.params
    actualHistDocReqSearch.searchRequests.size mustBe expected.searchRequests.size
    actualHistDocReqSearch.searchRequests mustBe expected.searchRequests
  }
}
}
