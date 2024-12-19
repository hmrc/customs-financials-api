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

import utils.SpecBase

class HistoricalStatementRetrievalInterfaceMetadataSpec extends SpecBase {

  "toString" should {

    "return correct output" in {

      val eoriNumber       = EORI("test_eori")
      val statReqId        = "test_statReqId"
      val statementType    = FileRole("import_vat")
      val periodStartYear  = "2024"
      val periodStartMonth = "2"
      val periodEndYear    = "2024"
      val periodEndMonth   = "4"
      val dan              = Some("12345678")

      val ob = HistoricalStatementRetrievalInterfaceMetadata(
        statementRequestID = statReqId,
        eori = eoriNumber,
        statementType = statementType,
        periodStartYear = periodStartYear,
        periodStartMonth = periodStartMonth,
        periodEndYear = periodEndYear,
        periodEndMonth = periodEndMonth,
        DAN = dan
      )

      ob.toString mustBe
        s"HistoricalStatementRetrievalInterfaceMetadata(statementRequestID:" +
        s" $statReqId, eori: xxx, statementType: $statementType, periodStartYear:" +
        s" $periodStartYear, periodStartMonth: $periodStartMonth, periodEndYear:" +
        s" $periodEndYear, periodEndMonth: $periodEndMonth, DAN: $dan"
    }
  }
}
