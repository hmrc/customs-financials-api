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

package domain.acc40

import utils.SpecBase
import play.api.libs.json.{JsResultException, JsSuccess, Json}
import utils.TestData.BANK_ACCOUNT

class GeneralGuaranteeAccountSpec extends SpecBase {

  "format" should {
    "generate correct output for Json Reads" in new Setup {
      import GeneralGuaranteeAccount.format

      Json.fromJson(Json.parse(ggAccountObJsString)) mustBe JsSuccess(ggAccountOb)
    }

    "generate correct output for Json Writes" in new Setup {
      Json.toJson(ggAccountOb) mustBe Json.parse(ggAccountObJsString)
    }

    "throw exception for invalid Json" in {
      val invalidJson = "{ \"account\": \"300\", \"totalAmount\": \"600\" }"

      intercept[JsResultException] {
        Json.parse(invalidJson).as[GeneralGuaranteeAccount]
      }
    }
  }

  trait Setup {
    val accountOb: Account = Account(BANK_ACCOUNT, "CDS Cash", "test_owner")

    val ggAccountOb: GeneralGuaranteeAccount = GeneralGuaranteeAccount(accountOb, Some("100"))
    val ggAccountObJsString: String          =
      """{"account":{
        |"accountNumber":"1234567890987",
        |"accountType":"CDS Cash",
        |"accountOwner":"test_owner"
        |},"availableGuaranteeBalance":"100"
        |}""".stripMargin
  }
}
