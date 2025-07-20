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

package models.claims.responses

import utils.SpecBase
import play.api.libs.json.{JsResultException, JsSuccess, Json}

class GoodsSpec extends SpecBase {

  "format" should {
    "generate correct output for Json Reads" in new Setup {
      import Goods.format

      Json.fromJson(Json.parse(goodsJsString)) mustBe JsSuccess(goodsOb)
    }

    "generate correct output for Json Writes" in new Setup {
      Json.toJson(goodsOb) mustBe Json.parse(goodsJsString)
    }

    "throw exception for invalid Json" in {
      val invalidJson = "{ \"itemNum\": \"15\", \"goodsDescription\": \"test_desc\" }"

      intercept[JsResultException] {
        Json.parse(invalidJson).as[Goods]
      }
    }
  }

  trait Setup {
    val goodsOb: Goods        = Goods("12", Some("test_description"))
    val goodsJsString: String = """{"itemNumber":"12","goodsDescription":"test_description"}""".stripMargin
  }
}
