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

import play.api.libs.json.{JsSuccess, Json}
import utils.SpecBase
import utils.Utils.emptyString

class SearchRequestSpec extends SpecBase {
  "object should be created" should {
    "for correct parameter values" in {
      val srOb = SearchRequest("GB123456789012",
        "5b89895-f0da-4472-af5a-d84d340e7mn5",
        "inProcess",
        emptyString,
        emptyString,
        0)

      srOb.failureRetryCount mustBe 0
    }
  }

  "Exception must be thrown" should {
    "for incorrect parameter values" in {
      intercept[RuntimeException] {
        SearchRequest("GB123456789012",
          "5b89895-f0da-4472-af5a-d84d340e7mn5",
          "inProcess",
          emptyString,
          emptyString,
          6)
      }
    }
  }

  "Json Reads" should {
    "result the correct output" in {
      import SearchRequest.searchRequestFormat

      val srOb = SearchRequest("GB123456789012",
        "5b89895-f0da-4472-af5a-d84d340e7mn5",
        "inProcess",
        emptyString,
        emptyString,
        0)

      val jsValue =
        """{"eoriNumber": "GB123456789012",
          |"statementRequestId": "5b89895-f0da-4472-af5a-d84d340e7mn5",
          |"searchSuccessful": "inProcess",
          |"searchDateTime": "",
          |"searchFailureReasonCode": "","failureRetryCount": 0}""".stripMargin
      Json.fromJson(Json.parse(jsValue)) mustBe JsSuccess(srOb)
    }
  }

  "Json Writes" should {
    "result in correct output" in {
      val srOb = SearchRequest("GB123456789012",
        "5b89895-f0da-4472-af5a-d84d340e7mn5",
        "inProcess",
        emptyString,
        emptyString,
        0)

      val jsValue =
        """{"eoriNumber": "GB123456789012",
          |"statementRequestId": "5b89895-f0da-4472-af5a-d84d340e7mn5",
          |"searchSuccessful": "inProcess",
          |"searchDateTime": "",
          |"searchFailureReasonCode": "",
          |"failureRetryCount": 0}""".stripMargin

      Json.toJson(srOb) mustBe Json.parse(jsValue)
    }
  }
}
