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

package models.responses

import models.EORI
import play.api.libs.json.{JsError, JsResultException, JsString, JsSuccess, Json}
import utils.SpecBase
import utils.Utils.emptyString
import utils.TestData.{DATE_STRING, TAX_TYPE_ID, TEST_EORI, TEST_STATUS}

class GuaranteeTransactionsResponseSpec extends SpecBase {

  "thresholdErrorFormat" should {
    "generate correct output for the reads" in {
      import GuaranteeTransactionsResponse.thresholdErrorFormat

      Json.fromJson(JsString(emptyString)) mustBe JsError("Empty object expected")
    }
  }

  "noAssociatedDataFormat" should {
    "generate correct output for the reads" in {
      import GuaranteeTransactionsResponse.noAssociatedDataFormat

      Json.fromJson(JsString(emptyString)) mustBe JsError("Empty object expected")
    }
  }

  "ResponseCommon" should {
    import GuaranteeTransactionsResponse.responseCommonFormat

    "generate correct output for Json Reads" in new Setup {
      Json.fromJson(Json.parse(responseCommonObJsString)) mustBe JsSuccess(responseCommonOb)
    }

    "throw exception for invalid Json" in {
      val invalidJson = "{ \"status1\": \"300\", \"totalAmount\": \"600\" }"

      intercept[JsResultException] {
        Json.parse(invalidJson).as[ResponseCommon]
      }
    }

    "generate correct output for Json Writes" in new Setup {
      Json.toJson(responseCommonOb) mustBe Json.parse(responseCommonObJsString)
    }
  }

  "DefAmounts" should {
    import GuaranteeTransactionsResponse.defAmountsFormat

    "generate correct output for Json Reads" in new Setup {
      Json.fromJson(Json.parse(defAmountsObJsString)) mustBe JsSuccess(defAmountsOb)
    }

    "throw exception for invalid Json" in {
      val invalidJson = "{ \"taxTy\": \"300\", \"totalAmount\": \"600\" }"

      intercept[JsResultException] {
        Json.parse(invalidJson).as[DefAmounts]
      }
    }

    "generate correct output for Json Writes" in new Setup {
      Json.toJson(defAmountsOb) mustBe Json.parse(defAmountsObJsString)
    }
  }

  "TaxType" should {
    import GuaranteeTransactionsResponse.taxTypeFormat

    "generate correct output for Json Reads" in new Setup {
      Json.fromJson(Json.parse(taxTypeObJsString)) mustBe JsSuccess(taxTypeOb)
    }

    "throw exception for invalid Json" in {
      val invalidJson = "{ \"taxTy\": \"300\", \"totalAmount\": \"600\" }"

      intercept[JsResultException] {
        Json.parse(invalidJson).as[TaxType]
      }
    }

    "generate correct output for Json Writes" in new Setup {
      Json.toJson(taxTypeOb) mustBe Json.parse(taxTypeObJsString)
    }
  }

  "TaxTypeGroup" should {
    import GuaranteeTransactionsResponse.taxTypeGroupFormat

    "generate correct output for Json Reads" in new Setup {
      Json.fromJson(Json.parse(taxTypeGroupObJsString)) mustBe JsSuccess(taxTypeGroupOb)
    }

    "throw exception for invalid Json" in {
      val invalidJson = "{ \"taxGroup\": \"300\", \"totalAmount\": \"600\" }"

      intercept[JsResultException] {
        Json.parse(invalidJson).as[TaxTypeGroup]
      }
    }

    "generate correct output for Json Writes" in new Setup {
      Json.toJson(taxTypeGroupOb) mustBe Json.parse(taxTypeGroupObJsString)
    }
  }

  "DueDate" should {
    import GuaranteeTransactionsResponse.dueDateFormat

    "generate correct output for Json Reads" in new Setup {
      Json.fromJson(Json.parse(dueDateObJsString)) mustBe JsSuccess(dueDateOb)
    }

    "throw exception for invalid Json" in {
      val invalidJson = "{ \"dueDate1\": \"300\", \"totalAmount\": \"600\" }"

      intercept[JsResultException] {
        Json.parse(invalidJson).as[DueDate]
      }
    }

    "generate correct output for Json Writes" in new Setup {
      Json.toJson(dueDateOb) mustBe Json.parse(dueDateObJsString)
    }
  }

  "GuaranteeTransactionDeclaration" should {
    import GuaranteeTransactionsResponse.declarationsFormat

    "generate correct output for Json Reads" in new Setup {
      Json.fromJson(Json.parse(guaranteeTransactionDeclarationObJsString)) mustBe JsSuccess(
        guaranteeTransactionDeclarationOb
      )
    }

    "throw exception for invalid Json" in {
      val invalidJson = "{ \"decId\": \"300\", \"totalAmount\": \"600\" }"

      intercept[JsResultException] {
        Json.parse(invalidJson).as[GuaranteeTransactionDeclaration]
      }
    }

    "generate correct output for Json Writes" in new Setup {
      Json.toJson(guaranteeTransactionDeclarationOb) mustBe Json.parse(guaranteeTransactionDeclarationObJsString)
    }
  }

  "ResponseDetail" should {
    import GuaranteeTransactionsResponse.responseDetailFormat

    "generate correct output for Json Reads" in new Setup {
      Json.fromJson(Json.parse(responseDetailObJsString)) mustBe JsSuccess(responseDetailOb)
    }

    "throw exception for invalid Json" in {
      val invalidJson = "{ \"openItem\": \"false\", \"totalAmount\": \"600\" }"

      intercept[JsResultException] {
        Json.parse(invalidJson).as[ResponseDetail]
      }
    }

    "generate correct output for Json Writes" in new Setup {
      Json.toJson(responseDetailOb) mustBe Json.parse(responseDetailObJsString)
    }
  }

  "GetGGATransactionResponse" should {
    import GuaranteeTransactionsResponse.getGGATransactionResponseFormat

    "generate correct output for Json Reads" in new Setup {
      Json.fromJson(Json.parse(getGGATransactionResponseObJsString)) mustBe JsSuccess(getGGATransactionResponseOb)
    }

    "throw exception for invalid Json" in {
      val invalidJson = "{ \"taxTy\": \"300\", \"totalAmount\": \"600\" }"

      intercept[JsResultException] {
        Json.parse(invalidJson).as[GetGGATransactionResponse]
      }
    }

    "generate correct output for Json Writes" in new Setup {
      Json.toJson(getGGATransactionResponseOb) mustBe Json.parse(getGGATransactionResponseObJsString)
    }
  }

  "GetGGATransactionResponse.guaranteeTransactionsResponseFormat" should {
    import GuaranteeTransactionsResponse.guaranteeTransactionsResponseFormat

    "generate correct output for Json Reads" in new Setup {
      Json.fromJson(Json.parse(guaranteeTransactionsResponseObJsString)) mustBe JsSuccess(
        guaranteeTransactionsResponseOb
      )
    }

    "throw exception for invalid Json" in {
      val invalidJson = "{ \"ggaResponse\": \"300\" }"

      intercept[JsResultException] {
        Json.parse(invalidJson).as[GuaranteeTransactionsResponse]
      }
    }

    "generate correct output for Json Writes" in new Setup {
      Json.toJson(guaranteeTransactionsResponseOb) mustBe Json.parse(guaranteeTransactionsResponseObJsString)
    }
  }

  trait Setup {

    val responseCommonOb: ResponseCommon = ResponseCommon(
      status = TEST_STATUS,
      statusText = Some("test_status"),
      processingDate = "test_data"
    )

    val defAmountsOb: DefAmounts = DefAmounts(None, "10000", None, DATE_STRING)

    val taxTypeOb: TaxType = TaxType(taxType = TAX_TYPE_ID, defAmounts = defAmountsOb)

    val taxTypeGroupOb: TaxTypeGroup =
      TaxTypeGroup(taxTypeGroup = "VAT", defAmounts = defAmountsOb, taxTypes = Seq(taxTypeOb))

    val dueDateOb: DueDate = DueDate(
      dueDate = DATE_STRING,
      reasonForSecurity = None,
      defAmounts = defAmountsOb,
      taxTypeGroups = Seq(taxTypeGroupOb)
    )

    val guaranteeTransactionDeclarationOb: GuaranteeTransactionDeclaration = GuaranteeTransactionDeclaration(
      "id",
      "date",
      None,
      EORI("someEori"),
      EORI("someOtherEori"),
      DefAmounts(None, "10000", None, "date"),
      None,
      None,
      Seq(dueDateOb)
    )

    val responseDetailOb: ResponseDetail = ResponseDetail(true, Seq(guaranteeTransactionDeclarationOb))

    val getGGATransactionResponseOb: GetGGATransactionResponse =
      GetGGATransactionResponse(responseCommonOb, Some(responseDetailOb))

    val guaranteeTransactionsResponseOb: GuaranteeTransactionsResponse = GuaranteeTransactionsResponse(
      getGGATransactionResponseOb
    )

    val responseCommonObJsString: String =
      """{"status":"pending","statusText":"test_status","processingDate":"test_data"}""".stripMargin

    val defAmountsObJsString: String = """{"totalAmount":"10000","updateDate":"2024-05-28"}""".stripMargin

    val taxTypeObJsString: String =
      """{"taxType":"test_tax_id","defAmounts":{"totalAmount":"10000","updateDate":"2024-05-28"}}""".stripMargin

    val taxTypeGroupObJsString: String =
      """{"taxTypeGroup":"VAT",
        |"defAmounts":{
        |"totalAmount":"10000",
        |"updateDate":"2024-05-28"},
        |"taxTypes":[{"taxType":"test_tax_id","defAmounts":{"totalAmount":"10000","updateDate":"2024-05-28"}}]
        |}""".stripMargin

    val dueDateObJsString: String =
      """{"dueDate":"2024-05-28",
        |"defAmounts":{"totalAmount":"10000","updateDate":"2024-05-28"},
        |"taxTypeGroups":[{
        |"taxTypeGroup":"VAT",
        |"defAmounts":{"totalAmount":"10000","updateDate":"2024-05-28"},
        |"taxTypes":[{"taxType":"test_tax_id","defAmounts":{"totalAmount":"10000","updateDate":"2024-05-28"}}]
        |}]}""".stripMargin

    val guaranteeTransactionDeclarationObJsString: String =
      """{
        |"declarationID":"id",
        |"postingDate":"date",
        |"declarantsEORINumber":"someEori",
        |"importersEORINumber":"someOtherEori",
        |"defAmounts":{"totalAmount":"10000","updateDate":"date"},
        |"dueDates":[{
        |"dueDate":"2024-05-28",
        |"defAmounts":{"totalAmount":"10000","updateDate":"2024-05-28"},
        |"taxTypeGroups":[{
        |"taxTypeGroup":"VAT",
        |"defAmounts":{
        |"totalAmount":"10000","updateDate":"2024-05-28"},
        |"taxTypes":[{"taxType":"test_tax_id","defAmounts":{"totalAmount":"10000","updateDate":"2024-05-28"}}]
        |}]}]}""".stripMargin

    val responseDetailObJsString: String =
      """{
        |"openItems":true,
        |"declarations":[{
        |"declarationID":"id",
        |"postingDate":"date",
        |"declarantsEORINumber":"someEori",
        |"importersEORINumber":"someOtherEori",
        |"defAmounts":{"totalAmount":"10000","updateDate":"date"},
        |"dueDates":[{
        |"dueDate":"2024-05-28",
        |"defAmounts":{"totalAmount":"10000","updateDate":"2024-05-28"},
        |"taxTypeGroups":[{
        |"taxTypeGroup":"VAT",
        |"defAmounts":{
        |"totalAmount":"10000",
        |"updateDate":"2024-05-28"},
        |"taxTypes":[{"taxType":"test_tax_id","defAmounts":{"totalAmount":"10000","updateDate":"2024-05-28"}}]
        |}]}]}]}""".stripMargin

    val getGGATransactionResponseObJsString: String =
      """{
        |"responseCommon":{"status":"pending","statusText":"test_status","processingDate":"test_data"},
        |"responseDetail":{
        |"openItems":true,
        |"declarations":[{
        |"declarationID":"id",
        |"postingDate":"date",
        |"declarantsEORINumber":"someEori",
        |"importersEORINumber":"someOtherEori",
        |"defAmounts":{
        |"totalAmount":"10000",
        |"updateDate":"date"},
        |"dueDates":[{
        |"dueDate":"2024-05-28",
        |"defAmounts":{"totalAmount":"10000","updateDate":"2024-05-28"},
        |"taxTypeGroups":[{
        |"taxTypeGroup":"VAT",
        |"defAmounts":{"totalAmount":"10000","updateDate":"2024-05-28"},
        |"taxTypes":[{"taxType":"test_tax_id","defAmounts":{"totalAmount":"10000","updateDate":"2024-05-28"}}]
        |}]}]}]}}""".stripMargin

    val guaranteeTransactionsResponseObJsString: String =
      """{
        |"getGGATransactionResponse":
        |{"responseCommon":{"status":"pending","statusText":"test_status","processingDate":"test_data"},
        |"responseDetail":
        |{"openItems":true,
        |"declarations":[
        |{"declarationID":"id",
        |"postingDate":"date",
        |"declarantsEORINumber":"someEori",
        |"importersEORINumber":"someOtherEori",
        |"defAmounts":{"totalAmount":"10000","updateDate":"date"},
        |"dueDates":
        |[{"dueDate":"2024-05-28",
        |"defAmounts":{"totalAmount":"10000","updateDate":"2024-05-28"},
        |"taxTypeGroups":
        |[{"taxTypeGroup":"VAT",
        |"defAmounts":{"totalAmount":"10000","updateDate":"2024-05-28"},
        |"taxTypes":[{"taxType":"test_tax_id","defAmounts":{"totalAmount":"10000","updateDate":"2024-05-28"}}
        |]}]}]}]}}}""".stripMargin
  }
}
