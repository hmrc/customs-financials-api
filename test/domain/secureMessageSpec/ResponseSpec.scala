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

package domain.secureMessage

import play.api.libs.json.{JsSuccess, Json}
import utils.SpecBase

class ResponseSpec extends SpecBase {

  "ExternalReference" should {
    "generate correct output using the Reads" in new Setup {
      import domain.secureMessage.ExternalReference.extRefFormat
      Json.fromJson(Json.parse(externalRefJsValue)) mustBe JsSuccess(externalRefObject)
    }

    "generate correct output using the Writes" in new Setup {
      Json.toJson(externalRefObject) mustBe Json.parse(externalRefJsValue)
    }
  }

  trait Setup {
    val externalRefJsValue: String = """{"id": "abcd12345","source": "mdtp"}""".stripMargin
    val externalRefObject: ExternalReference = ExternalReference(id = "abcd12345", source = "mdtp")
  }
}
