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

import models.requests.{HistoricDocumentRequest, HistoricStatementRequest}
import utils.SpecBase
import utils.TestData.{MONTH_2, MONTH_4, YEAR_2021}

import java.util.UUID

class HistoricStatementRequestSpec extends SpecBase {

  "from" should {
    "create HistoricStatementRequest object correctly" in new Setup {
      HistoricStatementRequest.from(histDocRequest) mustBe histStatementReq
    }
  }

  trait Setup {
    val statementRequestID: UUID = UUID.randomUUID()
    val eori: EORI               = EORI("test_eori")
    val fileRole: FileRole       = FileRole("PostponedVATStatement")

    val histStatRetIntMetadata: HistoricalStatementRetrievalInterfaceMetadata =
      HistoricalStatementRetrievalInterfaceMetadata(
        statementRequestID.toString,
        eori,
        fileRole,
        "2021",
        "02",
        "2021",
        "04",
        None
      )

    val histDocRequest: HistoricDocumentRequest =
      HistoricDocumentRequest(eori, fileRole, YEAR_2021, MONTH_2, YEAR_2021, MONTH_4, None, statementRequestID)

    val histStatementReq: HistoricStatementRequest = HistoricStatementRequest(histStatRetIntMetadata)
  }
}
