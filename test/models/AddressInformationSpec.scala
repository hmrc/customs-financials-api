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

import models.AddressInformation.format
import utils.SpecBase
import play.api.libs.json.{Json, JsSuccess}

class AddressInformationSpec extends SpecBase {

  "AddressInformation" should {
    "populate correctly" in new Setup {
      val result: AddressInformation = AddressInformation("street&Number", "london", Option("Post"), "GB")
      result mustBe expectedResult
    }

    "generate correct output using the Reads" in new Setup {
      Json.fromJson(Json.parse(addressJsValue)) mustBe JsSuccess(addressObject)
    }

    "generate correct output using the Writes" in new Setup {
      Json.toJson(addressObject) mustBe Json.parse(addressJsValue)
    }
  }

  trait Setup {
    val streetAndNumber: String = "street&Number"
    val city: String = "london"
    val postalCode: Option[String] = Option("Post")
    val countryCode: String = "GB"

    val expectedResult: AddressInformation = AddressInformation(
      streetAndNumber = streetAndNumber,
      city = city,
      postalCode = postalCode,
      countryCode = countryCode)

    val addressJsValue: String = """{"streetAndNumber": "123",
        |"city": "city","postalCode": "post","countryCode": "GB"}""".stripMargin

    val addressObject: AddressInformation = AddressInformation(streetAndNumber = "123",
      city = "city", postalCode = Option("post"), countryCode = "GB")
  }
}
