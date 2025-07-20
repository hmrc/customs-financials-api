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

package domain

import utils.SpecBase
import utils.TestData.{DATE_STRING, DECLARANT_REF, TEST_ACK_REF, TEST_EORI}
import play.api.libs.json.{JsResultException, JsSuccess, Json}

class GuaranteeTransactionSpec extends SpecBase {

  "amountsFormat" should {
    "generate correct output for Json Reads" in new Setup {
      import GuaranteeTransaction.amountsFormat

      Json.fromJson(Json.parse(amountsObJsString)) mustBe JsSuccess(amountsOb)
    }

    "generate correct output for Json Writes" in new Setup {
      import GuaranteeTransaction.amountsFormat

      Json.toJson(amountsOb) mustBe Json.parse(amountsObJsString)
    }

    "throw exception for invalid Json" in {
      import GuaranteeTransaction.amountsFormat

      val invalidJson = "{ \"openAmount\": \"300\", \"totalAmount\": \"600\" }"

      intercept[JsResultException] {
        Json.parse(invalidJson).as[Amounts]
      }
    }
  }

  "taxTypeFormat" should {
    "generate correct output for Json Reads" in new Setup {
      import GuaranteeTransaction.taxTypeFormat

      Json.fromJson(Json.parse(taxTypeObJsString)) mustBe JsSuccess(taxTypeOb)
    }

    "generate correct output for Json Writes" in new Setup {
      import GuaranteeTransaction.taxTypeFormat

      Json.toJson(taxTypeOb) mustBe Json.parse(taxTypeObJsString)
    }

    "throw exception for invalid Json" in {
      import GuaranteeTransaction.taxTypeFormat

      val invalidJson = "{ \"taxType\": \"300\", \"totalAmount\": \"600\" }"

      intercept[JsResultException] {
        Json.parse(invalidJson).as[TaxType]
      }
    }
  }

  "taxTypeGroupFormat" should {
    "generate correct output for Json Reads" in new Setup {
      import GuaranteeTransaction.taxTypeGroupFormat

      Json.fromJson(Json.parse(taxTypeGroupObJsString)) mustBe JsSuccess(taxTypeGroupOb)
    }

    "generate correct output for Json Writes" in new Setup {
      import GuaranteeTransaction.taxTypeGroupFormat

      Json.toJson(taxTypeGroupOb) mustBe Json.parse(taxTypeGroupObJsString)
    }

    "throw exception for invalid Json" in {
      import GuaranteeTransaction.taxTypeGroupFormat

      val invalidJson = "{ \"taxType\": \"300\", \"totalAmount\": \"600\" }"

      intercept[JsResultException] {
        Json.parse(invalidJson).as[TaxTypeGroup]
      }
    }
  }

  "dueDateFormat" should {
    "generate correct output for Json Reads" in new Setup {
      import GuaranteeTransaction.dueDateFormat

      Json.fromJson(Json.parse(dueDateObJsString)) mustBe JsSuccess(dueDateOb)
    }

    "generate correct output for Json Writes" in new Setup {
      import GuaranteeTransaction.dueDateFormat

      Json.toJson(dueDateOb) mustBe Json.parse(dueDateObJsString)
    }

    "throw exception for invalid Json" in {
      import GuaranteeTransaction.dueDateFormat

      val invalidJson = "{ \"duedate\": \"2023-01-12\", \"unknown\": \"600\" }"

      intercept[JsResultException] {
        Json.parse(invalidJson).as[DueDate]
      }
    }
  }

  "guaranteeTransactionFormat" should {
    "generate correct output for Json Reads" in new Setup {

      import GuaranteeTransaction.guaranteeTransactionFormat

      Json.fromJson(Json.parse(guaranteeTransactionObJsString)) mustBe JsSuccess(guaranteeTransactionOb)
    }

    "generate correct output for Json Writes" in new Setup {
      Json.toJson(guaranteeTransactionOb) mustBe Json.parse(guaranteeTransactionObJsString)
    }

    "throw exception for invalid Json" in {
      val invalidJson = "{ \"date\": \"2023-01-12\", \"ref_no\": \"1234fghhs\" }"

      intercept[JsResultException] {
        Json.parse(invalidJson).as[GuaranteeTransaction]
      }
    }
  }

  trait Setup {

    val amountsOb: Amounts =
      Amounts(openAmount = Some("300"), totalAmount = "600", clearedAmount = Some("200"), updateDate = DATE_STRING)

    val taxTypeOb: TaxType = TaxType("taxType_1", amountsOb)

    val taxTypeGroupOb: TaxTypeGroup = TaxTypeGroup(taxTypeGroup = "VAT", amounts = amountsOb, taxType = taxTypeOb)

    val dueDateOb: DueDate = DueDate(
      dueDate = DATE_STRING,
      reasonForSecurity = Some("test_reason"),
      amounts = amountsOb,
      taxTypeGroups = Seq(taxTypeGroupOb)
    )

    val guaranteeTransactionOb: GuaranteeTransaction = GuaranteeTransaction(
      date = DATE_STRING,
      movementReferenceNumber = DECLARANT_REF,
      balance = Some("100"),
      uniqueConsignmentReference = Some(TEST_ACK_REF),
      declarantEori = TEST_EORI,
      consigneeEori = TEST_EORI,
      originalCharge = "1200",
      dischargedAmount = None,
      interestCharge = None,
      c18Reference = None,
      dueDates = Seq(dueDateOb)
    )

    val amountsObJsString: String =
      """{"openAmount":"300","totalAmount":"600","clearedAmount":"200","updateDate":"2024-05-28"}""".stripMargin

    val taxTypeObJsString: String =
      """{
        |"taxType":"taxType_1",
        |"amounts":{"openAmount":"300","totalAmount":"600","clearedAmount":"200","updateDate":"2024-05-28"}
        |}""".stripMargin

    val taxTypeGroupObJsString: String =
      """{
        |"taxTypeGroup":"VAT",
        |"amounts":{"openAmount":"300","totalAmount":"600","clearedAmount":"200","updateDate":"2024-05-28"},
        |"taxType":{
        |"taxType":"taxType_1",
        |"amounts":{"openAmount":"300","totalAmount":"600","clearedAmount":"200","updateDate":"2024-05-28"}
        |}
        |}""".stripMargin

    val dueDateObJsString: String =
      """{
        |"dueDate":"2024-05-28",
        |"reasonForSecurity":"test_reason",
        |"amounts":{"openAmount":"300","totalAmount":"600","clearedAmount":"200","updateDate":"2024-05-28"},
        |"taxTypeGroups":[{
        |"taxTypeGroup":"VAT",
        |"amounts":{"openAmount":"300","totalAmount":"600","clearedAmount":"200","updateDate":"2024-05-28"},
        |"taxType":{
        |"taxType":"taxType_1",
        |"amounts":{"openAmount":"300","totalAmount":"600","clearedAmount":"200","updateDate":"2024-05-28"}
        |}
        |}
        |]}""".stripMargin

    val guaranteeTransactionObJsString: String =
      """{
        |"date":"2024-05-28",
        |"movementReferenceNumber":"1234567890abcdefgh",
        |"balance":"100",
        |"uniqueConsignmentReference":"1234567890abcdefgh",
        |"declarantEori":"testEORI",
        |"consigneeEori":"testEORI",
        |"originalCharge":"1200",
        |"dueDates":[
        |{"dueDate":"2024-05-28",
        |"reasonForSecurity":"test_reason",
        |"amounts":{"openAmount":"300","totalAmount":"600","clearedAmount":"200","updateDate":"2024-05-28"},
        |"taxTypeGroups":[
        |{"taxTypeGroup":"VAT",
        |"amounts":{"openAmount":"300","totalAmount":"600","clearedAmount":"200","updateDate":"2024-05-28"},
        |"taxType":{"taxType":"taxType_1",
        |"amounts":{"openAmount":"300","totalAmount":"600","clearedAmount":"200","updateDate":"2024-05-28"}
        |}
        |}
        |]}
        |]}""".stripMargin
  }
}
