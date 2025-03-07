/*
 * Copyright 2025 HM Revenue & Customs
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

import play.api.libs.json.{JsSuccess, Json}
import utils.SpecBase
import utils.TestData.{EORI_REQUEST, EORI_REQUEST_STRING}

class EoriRequestSpec extends SpecBase {

  "EoriRequest.format" should {

    "return correct result" when {
      import EoriRequest.format

      "Reads the request" in {
        Json.fromJson(Json.parse(EORI_REQUEST_STRING)) mustBe JsSuccess(EORI_REQUEST)
      }
      "Writes the object" in {
        Json.toJson(EORI_REQUEST) mustBe Json.parse(EORI_REQUEST_STRING)
      }
    }
  }
}
