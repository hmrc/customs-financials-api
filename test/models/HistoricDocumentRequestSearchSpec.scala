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
import play.api.libs.json.{JsString, JsSuccess, Json}
import utils.SpecBase
import utils.TestData.{MONTH_2, MONTH_4, YEAR_2021}
import utils.Utils.emptyString

import java.util.UUID

class HistoricDocumentRequestSearchSpec extends SpecBase {

  "Json Reads" should {
    "result the correct output" in new Setup {

      import HistoricDocumentRequestSearch.historicDocumentRequestSearchFormat

      Json.fromJson(Json.parse(jsValue)) mustBe JsSuccess(histDocRequestSearch)
    }
  }

  "expireAtDateTimeFormat Reads" should {
    "create the date successfully if the date time is not in right format" in new Setup {

      import HistoricDocumentRequestSearch.expireAtDateTimeFormat

      Json.fromJson(JsString(emptyString)).isSuccess mustBe true
    }
  }

  "Json Writes" should {
    "result in correct output" in new Setup {
      Json.toJson(histDocRequestSearch) mustBe Json.parse(jsValue)
    }
  }

  "from" should {
    "return the correct HistoricDocumentRequestSearchSpec object" in new Setup {
      val actualHistDocReqSearch: HistoricDocumentRequestSearch =
        HistoricDocumentRequestSearch.from(historicDocumentRequests, currentEori)
      val expected: HistoricDocumentRequestSearch               = histDocRequestSearch

      actualHistDocReqSearch.resultsFound mustBe expected.resultsFound
      actualHistDocReqSearch.params mustBe expected.params
      actualHistDocReqSearch.searchRequests.size mustBe expected.searchRequests.size
      actualHistDocReqSearch.searchRequests.exists(req => req.eoriNumber == "GB123456789012") mustBe true
      actualHistDocReqSearch.searchRequests.exists(req => req.eoriNumber == "GB234567890121") mustBe true
      actualHistDocReqSearch.searchRequests.exists(req =>
        req.searchSuccessful == SearchResultStatus.inProcess
      ) mustBe true

      actualHistDocReqSearch.searchRequests.exists(req => req.failureRetryCount == 0) mustBe true
    }
  }

  trait Setup {

    val searchID: UUID                         = UUID.randomUUID()
    val userId: String                         = "test_userId"
    val resultsFound: SearchResultStatus.Value = SearchResultStatus.inProcess
    val searchStatusUpdateDate: String         = emptyString
    val currentEori: String                    = "GB123456789012"
    val params: Params                         = Params("02", "2021", "04", "2021", "DutyDefermentStatement", "1234567")

    val searchRequests: Set[SearchRequest] = Set(
      SearchRequest(
        "GB123456789012",
        "5b89895-f0da-4472-af5a-d84d340e7mn5",
        SearchResultStatus.inProcess,
        emptyString,
        emptyString,
        0
      ),
      SearchRequest(
        "GB234567890121",
        "5c79895-f0da-4472-af5a-d84d340e7mn6",
        SearchResultStatus.inProcess,
        emptyString,
        emptyString,
        0
      )
    )

    val histDocRequestSearch: HistoricDocumentRequestSearch =
      HistoricDocumentRequestSearch(searchID, resultsFound, searchStatusUpdateDate, currentEori, params, searchRequests)

    val historicDocumentRequests: Set[HistoricDocumentRequest] = Set(
      HistoricDocumentRequest(
        EORI("GB123456789012"),
        FileRole("DutyDefermentStatement"),
        YEAR_2021,
        MONTH_2,
        YEAR_2021,
        MONTH_4,
        Some("1234567")
      ),
      HistoricDocumentRequest(
        EORI("GB234567890121"),
        FileRole("DutyDefermentStatement"),
        YEAR_2021,
        MONTH_2,
        YEAR_2021,
        MONTH_4,
        Some("1234567")
      )
    )

    val jsValue: String =
      s"""{"searchID": "$searchID",
         |"resultsFound": "inProcess",
         |"searchStatusUpdateDate": "",
         |"currentEori": "GB123456789012",
         |"params": {
         |"periodStartMonth": "02",
         |"periodStartYear": "2021",
         |"periodEndMonth": "04",
         |"periodEndYear": "2021",
         |"accountType": "DutyDefermentStatement",
         |"dan": "1234567"
         |},
         |"searchRequests":[
         |{
         |"eoriNumber": "GB123456789012",
         |"statementRequestId": "5b89895-f0da-4472-af5a-d84d340e7mn5",
         |"searchSuccessful": "inProcess",
         |"searchDateTime": "",
         |"searchFailureReasonCode": "",
         |"failureRetryCount": 0
         |},
         |{
         |"eoriNumber": "GB234567890121",
         |"statementRequestId": "5c79895-f0da-4472-af5a-d84d340e7mn6",
         |"searchSuccessful": "inProcess",
         |"searchDateTime": "",
         |"searchFailureReasonCode": "",
         |"failureRetryCount": 0
         |}
         |]
         |}""".stripMargin
  }
}
