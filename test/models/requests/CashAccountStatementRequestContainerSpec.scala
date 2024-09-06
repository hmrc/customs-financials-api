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

import play.api.libs.json.Json
import utils.SpecBase
import CashAccountStatementRequestContainer.cashAccountStatementRequestContainerFormat

class CashAccountStatementRequestContainerSpec extends SpecBase {

  "CashAccountStatementRequest Reads" should {

    "read the JsValue and create the object correctly" in new Setup {
      Json.fromJson(Json.parse(validJson)).get mustBe casRequest
    }

    "generate error for incorrect JsValue" in new Setup {
      Json.fromJson(Json.parse(incorrectJsValue)).isError mustBe true
    }
  }

  trait Setup {

    val casRequestCommon = CashAccountStatementRequestCommon(
      "MDTP", "2021-12-17T09:30:47Z", "601bb176b8e411ed8a9800001e3b1802")

    val casRequestDetail = CashAccountStatementRequestDetail(
      "GB123456789012345", "98765432103", "2024-05-10", "2024-05-20")

    val casRequest = CashAccountStatementRequestContainer(
      CashAccountStatementRequest(casRequestCommon, casRequestDetail))

    val validJson: String =
      """
        |{
        |  "cashAccountStatementRequest": {
        |    "requestCommon": {
        |      "originatingSystem": "MDTP",
        |      "receiptDate": "2021-12-17T09:30:47Z",
        |      "acknowledgementReference": "601bb176b8e411ed8a9800001e3b1802"
        |    },
        |    "requestDetail": {
        |      "eori": "GB123456789012345",
        |      "can": "98765432103",
        |      "dateFrom": "2024-05-10",
        |      "dateTo": "2024-05-20"
        |    }
        |  }
        |}""".stripMargin

    val incorrectJsValue: String =
      """
        |{
        |  "cashAccountStatementRequest": {
        |    "requestCommon": {
        |      "originatingSystem": "MDTP",
        |      "receiptDate": "2021-12-17T09:30:47Z",
        |      "acknowledgementReference": "601bb176b8e411ed8a9800001e3b1802"
        |    },
        |    "requestDetail": {
        |      "eori": "GB123456789012345",
        |      "dateFrom": "2024-05-10",
        |      "dateTo": "2024-05-20"
        |    }
        |  }
        |}""".stripMargin
  }
}
