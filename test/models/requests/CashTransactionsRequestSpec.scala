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

import models.requests.ParamName.{MRN, UCR}
import models.requests.SearchType.{D, P}
import play.api.libs.json.{JsString, JsSuccess, Json}
import utils.SpecBase

class CashTransactionsRequestSpec extends SpecBase {

  "SearchType" should {

    "return correct value for Json Reads" in {

      import models.requests.SearchType.searchTypeReads

      val incomingJsValueP: JsString = JsString("P")
      val incomingJsValueD: JsString = JsString("D")

      Json.fromJson(incomingJsValueP) mustBe JsSuccess(P)
      Json.fromJson(incomingJsValueD) mustBe JsSuccess(D)
    }

    "return correct value for Json Writes" in {
      Json.toJson(P) mustBe JsString("P")
      Json.toJson(D) mustBe JsString("D")
    }
  }

  "ParamName" should {

    "return correct value for Json Reads" in {

      import models.requests.ParamName.paramNameReads

      val incomingJsValueMRN: JsString = JsString("MRN")
      val incomingJsValueUCR: JsString = JsString("UCR")

      Json.fromJson(incomingJsValueMRN) mustBe JsSuccess(MRN)
      Json.fromJson(incomingJsValueUCR) mustBe JsSuccess(UCR)
    }

    "return correct value for Json Writes" in {
      Json.toJson(MRN) mustBe JsString("MRN")
      Json.toJson(UCR) mustBe JsString("UCR")
    }
  }

  "CashAccountPaymentDetails" should {

    "return correct value for Json Reads" in new Setup {

      import models.requests.CashAccountPaymentDetails.format

      Json.fromJson(Json.parse(cashAccountPaymentDetailsJsString)) mustBe JsSuccess(cashAccountPaymentDetailsOb)
      Json.fromJson(Json.parse(cashAccountPaymentDetailsAmountOnlyJsString)) mustBe
        JsSuccess(cashAccountPaymentDetailsObAmountOnly)
    }

    "return correct value for Json Writes" in new Setup {
      Json.toJson(cashAccountPaymentDetailsOb) mustBe Json.parse(cashAccountPaymentDetailsJsString)
      Json.toJson(cashAccountPaymentDetailsObAmountOnly) mustBe Json.parse(cashAccountPaymentDetailsAmountOnlyJsString)
    }
  }

  "CashTransactionSearchRequestDetails" should {

    "return correct value for Json Reads" in new Setup {

      import models.requests.CashTransactionSearchRequestDetails.format

      Json.fromJson(Json.parse(cashTranSearchRequestDetailsObString)) mustBe JsSuccess(cashTranSearchRequestDetailsOb)
    }

    "return correct value for Json Writes" in new Setup {
      Json.toJson(cashTranSearchRequestDetailsOb) mustBe Json.parse(cashTranSearchRequestDetailsObString)
    }
  }

  trait Setup {
    val amount = 999.90
    val dateFromString = "2024-05-28"
    val dateToString = "2024-05-28"
    val can = "12345678"
    val ownerEORI = "test_eori"
    val paramValue = "test_value"
    val declarationDetailsOb: DeclarationDetails = DeclarationDetails(MRN, paramValue)

    val cashAccountPaymentDetailsOb: CashAccountPaymentDetails =
      CashAccountPaymentDetails(amount, Some(dateFromString), Some(dateToString))

    val cashAccountPaymentDetailsObAmountOnly: CashAccountPaymentDetails = CashAccountPaymentDetails(amount)

    val cashAccountPaymentDetailsJsString = """{"amount":999.9,"dateFrom":"2024-05-28","dateTo":"2024-05-28"}"""
    val cashAccountPaymentDetailsAmountOnlyJsString =
      """{"amount":999.9}"""

    val cashTranSearchRequestDetailsOb: CashTransactionSearchRequestDetails =
      CashTransactionSearchRequestDetails(can, ownerEORI, P, Some(declarationDetailsOb), Some(cashAccountPaymentDetailsOb))

    val cashTranSearchRequestDetailsObString: String =
      """{"can":"12345678",
        |"ownerEORI":"test_eori",
        |"searchType":"P",
        |"declarationDetails":{"paramName":"MRN","paramValue":"test_value"},
        |"cashAccountPaymentDetails":{"amount":999.9,"dateFrom":"2024-05-28","dateTo":"2024-05-28"}
        |}""".stripMargin
  }
}
