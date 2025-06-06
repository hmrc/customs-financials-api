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

import play.api.libs.json.*
import utils.SpecBase
import utils.Utils.emptyString

class AccountNumberSpec extends SpecBase {
  "apply" should {

    "return correct value" in {
      val accountNumberValue = "12345678"

      AccountNumber(Some(accountNumberValue)) mustBe AccountNumber(accountNumberValue)
      AccountNumber(None) mustBe AccountNumber(emptyString)
    }
  }

  "Json writes" should {
    "write the object correctly" in {
      val accountNumberValue = "12345678"

      Json.toJson(AccountNumber(accountNumberValue)) mustBe JsString("12345678")
    }
  }

  "Json reads" should {

    "read the object correctly" in {
      import models.AccountNumber.format
      val accountNumberValue = "12345678"

      Json.fromJson(JsString(accountNumberValue)) mustBe JsSuccess(AccountNumber(accountNumberValue))
    }

    "return JsError for invalid input" in {
      import models.AccountNumber.format

      Json.fromJson(JsNumber(1)) mustBe JsError(s"Expected JSON string type")
    }
  }
}
