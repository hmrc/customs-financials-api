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
import utils.TestData.{
  BANK_ACCOUNT, DATE_STRING, DECLARANT_REF, REASON_FOR_SECURITY, TAX_TYPE_ID, TEST_ACK_REF, TEST_EORI, TEST_TRANS_TYPE
}
import play.api.libs.json.{JsResultException, JsSuccess, Json}

class CashDailyStatementSpec extends SpecBase {

  "taxTypeHolderFormat" should {
    import CashDailyStatement.taxTypeHolderFormat

    "generate correct output for Json Reads" in new Setup {
      Json.fromJson(Json.parse(taxTypeHolderObJsString)) mustBe JsSuccess(taxTypeHolderOb)
    }

    "generate correct output for Json Writes" in new Setup {
      Json.toJson(taxTypeHolderOb) mustBe Json.parse(taxTypeHolderObJsString)
    }

    "throw exception for invalid Json" in {
      val invalidJson = "{ \"reason\": \"London Street\", \"totalAmount\": \"600\" }"

      intercept[JsResultException] {
        Json.parse(invalidJson).as[TaxTypeHolder]
      }
    }
  }

  "transactionFormat" should {
    import CashDailyStatement.transactionFormat

    "generate correct output for Json Reads" in new Setup {
      Json.fromJson(Json.parse(transactionObJsString)) mustBe JsSuccess(transactionOb)
    }

    "generate correct output for Json Writes" in new Setup {
      Json.toJson(transactionOb) mustBe Json.parse(transactionObJsString)
    }

    "throw exception for invalid Json" in {
      val invalidJson = "{ \"bankAccount\": \"London Street\", \"totalAmount\": \"600\" }"

      intercept[JsResultException] {
        Json.parse(invalidJson).as[Transaction]
      }
    }
  }

  "taxGroupDetailFormat" should {
    import CashDailyStatement.taxGroupDetailFormat

    "generate correct output for Json Reads" in new Setup {
      Json.fromJson(Json.parse(taxGroupObJsString)) mustBe JsSuccess(taxGroupOb)
    }

    "generate correct output for Json Writes" in new Setup {
      Json.toJson(taxGroupOb) mustBe Json.parse(taxGroupObJsString)
    }

    "throw exception for invalid Json" in {
      val invalidJson = "{ \"description\": \"London Street\", \"totalAmount\": \"600\" }"

      intercept[JsResultException] {
        Json.parse(invalidJson).as[TaxGroup]
      }
    }
  }

  "declarationFormat" should {
    import CashDailyStatement.declarationFormat

    "generate correct output for Json Reads" in new Setup {
      Json.fromJson(Json.parse(declarationObJsString)) mustBe JsSuccess(declarationOb)
    }

    "generate correct output for Json Writes" in new Setup {
      Json.toJson(declarationOb) mustBe Json.parse(declarationObJsString)
    }

    "throw exception for invalid Json" in {
      val invalidJson = "{ \"eoriNumber\": \"London Street\", \"date\": \"2023-10-12\" }"

      intercept[JsResultException] {
        Json.parse(invalidJson).as[Declaration]
      }
    }
  }

  "cashDailyStatementFormat" should {
    import CashDailyStatement.cashDailyStatementFormat

    "generate correct output for Json Reads" in new Setup {
      Json.fromJson(Json.parse(cashDailyStatementObJsString)) mustBe JsSuccess(cashDailyStatementOb)
    }

    "generate correct output for Json Writes" in new Setup {
      Json.toJson(cashDailyStatementOb) mustBe Json.parse(cashDailyStatementObJsString)
    }

    "throw exception for invalid Json" in {
      val invalidJson = "{ \"date1\": \"2023-12-01\", \"totalAmount\": \"600\" }"

      intercept[JsResultException] {
        Json.parse(invalidJson).as[CashDailyStatement]
      }
    }
  }

  "cashTransactionsFormat" should {
    import CashDailyStatement.cashTransactionsFormat

    "generate correct output for Json Reads" in new Setup {
      Json.fromJson(Json.parse(cashTransactionsObJsString)) mustBe JsSuccess(cashTransactionsOb)
    }

    "generate correct output for Json Writes" in new Setup {
      Json.toJson(cashTransactionsOb) mustBe Json.parse(cashTransactionsObJsString)
    }

    "throw exception for invalid Json" in {
      val invalidJson = "{ \"transactions\": \"London Street\", \"closeTransactions\": \"600\" }"

      intercept[JsResultException] {
        Json.parse(invalidJson).as[CashTransactions]
      }
    }
  }

  trait Setup {
    val taxTypeHolderOb: TaxTypeHolder = TaxTypeHolder(Some(REASON_FOR_SECURITY), TAX_TYPE_ID, "100")
    val transactionOb: Transaction     = Transaction("100", TEST_TRANS_TYPE, Some(BANK_ACCOUNT))

    val taxGroupOb: TaxGroup =
      TaxGroup(taxGroupDescription = "test_group", amount = "100", taxTypes = Seq(taxTypeHolderOb))

    val declarationOb: Declaration = Declaration(
      movementReferenceNumber = TEST_ACK_REF,
      importerEori = Some(TEST_EORI),
      declarantEori = TEST_EORI,
      declarantReference = Some(DECLARANT_REF),
      date = DATE_STRING,
      amount = "100",
      taxGroups = Seq(taxGroupOb)
    )

    val cashDailyStatementOb: CashDailyStatement = CashDailyStatement(
      date = DATE_STRING,
      openingBalance = "500",
      closingBalance = "300",
      declarations = Seq(declarationOb),
      otherTransactions = Seq(transactionOb)
    )

    val cashTransactionsOb: CashTransactions = CashTransactions(
      pendingTransactions = Seq(declarationOb),
      cashDailyStatements = Seq(cashDailyStatementOb),
      maxTransactionsExceeded = Some(false)
    )

    val taxTypeHolderObJsString: String =
      """{"reasonForSecurity":"Reason1","taxTypeID":"test_tax_id","amount":"100"}""".stripMargin

    val transactionObJsString: String =
      """{"amount":"100","transactionType":"Payment","bankAccountNumber":"1234567890987"}""".stripMargin

    val taxGroupObJsString: String =
      """{
        |"taxGroupDescription":"test_group",
        |"amount":"100",
        |"taxTypes":[{"reasonForSecurity":"Reason1","taxTypeID":"test_tax_id","amount":"100"}]
        |}""".stripMargin

    val declarationObJsString: String =
      """{
        |"movementReferenceNumber":"1234567890abcdefgh",
        |"importerEori":"testEORI",
        |"declarantEori":"testEORI",
        |"declarantReference":"1234567890abcdefgh",
        |"date":"2024-05-28",
        |"amount":"100",
        |"taxGroups":[{
        |"taxGroupDescription":"test_group",
        |"amount":"100",
        |"taxTypes":[{"reasonForSecurity":"Reason1","taxTypeID":"test_tax_id","amount":"100"}]
        |}]
        |}""".stripMargin

    val cashDailyStatementObJsString: String =
      """{
        |"date":"2024-05-28",
        |"openingBalance":"500",
        |"closingBalance":"300",
        |"declarations":[{
        |"movementReferenceNumber":"1234567890abcdefgh",
        |"importerEori":"testEORI",
        |"declarantEori":"testEORI",
        |"declarantReference":"1234567890abcdefgh",
        |"date":"2024-05-28",
        |"amount":"100",
        |"taxGroups":[{"taxGroupDescription":"test_group",
        |"amount":"100",
        |"taxTypes":[{"reasonForSecurity":"Reason1","taxTypeID":"test_tax_id","amount":"100"}]}]}],
        |"otherTransactions":[{"amount":"100","transactionType":"Payment","bankAccountNumber":"1234567890987"}]
        |}""".stripMargin

    val cashTransactionsObJsString: String =
      """{
        |"pendingTransactions":
        |[{"movementReferenceNumber":"1234567890abcdefgh",
        |"importerEori":"testEORI",
        |"declarantEori":"testEORI",
        |"declarantReference":"1234567890abcdefgh",
        |"date":"2024-05-28",
        |"amount":"100",
        |"taxGroups":
        |[{"taxGroupDescription":"test_group",
        |"amount":"100",
        |"taxTypes":[{"reasonForSecurity":"Reason1","taxTypeID":"test_tax_id","amount":"100"}]}]}],
        |"cashDailyStatements":
        |[{"date":"2024-05-28",
        |"openingBalance":"500",
        |"closingBalance":"300",
        |"declarations":
        |[{"movementReferenceNumber":"1234567890abcdefgh",
        |"importerEori":"testEORI",
        |"declarantEori":"testEORI",
        |"declarantReference":"1234567890abcdefgh",
        |"date":"2024-05-28",
        |"amount":"100",
        |"taxGroups":
        |[{"taxGroupDescription":"test_group",
        |"amount":"100",
        |"taxTypes":[{"reasonForSecurity":"Reason1","taxTypeID":"test_tax_id","amount":"100"}]}]}],
        |"otherTransactions":[{"amount":"100","transactionType":"Payment","bankAccountNumber":"1234567890987"}]}],
        |"maxTransactionsExceeded":false
        |}""".stripMargin
  }
}
