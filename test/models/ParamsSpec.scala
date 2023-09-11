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

class ParamsSpec extends SpecBase {
  "object should be created" should {
    "for correct parameter values" in {
      val paramsOb = Params("2",
        "2021",
        "3",
        "2021",
        AccountTypeForParams.DutyDefermentStatement.toString,
        "1234567")

      paramsOb.accountType mustBe AccountTypeForParams.DutyDefermentStatement.toString
    }
  }

  "Exception must be thrown" should {
    "for incorrect parameter values" in {
      intercept[RuntimeException] {
        val paramsOb = Params("2",
          "2021",
          "3",
          "2021",
          "DutyDeferment",
          "1234567")
      }.getMessage.contains("invalid value for accountType," +
        " valid values are C79Certificate,PostponedVATStatement,SecurityStatement,DutyDefermentStatement")
    }
  }

  "Json Reads" should {
    "result the correct output" in {
      import Params.paramsFormat

      val paramsOb = Params("2",
        "2021",
        "3",
        "2021",
        AccountTypeForParams.DutyDefermentStatement.toString,
        "1234567")

      val jsValue =
        """{"periodStartMonth": "2",
          |"periodStartYear": "2021",
          |"periodEndMonth": "3",
          |"periodEndYear": "2021",
          |"accountType": "DutyDefermentStatement",
          |"dan": "1234567"}""".stripMargin

      Json.fromJson(Json.parse(jsValue)) mustBe JsSuccess(paramsOb)
    }
  }

  "Json Writes" should {
    "result in correct output" in {
      val paramsOb = Params("2",
        "2021",
        "3",
        "2021",
        "DutyDefermentStatement",
        "1234567")

      val jsValue =
        """{"periodStartMonth": "2",
          |"periodStartYear": "2021",
          |"periodEndMonth": "3",
          |"periodEndYear": "2021",
          |"accountType": "DutyDefermentStatement",
          |"dan": "1234567"}""".stripMargin

      Json.toJson(paramsOb) mustBe Json.parse(jsValue)
    }
  }
}
