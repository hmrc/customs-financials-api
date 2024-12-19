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

class StatementSearchFailureNotificationMetadataSpec extends SpecBase {

  "Json Reads" should {

    "generate the correct output" in new Setup {
      import StatementSearchFailureNotificationMetadata.ssfnMetaDataFormats

      Json.fromJson(Json.parse(jsValue)) mustBe JsSuccess(ssfnMetaDataOb)
    }

    "generate error for incorrect JsValue" in new Setup {
      import StatementSearchFailureNotificationMetadata.ssfnMetaDataFormats

      Json.fromJson(Json.parse(incorrectJsValue)).isError mustBe true
    }
  }

  "Json Writes" should {
    "generate the correct output" in new Setup {
      Json.toJson(ssfnMetaDataOb) mustBe Json.parse(jsValue)
    }
  }

  trait Setup {
    val jsValue: String =
      """{
        | "statementRequestID": "3jh1f6b3-f8b1-4f3c-973a-05b4720e64e1",
        |  "reason": "NoDocumentsFound"
        |}""".stripMargin

    val incorrectJsValue: String =
      """{
        | "statementID": "3jh1f6b3-f8b1-4f3c-973a-05b4720e64e1",
        |  "reason": "NoDocumentsFound"
        |}""".stripMargin

    val ssfnMetaDataOb: StatementSearchFailureNotificationMetadata =
      StatementSearchFailureNotificationMetadata("3jh1f6b3-f8b1-4f3c-973a-05b4720e64e1", "NoDocumentsFound")
  }
}
