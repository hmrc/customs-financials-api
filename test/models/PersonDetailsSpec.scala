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

class PersonDetailsSpec extends SpecBase {

  "Json Reads" should {

    "read the object correctly" in new Setup {
      import models.PersonDetails.format

      Json.fromJson(Json.parse(personObJsonString)) mustBe JsSuccess(personOb)
    }
  }

  "Json Writes" should {

    "write the object correctly" in new Setup {
      Json.toJson(personOb) mustBe Json.parse(personObJsonString)
    }
  }
}

trait Setup {
  val personOb: PersonDetails = PersonDetails("Jamie", "QQ123456B")
  val personObJsonString: String ="""{"name":"Jamie","niNumber":"QQ123456B"}""".stripMargin
}
