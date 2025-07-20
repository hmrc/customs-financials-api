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
import models.responses.PaymentType.*
import play.api.libs.json.{JsResultException, JsString, JsSuccess, Json}
import utils.SpecBase
import utils.TestData.*

class CashTransactionsResponseSpec extends SpecBase {

  "PaymentAndWithdrawalDetail" should {
    import models.responses.CashTransactionsResponse.paymentAndWithdrawalDetailFormat

    "generate correct output for Json Writes" in new Setup {
      Json.toJson(paymentAndWithdrawalDetailOb) mustBe Json.parse(paymentAndWithdrawalDetailJsString)
    }

    "generate correct output for Json Reads" in new Setup {
      Json.fromJson(Json.parse(paymentAndWithdrawalDetailJsString)) mustBe JsSuccess(paymentAndWithdrawalDetailOb)
    }

    "throw exception for invalid Json" in {
      val invalidJson = "{ \"openAmount\": \"300\", \"totalAmount\": \"600\" }"

      intercept[JsResultException] {
        Json.parse(invalidJson).as[PaymentAndWithdrawalDetail]
      }
    }
  }

  "PaymentType" should {

    "generate correct output for Json Reads" in {
      val incomingJsValuePayment: JsString    = JsString("Payment")
      val incomingJsValueWithdrawal: JsString = JsString("Withdrawal")
      val incomingJsValueTransfer: JsString   = JsString("Transfer")

      Json.fromJson(incomingJsValuePayment) mustBe JsSuccess(Payment)
      Json.fromJson(incomingJsValueWithdrawal) mustBe JsSuccess(Withdrawal)
      Json.fromJson(incomingJsValueTransfer) mustBe JsSuccess(Transfer)
    }

    "generate correct output for Json Writes" in {
      Json.toJson(Payment) mustBe JsString("Payment")
      Json.toJson(Withdrawal) mustBe JsString("Withdrawal")
      Json.toJson(Transfer) mustBe JsString("Transfer")
    }
  }

  "PaymentsWithdrawalsAndTransfer" should {

    "generate correct output for Json Reads" in new Setup {

      import models.responses.PaymentsWithdrawalsAndTransfer.format

      Json.fromJson(Json.parse(paymentsWithdrawalAndTransferJsString)) mustBe
        JsSuccess(paymentsWithdrawalsAndTransferOb)
    }

    "generate correct output for Json Writes" in new Setup {

      import models.responses.PaymentsWithdrawalsAndTransfer.format

      Json.toJson(paymentsWithdrawalsAndTransferOb) mustBe Json.parse(paymentsWithdrawalAndTransferJsString)
    }
  }

  "CashAccountTransactionSearchResponseDetail" should {

    "generate correct output for Json Reads" in new Setup {

      import models.responses.CashAccountTransactionSearchResponseDetail.format

      Json.fromJson(Json.parse(cashAccountTransactionSearchResponseDetailJsString)) mustBe
        JsSuccess(cashAccountTransactionSearchResponseDetailOb)
    }

    "generate correct output for Json Writes" in new Setup {

      Json.toJson(cashAccountTransactionSearchResponseDetailOb) mustBe
        Json.parse(cashAccountTransactionSearchResponseDetailJsString)
    }
  }

  "CashAccountTransactionSearchResponse" should {

    "generate correct output for Json Reads" in new Setup {

      import models.responses.CashAccountTransactionSearchResponse.format

      Json.fromJson(Json.parse(cashAccountTransactionSearchResponseJsString)) mustBe
        JsSuccess(cashAccountTransactionSearchResponseOb)
    }

    "generate correct output for Json Writes" in new Setup {
      Json.toJson(cashAccountTransactionSearchResponseOb) mustBe
        Json.parse(cashAccountTransactionSearchResponseJsString)
    }
  }

  "CashAccountTransactionSearchResponseContainer" should {

    "generate correct output for Json Reads" when {
      import models.responses.CashAccountTransactionSearchResponseContainer.format

      "response is success" in new Setup {
        Json.fromJson(Json.parse(cashAccTranSearchResponseContainerJsString)) mustBe
          JsSuccess(cashAccTranSearchResponseContainerOb)

        Json.fromJson(Json.parse(cashAccTranSearchResponseContainerWithDeclarationJsString)) mustBe
          JsSuccess(cashAccTranSearchResponseContainerWithDeclarationOb)
      }

      "EIS returns 201 to MDTP" in new Setup {
        val actual: CashAccountTransactionSearchResponseContainer =
          Json.fromJson(Json.parse(etmpStatus422AndMDTP201TranSearchResponseContainerJsString)).get

        val expected: CashAccountTransactionSearchResponseContainer =
          etmpStatus422AndMDTP201TranSearchResponseContainerOb

        actual.cashAccountTransactionSearchResponse.responseCommon.returnParameters.get mustBe
          expected.cashAccountTransactionSearchResponse.responseCommon.returnParameters.get

        actual.cashAccountTransactionSearchResponse.responseCommon.status mustBe
          expected.cashAccountTransactionSearchResponse.responseCommon.status

        actual.cashAccountTransactionSearchResponse.responseCommon.processingDate mustBe
          expected.cashAccountTransactionSearchResponse.responseCommon.processingDate
      }
    }

    "generate correct output for Json Writes" when {

      "response is success" in new Setup {
        Json.toJson(cashAccTranSearchResponseContainerOb) mustBe Json.parse(cashAccTranSearchResponseContainerJsString)
      }

      "EIS returns 201 to MDTP" in new Setup {
        Json.toJson(etmpStatus422AndMDTP201TranSearchResponseContainerOb) mustBe
          Json.parse(etmpStatus422AndMDTP201TranSearchResponseContainerJsString)
      }
    }
  }

  "PaymentAndWithdrawalContainer" should {
    import CashTransactionsResponse.paymentAndWithdrawalContainerFormat

    "generate correct output for Json Reads" in new Setup {
      Json.fromJson(Json.parse(paymentAndWithdrawalContainerObJsSring)) mustBe JsSuccess(
        paymentAndWithdrawalContainerOb
      )
    }

    "throw exception for invalid Json" in {
      val invalidJson = "{ \"openAmount\": \"300\", \"totalAmount\": \"600\" }"

      intercept[JsResultException] {
        Json.parse(invalidJson).as[PaymentAndWithdrawalContainer]
      }
    }

    "generate correct output for Json Writes" in new Setup {
      Json.toJson(paymentAndWithdrawalContainerOb) mustBe Json.parse(paymentAndWithdrawalContainerObJsSring)
    }
  }

  "TaxTypeDetail" should {
    import CashTransactionsResponse.taxTypeDetailFormat

    "generate correct output for Json Reads" in new Setup {
      Json.fromJson(Json.parse(taxTypeDetailObJsString)) mustBe JsSuccess(taxTypeDetailOb)
    }

    "throw exception for invalid Json" in {
      val invalidJson = "{ \"taxTy\": \"300\", \"totalAmount\": \"600\" }"

      intercept[JsResultException] {
        Json.parse(invalidJson).as[TaxTypeDetail]
      }
    }

    "generate correct output for Json Writes" in new Setup {
      Json.toJson(taxTypeDetailOb) mustBe Json.parse(taxTypeDetailObJsString)
    }
  }

  "TaxTypeContainer" should {
    import CashTransactionsResponse.taxTypeContainerFormat

    "generate correct output for Json Reads" in new Setup {
      Json.fromJson(Json.parse(taxTypeContainerObJsString)) mustBe JsSuccess(taxTypeContainerOb)
    }

    "throw exception for invalid Json" in {
      val invalidJson = "{ \"taxTop\": \"300\" }"

      intercept[JsResultException] {
        Json.parse(invalidJson).as[TaxTypeContainer]
      }
    }

    "generate correct output for Json Writes" in new Setup {
      Json.toJson(taxTypeContainerOb) mustBe Json.parse(taxTypeContainerObJsString)
    }
  }

  "TaxGroupDetail" should {
    import CashTransactionsResponse.taxGroupFormat

    "generate correct output for Json Reads" in new Setup {
      Json.fromJson(Json.parse(taxGroupDetailObJsString)) mustBe JsSuccess(taxGroupDetailOb)
    }

    "throw exception for invalid Json" in {
      val invalidJson = "{ \"openAmount\": \"300\", \"totalAmount\": \"600\" }"

      intercept[JsResultException] {
        Json.parse(invalidJson).as[TaxGroupDetail]
      }
    }

    "generate correct output for Json Writes" in new Setup {
      Json.toJson(taxGroupDetailOb) mustBe Json.parse(taxGroupDetailObJsString)
    }
  }

  "TaxGroupContainer" should {
    import CashTransactionsResponse.taxGroupContainerFormat

    "generate correct output for Json Reads" in new Setup {
      Json.fromJson(Json.parse(taxGroupContainerObJsString)) mustBe JsSuccess(taxGroupContainerOb)
    }

    "throw exception for invalid Json" in {
      val invalidJson = "{ \"taxDesc\": \"300\" }"

      intercept[JsResultException] {
        Json.parse(invalidJson).as[TaxGroupContainer]
      }
    }

    "generate correct output for Json Writes" in new Setup {
      Json.toJson(taxGroupContainerOb) mustBe Json.parse(taxGroupContainerObJsString)
    }
  }

  "DeclarationDetail" should {
    import CashTransactionsResponse.declarationFormat

    "generate correct output for Json Reads" in new Setup {
      Json.fromJson(Json.parse(declarationDetailObJsString)) mustBe JsSuccess(declarationDetailOb)
    }

    "throw exception for invalid Json" in {
      val invalidJson = "{ \"declarationType\": \"300\", \"totalAmount\": \"600\" }"

      intercept[JsResultException] {
        Json.parse(invalidJson).as[DeclarationDetail]
      }
    }

    "generate correct output for Json Writes" in new Setup {
      Json.toJson(declarationDetailOb) mustBe Json.parse(declarationDetailObJsString)
    }
  }

  "DeclarationContainer" should {
    import CashTransactionsResponse.declarationContainerFormat

    "generate correct output for Json Reads" in new Setup {
      Json.fromJson(Json.parse(declarationContainerObJsString)) mustBe JsSuccess(declarationContainerOb)
    }

    "throw exception for invalid Json" in {
      val invalidJson = "{ \"declaration123\": \"300\", \"totalAmount\": \"600\" }"

      intercept[JsResultException] {
        Json.parse(invalidJson).as[DeclarationContainer]
      }
    }

    "generate correct output for Json Writes" in new Setup {
      Json.toJson(declarationContainerOb) mustBe Json.parse(declarationContainerObJsString)
    }
  }

  "PendingTransactions" should {
    import CashTransactionsResponse.PendingTransactionsFormat

    "generate correct output for Json Reads" in new Setup {
      Json.fromJson(Json.parse(pendingTransactionsObJsString)) mustBe JsSuccess(pendingTransactionsOb)
    }

    "throw exception for invalid Json" in {
      val invalidJson = "{ \"openAmount\": \"300\", \"totalAmount\": \"600\" }"

      intercept[JsResultException] {
        Json.parse(invalidJson).as[PendingTransactions]
      }
    }

    "generate correct output for Json Writes" in new Setup {
      Json.toJson(pendingTransactionsOb) mustBe Json.parse(pendingTransactionsObJsString)
    }
  }

  "DailyStatementDetail" should {
    import CashTransactionsResponse.dailyStatementDetailFormat

    "generate correct output for Json Reads" in new Setup {
      Json.fromJson(Json.parse(dailyStatementDetailObJsString)) mustBe JsSuccess(dailyStatementDetailOb)
    }

    "throw exception for invalid Json" in {
      val invalidJson = "{ \"date\": \"300\", \"balance\": \"600\" }"

      intercept[JsResultException] {
        Json.parse(invalidJson).as[DailyStatementDetail]
      }
    }

    "generate correct output for Json Writes" in new Setup {
      Json.toJson(dailyStatementDetailOb) mustBe Json.parse(dailyStatementDetailObJsString)
    }
  }

  "DailyStatementContainer" should {
    import CashTransactionsResponse.dailyStatementFormat

    "generate correct output for Json Reads" in new Setup {
      Json.fromJson(Json.parse(dailyStatementContainerObJsString)) mustBe JsSuccess(dailyStatementContainerOb)
    }

    "throw exception for invalid Json" in {
      val invalidJson = "{ \"dailyStat1\": \"300\" }"

      intercept[JsResultException] {
        Json.parse(invalidJson).as[DailyStatementContainer]
      }
    }

    "generate correct output for Json Writes" in new Setup {
      Json.toJson(dailyStatementContainerOb) mustBe Json.parse(dailyStatementContainerObJsString)
    }
  }

  "CashTransactionsResponseDetail" should {
    import CashTransactionsResponse.responseDetailFormat

    "generate correct output for Json Reads" in new Setup {
      Json.fromJson(Json.parse(cashTransactionsResponseDetailObJsString)) mustBe JsSuccess(
        cashTransactionsResponseDetailOb
      )
    }

    "generate correct output for Json Writes" in new Setup {
      Json.toJson(cashTransactionsResponseDetailOb) mustBe Json.parse(cashTransactionsResponseDetailObJsString)
    }
  }

  "GetCashAccountTransactionListingResponse" should {
    import CashTransactionsResponse.getCashAccountTransactionListingResponseFormat

    "generate correct output for Json Reads" in new Setup {
      Json.fromJson(Json.parse(getCashAccountTransactionListingResponseObJsString)) mustBe JsSuccess(
        getCashAccountTransactionListingResponseOb
      )
    }

    "throw exception for invalid Json" in {
      val invalidJson = "{ \"response\": \"300\", \"details\": \"600\" }"

      intercept[JsResultException] {
        Json.parse(invalidJson).as[GetCashAccountTransactionListingResponse]
      }
    }

    "generate correct output for Json Writes" in new Setup {
      Json.toJson(getCashAccountTransactionListingResponseOb) mustBe Json.parse(
        getCashAccountTransactionListingResponseObJsString
      )
    }
  }

  "CashTransactionsResponse" should {
    import CashTransactionsResponse.cashTransactionsResponseFormat

    "generate correct output for Json Reads" in new Setup {
      Json.fromJson(Json.parse(cashTransactionsResponseObJsString)) mustBe JsSuccess(cashTransactionsResponseOb)
    }

    "throw exception for invalid Json" in {
      val invalidJson = "{ \"openAmount\": \"300\", \"totalAmount\": \"600\" }"

      intercept[JsResultException] {
        Json.parse(invalidJson).as[CashTransactionsResponse]
      }
    }

    "generate correct output for Json Writes" in new Setup {
      Json.toJson(cashTransactionsResponseOb) mustBe Json.parse(cashTransactionsResponseObJsString)
    }
  }

  "EoriData" should {
    import EoriData.format

    "generate correct output for Json Reads" in new Setup {
      Json.fromJson(Json.parse(eoriDataObJsString)) mustBe JsSuccess(eoriDataOb)
    }

    "throw exception for invalid Json" in {
      val invalidJson = "{ \"number\": \"300\", \"name1\": \"test\" }"

      intercept[JsResultException] {
        Json.parse(invalidJson).as[EoriData]
      }
    }

    "generate correct output for Json Writes" in new Setup {
      Json.toJson(eoriDataOb) mustBe Json.parse(eoriDataObJsString)
    }
  }

  "TaxTypeWithSecurity" should {
    import TaxTypeWithSecurity.format

    "generate correct output for Json Reads" in new Setup {
      Json.fromJson(Json.parse(taxTypeWithSecurityObJsString)) mustBe JsSuccess(taxTypeWithSecurityOb)
    }

    "throw exception for invalid Json" in {
      val invalidJson = "{ \"reason\": \"300\", \"totalAmount\": \"600\" }"

      intercept[JsResultException] {
        Json.parse(invalidJson).as[TaxTypeWithSecurity]
      }
    }

    "generate correct output for Json Writes" in new Setup {
      Json.toJson(taxTypeWithSecurityOb) mustBe Json.parse(taxTypeWithSecurityObJsString)
    }
  }

  "TaxTypeWithSecurityContainer" should {
    import TaxTypeWithSecurityContainer.format

    "generate correct output for Json Reads" in new Setup {
      Json.fromJson(Json.parse(taxTypeWithSecurityContainerObJsString)) mustBe JsSuccess(taxTypeWithSecurityContainerOb)
    }

    "throw exception for invalid Json" in {
      val invalidJson = "{ \"taxtype1\": \"300\" }"

      intercept[JsResultException] {
        Json.parse(invalidJson).as[TaxTypeWithSecurityContainer]
      }
    }

    "generate correct output for Json Writes" in new Setup {
      Json.toJson(taxTypeWithSecurityContainerOb) mustBe Json.parse(taxTypeWithSecurityContainerObJsString)
    }
  }

  "TaxGroup" should {
    import TaxGroup.format

    "generate correct output for Json Reads" in new Setup {
      Json.fromJson(Json.parse(taxGroupObJsString)) mustBe JsSuccess(taxGroupOb)
    }

    "throw exception for invalid Json" in {
      val invalidJson = "{ \"desc\": \"300\", \"totalAmount\": \"600\" }"

      intercept[JsResultException] {
        Json.parse(invalidJson).as[TaxGroup]
      }
    }

    "generate correct output for Json Writes" in new Setup {
      Json.toJson(taxGroupOb) mustBe Json.parse(taxGroupObJsString)
    }
  }

  "TaxGroupWrapper" should {
    import TaxGroupWrapper.format

    "generate correct output for Json Reads" in new Setup {
      Json.fromJson(Json.parse(taxGroupWrapperObJsString)) mustBe JsSuccess(taxGroupWrapperOb)
    }

    "throw exception for invalid Json" in {
      val invalidJson = "{ \"group\": \"300\", \"totalAmount\": \"600\" }"

      intercept[JsResultException] {
        Json.parse(invalidJson).as[TaxGroupWrapper]
      }
    }

    "generate correct output for Json Writes" in new Setup {
      Json.toJson(taxGroupWrapperOb) mustBe Json.parse(taxGroupWrapperObJsString)
    }
  }

  "DeclarationWrapper" should {
    import DeclarationWrapper.format

    "generate correct output for Json Reads" in new Setup {
      Json.fromJson(Json.parse(declarationWrapperObJsString)) mustBe JsSuccess(declarationWrapperOb)
    }

    "throw exception for invalid Json" in {
      val invalidJson = "{ \"declare\": \"300\" }"

      intercept[JsResultException] {
        Json.parse(invalidJson).as[DeclarationWrapper]
      }
    }

    "generate correct output for Json Writes" in new Setup {
      Json.toJson(declarationWrapperOb) mustBe Json.parse(declarationWrapperObJsString)
    }
  }

  "PaymentsWithdrawalsAndTransferContainer" should {
    import PaymentsWithdrawalsAndTransferContainer.format

    "generate correct output for Json Reads" in new Setup {
      Json.fromJson(Json.parse(paymentsWithdrawalsAndTransferContainerObJsString)) mustBe JsSuccess(
        paymentsWithdrawalsAndTransferContainerOb
      )
    }

    "throw exception for invalid Json" in {
      val invalidJson = "{ \"withdraw\": \"300\" }"

      intercept[JsResultException] {
        Json.parse(invalidJson).as[PaymentsWithdrawalsAndTransferContainer]
      }
    }

    "generate correct output for Json Writes" in new Setup {
      Json.toJson(paymentsWithdrawalsAndTransferContainerOb) mustBe Json.parse(
        paymentsWithdrawalsAndTransferContainerObJsString
      )
    }
  }

  "CashTransactionsResponseCommon" should {
    import CashTransactionsResponse.responseCommonFormat

    "generate correct output for Json Reads" in new Setup {
      Json.fromJson(Json.parse(cashTransactionsResponseCommonObJsString)) mustBe JsSuccess(
        cashTransactionsResponseCommonOb
      )
    }

    "throw exception for invalid Json" in {
      val invalidJson = "{ \"stat\": \"300\", \"totalAmount\": \"600\" }"

      intercept[JsResultException] {
        Json.parse(invalidJson).as[CashTransactionsResponseCommon]
      }
    }

    "generate correct output for Json Writes" in new Setup {
      Json.toJson(cashTransactionsResponseCommonOb) mustBe Json.parse(cashTransactionsResponseCommonObJsString)
    }
  }

  trait Setup {
    val processingDate = "2001-12-17T09:30:47Z"

    val paymentsWithdrawalsAndTransferOb: PaymentsWithdrawalsAndTransfer =
      PaymentsWithdrawalsAndTransfer(
        valueDate = DATE_STRING,
        postingDate = DATE_STRING,
        paymentReference = PAYMENT_REFERENCE,
        amount = AMOUNT,
        `type` = Payment,
        bankAccount = Some(BANK_ACCOUNT),
        sortCode = Some(SORT_CODE)
      )

    val paymentsWithdrawalAndTransferJsString: String =
      """{
        |"valueDate": "2024-05-28",
        |"postingDate": "2024-05-28",
        |"paymentReference": "CDSC1234567890",
        |"amount": 9999.99,
        |"type": "Payment",
        |"bankAccount": "1234567890987",
        |"sortCode": "123456789"
        |}""".stripMargin

    val paymentAndWithdrawalDetailJsString: String =
      """{
        |"amount": "999",
        |"type": "test",
        |"bankAccount": "1234567890987"
        |}""".stripMargin

    val cashAccountTransactionSearchResponseDetailOb: CashAccountTransactionSearchResponseDetail =
      CashAccountTransactionSearchResponseDetail(
        CAN,
        eoriDetails = Seq(EoriDataContainer(EoriData(EORI_NUMBER, EORI_DATA_NAME))),
        declarations = None,
        paymentsWithdrawalsAndTransfers = Some(
          Seq(
            PaymentsWithdrawalsAndTransferContainer(
              PaymentsWithdrawalsAndTransfer(
                DATE_STRING,
                DATE_STRING,
                PAYMENT_REFERENCE,
                AMOUNT,
                Payment,
                Some(BANK_ACCOUNT),
                Some(SORT_CODE)
              )
            )
          )
        )
      )

    val declarationWrapper: DeclarationWrapper = DeclarationWrapper(
      Declaration(
        DECLARATION_ID,
        DECLARANT_EORI_NUMBER,
        Some(DECLARANT_REF),
        Some(C18_OR_OVER_PAYMENT_REFERENCE),
        IMPORTERS_EORI_NUMBER,
        DATE_STRING,
        DATE_STRING,
        AMOUNT,
        taxGroups = Seq(
          TaxGroupWrapper(
            TaxGroup(
              "Customs",
              AMOUNT,
              Seq(TaxTypeWithSecurityContainer(TaxTypeWithSecurity(Some("CRQ"), "A00", AMOUNT)))
            )
          )
        )
      )
    )

    val cashAccountTransactionSearchResponseDetailWithDeclarationOb: CashAccountTransactionSearchResponseDetail =
      CashAccountTransactionSearchResponseDetail(
        CAN,
        eoriDetails = Seq(EoriDataContainer(EoriData(EORI_NUMBER, EORI_DATA_NAME))),
        declarations = Some(Seq(declarationWrapper)),
        paymentsWithdrawalsAndTransfers = None
      )

    val cashAccountTransactionSearchResponseDetailJsString: String =
      """{
        |"can": "12345678909",
        |"eoriDetails": [
        |{
        |"eoriData": {
        |"eoriNumber": "GB123456789",
        |"name": "test"
        |}
        |}
        |],
        |"paymentsWithdrawalsAndTransfers": [
        |{
        |"paymentsWithdrawalsAndTransfer": {
        |"valueDate": "2024-05-28",
        |"postingDate": "2024-05-28",
        |"paymentReference": "CDSC1234567890",
        |"amount": 9999.99,
        |"type": "Payment",
        |"bankAccount": "1234567890987",
        |"sortCode": "123456789"
        |}
        |}
        |]
        |}""".stripMargin

    val resCommonOb: CashTransactionsResponseCommon = CashTransactionsResponseCommon(
      status = "OK",
      statusText = None,
      processingDate = processingDate,
      returnParameters = None
    )

    val cashAccountTransactionSearchResponseOb: CashAccountTransactionSearchResponse =
      CashAccountTransactionSearchResponse(resCommonOb, Some(cashAccountTransactionSearchResponseDetailOb))

    val cashAccountTransactionSearchResponseWithDeclarationOb: CashAccountTransactionSearchResponse =
      CashAccountTransactionSearchResponse(
        resCommonOb,
        Some(cashAccountTransactionSearchResponseDetailWithDeclarationOb)
      )

    val cashAccountTransactionSearchResponseJsString: String =
      """{
        |"responseCommon": {
        |"status": "OK",
        |"processingDate": "2001-12-17T09:30:47Z"
        |},
        |"responseDetail": {
        |"can": "12345678909",
        |"eoriDetails": [
        |{
        |"eoriData": {
        |"eoriNumber": "GB123456789",
        |"name": "test"
        |}
        |}
        |],
        |"paymentsWithdrawalsAndTransfers": [
        |{
        |"paymentsWithdrawalsAndTransfer": {
        |"valueDate": "2024-05-28",
        |"postingDate": "2024-05-28",
        |"paymentReference": "CDSC1234567890",
        |"amount": 9999.99,
        |"type": "Payment",
        |"bankAccount": "1234567890987",
        |"sortCode": "123456789"
        |}
        |}
        |]
        |}
        |}""".stripMargin

    val cashAccTranSearchResponseContainerOb: CashAccountTransactionSearchResponseContainer =
      CashAccountTransactionSearchResponseContainer(cashAccountTransactionSearchResponseOb)

    val cashAccTranSearchResponseContainerJsString: String =
      """{
        |"cashAccountTransactionSearchResponse": {
        |"responseCommon": {
        |"status": "OK",
        |"processingDate": "2001-12-17T09:30:47Z"
        |},
        |"responseDetail": {
        |"can": "12345678909",
        |"eoriDetails": [
        |{
        |"eoriData": {
        |"eoriNumber": "GB123456789",
        |"name": "test"
        |}
        |}
        |],
        |"paymentsWithdrawalsAndTransfers": [
        |{
        |"paymentsWithdrawalsAndTransfer": {
        |"valueDate": "2024-05-28",
        |"postingDate": "2024-05-28",
        |"paymentReference": "CDSC1234567890",
        |"amount": 9999.99,
        |"type": "Payment",
        |"bankAccount": "1234567890987",
        |"sortCode": "123456789"
        |}
        |}
        |]
        |}
        |}
        |}""".stripMargin

    val resCommonForETMP422AndMDTP201Ob: CashTransactionsResponseCommon = CashTransactionsResponseCommon(
      status = "OK",
      statusText = Some("001-Invalid Cash Account"),
      processingDate = "2024-01-17T09:30:47Z",
      returnParameters = Some(Seq(ReturnParameter("POSITION", "FAIL")).toArray)
    )

    val cashAccTransResponseObForETMP422AndMDTP201: CashAccountTransactionSearchResponse =
      CashAccountTransactionSearchResponse(resCommonForETMP422AndMDTP201Ob)

    val etmpStatus422AndMDTP201TranSearchResponseContainerOb: CashAccountTransactionSearchResponseContainer =
      CashAccountTransactionSearchResponseContainer(cashAccTransResponseObForETMP422AndMDTP201)

    val etmpStatus422AndMDTP201TranSearchResponseContainerJsString: String =
      """{
        |"cashAccountTransactionSearchResponse": {
        |"responseCommon": {
        |"status": "OK",
        |"statusText": "001-Invalid Cash Account",
        |"processingDate": "2024-01-17T09:30:47Z",
        |"returnParameters": [
        |{
        |"paramName": "POSITION",
        |"paramValue": "FAIL"
        |}
        |]
        |}
        |}
        |}""".stripMargin

    val cashAccTranSearchResponseContainerWithDeclarationJsString: String =
      """{
        |"cashAccountTransactionSearchResponse":
        |{"responseCommon":
        |{"status":"OK",
        |"processingDate":"2001-12-17T09:30:47Z"
        |},
        |"responseDetail":
        |{"can":"12345678909",
        |"eoriDetails":[
        |{"eoriData":{"eoriNumber":"GB123456789","name":"test"}}
        |],
        |"declarations":
        |[{"declaration":
        |{"declarationID":"24GB123456789",
        |"declarantEORINumber":"GB12345678",
        |"declarantRef":"1234567890abcdefgh",
        |"c18OrOverpaymentReference":"RPCSCCCS1",
        |"importersEORINumber":"GB1234567",
        |"postingDate":"2024-05-28",
        |"acceptanceDate":"2024-05-28",
        |"amount":9999.99,
        |"taxGroups":[
        |{"taxGroup":
        |{"taxGroupDescription":"Customs",
        |"amount":9999.99,
        |"taxTypes":[{"taxType":{"reasonForSecurity":"CRQ","taxTypeID":"A00","amount":9999.99}}]}}]}}]}}}""".stripMargin

    val cashAccTranSearchResponseContainerWithDeclarationOb: CashAccountTransactionSearchResponseContainer =
      CashAccountTransactionSearchResponseContainer(cashAccountTransactionSearchResponseWithDeclarationOb)

    val twoHundred = "200.00"
    val hundred    = "100.00"

    val paymentAndWithdrawalDetailOb: PaymentAndWithdrawalDetail =
      PaymentAndWithdrawalDetail("999", "test", Some("1234567890987"))

    lazy val paymentAndWithdrawalContainerOb: PaymentAndWithdrawalContainer = PaymentAndWithdrawalContainer(
      paymentAndWithdrawalDetailOb
    )

    lazy val taxTypeDetailOb: TaxTypeDetail =
      TaxTypeDetail(reasonForSecurity = Some("b"), taxTypeID = "a", amount = hundred)

    lazy val taxTypeContainerOb: TaxTypeContainer = TaxTypeContainer(taxTypeDetailOb)
    lazy val taxGroupDetailOb: TaxGroupDetail     =
      TaxGroupDetail(taxGroupDescription = "Customs", amount = twoHundred, taxTypes = Seq(taxTypeContainerOb))

    lazy val taxGroupContainerOb: TaxGroupContainer = TaxGroupContainer(taxGroupDetailOb)
    lazy val declarationDetailOb: DeclarationDetail = DeclarationDetail(
      declarationID = "DeclarationID",
      importerEORINumber = Some(EORI("importerEORI123")),
      declarantEORINumber = EORI("declarantEORI123"),
      declarantReference = Some("reference123"),
      postingDate = "2024-01-01",
      amount = "500",
      taxGroups = Seq(taxGroupContainerOb)
    )

    lazy val declarationContainerOb: DeclarationContainer = DeclarationContainer(declarationDetailOb)
    lazy val pendingTransactionsOb: PendingTransactions   = PendingTransactions(Seq(declarationContainerOb))

    lazy val dailyStatementDetailOb: DailyStatementDetail = DailyStatementDetail(
      DATE_STRING,
      "10000",
      "9000",
      Some(Seq(declarationContainerOb)),
      Some(Seq(paymentAndWithdrawalContainerOb))
    )

    lazy val dailyStatementContainerOb: DailyStatementContainer = DailyStatementContainer(dailyStatementDetailOb)

    lazy val cashTransactionsResponseDetailOb: CashTransactionsResponseDetail =
      CashTransactionsResponseDetail(
        dailyStatements = Some(Seq(dailyStatementContainerOb)),
        pendingTransactions = Some(pendingTransactionsOb),
        maxTransactionsExceeded = Some(false)
      )

    lazy val getCashAccountTransactionListingResponseOb: GetCashAccountTransactionListingResponse =
      GetCashAccountTransactionListingResponse(resCommonOb, Some(cashTransactionsResponseDetailOb))

    lazy val cashTransactionsResponseOb: CashTransactionsResponse = CashTransactionsResponse(
      getCashAccountTransactionListingResponseOb
    )

    lazy val eoriDataOb: EoriData                   = EoriData(EORI_NUMBER, EORI_DATA_NAME)
    lazy val eoriDataContainerOb: EoriDataContainer = EoriDataContainer(eoriDataOb)

    lazy val taxTypeWithSecurityOb: TaxTypeWithSecurity = TaxTypeWithSecurity(Some("CRQ"), "A00", AMOUNT)

    lazy val taxTypeWithSecurityContainerOb: TaxTypeWithSecurityContainer = TaxTypeWithSecurityContainer(
      taxTypeWithSecurityOb
    )

    lazy val taxGroupOb: TaxGroup = TaxGroup("Customs", AMOUNT, Seq(taxTypeWithSecurityContainerOb))

    lazy val taxGroupWrapperOb: TaxGroupWrapper = TaxGroupWrapper(taxGroupOb)
    lazy val declarationOb: Declaration         = Declaration(
      DECLARATION_ID,
      EORI_NUMBER,
      Some(DECLARANT_REF),
      Some(C18_OR_OVER_PAYMENT_REFERENCE),
      IMPORTERS_EORI_NUMBER,
      DATE_STRING,
      DATE_STRING,
      AMOUNT,
      Seq(taxGroupWrapperOb)
    )

    lazy val declarationWrapperOb: DeclarationWrapper = DeclarationWrapper(declarationOb)

    lazy val paymentsWithdrawalsAndTransferContainerOb: PaymentsWithdrawalsAndTransferContainer =
      PaymentsWithdrawalsAndTransferContainer(paymentsWithdrawalsAndTransferOb)

    lazy val cashTransactionsResponseCommonOb: CashTransactionsResponseCommon = CashTransactionsResponseCommon(
      status = "OK",
      statusText = Some("001-Invalid Cash Account"),
      processingDate = "2024-01-17T09:30:47Z",
      returnParameters = None
    )

    lazy val paymentAndWithdrawalContainerObJsSring: String =
      """{"paymentAndWithdrawal":{"amount":"999","type":"test","bankAccount":"1234567890987"}}""".stripMargin

    lazy val taxTypeDetailObJsString: String =
      """{"reasonForSecurity":"b","taxTypeID":"a","amount":"100.00"}""".stripMargin

    lazy val taxTypeContainerObJsString: String =
      """{"taxType":{"reasonForSecurity":"b","taxTypeID":"a","amount":"100.00"}}""".stripMargin

    lazy val taxGroupDetailObJsString: String =
      """{"taxGroupDescription":"Customs",
        |"amount":"200.00",
        |"taxTypes":[{"taxType":{"reasonForSecurity":"b","taxTypeID":"a","amount":"100.00"}}]
        |}""".stripMargin

    lazy val taxGroupContainerObJsString: String =
      """{"taxGroup":{
        |"taxGroupDescription":"Customs",
        |"amount":"200.00",
        |"taxTypes":[{"taxType":{"reasonForSecurity":"b","taxTypeID":"a","amount":"100.00"}}]
        |}}""".stripMargin

    lazy val declarationDetailObJsString: String =
      """{
        |"declarationID":"DeclarationID",
        |"importerEORINumber":"importerEORI123",
        |"declarantEORINumber":"declarantEORI123",
        |"declarantReference":"reference123",
        |"postingDate":"2024-01-01",
        |"amount":"500",
        |"taxGroups":[
        |{"taxGroup":{"taxGroupDescription":"Customs",
        |"amount":"200.00",
        |"taxTypes":[{"taxType":{"reasonForSecurity":"b","taxTypeID":"a","amount":"100.00"}}]
        |}
        |}]}""".stripMargin

    lazy val declarationContainerObJsString: String =
      """{"declaration":{
        |"declarationID":"DeclarationID",
        |"importerEORINumber":"importerEORI123",
        |"declarantEORINumber":"declarantEORI123",
        |"declarantReference":"reference123",
        |"postingDate":"2024-01-01",
        |"amount":"500",
        |"taxGroups":[{"taxGroup":{
        |"taxGroupDescription":"Customs",
        |"amount":"200.00",
        |"taxTypes":[{"taxType":{"reasonForSecurity":"b","taxTypeID":"a","amount":"100.00"}}]
        |}
        |}
        |]}}""".stripMargin

    lazy val pendingTransactionsObJsString: String =
      """{"declarations":[
        |{"declaration":{
        |"declarationID":"DeclarationID",
        |"importerEORINumber":"importerEORI123",
        |"declarantEORINumber":"declarantEORI123",
        |"declarantReference":"reference123",
        |"postingDate":"2024-01-01",
        |"amount":"500",
        |"taxGroups":[{"taxGroup":{
        |"taxGroupDescription":"Customs",
        |"amount":"200.00",
        |"taxTypes":[{"taxType":{"reasonForSecurity":"b","taxTypeID":"a","amount":"100.00"}}]
        |}}
        |]}}]}""".stripMargin

    lazy val dailyStatementDetailObJsString: String =
      """{
        |"date":"2024-05-28",
        |"openingBalance":"10000",
        |"closingBalance":"9000",
        |"declarations":[{
        |"declaration":{
        |"declarationID":"DeclarationID",
        |"importerEORINumber":"importerEORI123",
        |"declarantEORINumber":"declarantEORI123",
        |"declarantReference":"reference123",
        |"postingDate":"2024-01-01",
        |"amount":"500",
        |"taxGroups":[{"taxGroup":{
        |"taxGroupDescription":"Customs",
        |"amount":"200.00",
        |"taxTypes":[{"taxType":{"reasonForSecurity":"b","taxTypeID":"a","amount":"100.00"}}]
        |}}]}}],
        |"paymentsAndWithdrawals":[
        |{"paymentAndWithdrawal":{"amount":"999","type":"test","bankAccount":"1234567890987"}}]
        |}""".stripMargin

    lazy val dailyStatementContainerObJsString: String =
      """{"dailyStatement":{
        |"date":"2024-05-28",
        |"openingBalance":"10000",
        |"closingBalance":"9000",
        |"declarations":[{"declaration":{
        |"declarationID":"DeclarationID",
        |"importerEORINumber":"importerEORI123",
        |"declarantEORINumber":"declarantEORI123",
        |"declarantReference":"reference123",
        |"postingDate":"2024-01-01",
        |"amount":"500",
        |"taxGroups":[{
        |"taxGroup":{"taxGroupDescription":"Customs",
        |"amount":"200.00",
        |"taxTypes":[{"taxType":{"reasonForSecurity":"b","taxTypeID":"a","amount":"100.00"}}]}}]}}],
        |"paymentsAndWithdrawals":[{
        |"paymentAndWithdrawal":{"amount":"999","type":"test","bankAccount":"1234567890987"}}]
        |}}""".stripMargin

    lazy val cashTransactionsResponseDetailObJsString: String =
      """{"dailyStatements":[{
        |"dailyStatement":{
        |"date":"2024-05-28",
        |"openingBalance":"10000",
        |"closingBalance":"9000",
        |"declarations":[{
        |"declaration":{
        |"declarationID":"DeclarationID",
        |"importerEORINumber":"importerEORI123",
        |"declarantEORINumber":"declarantEORI123",
        |"declarantReference":"reference123",
        |"postingDate":"2024-01-01",
        |"amount":"500",
        |"taxGroups":[{
        |"taxGroup":{
        |"taxGroupDescription":"Customs",
        |"amount":"200.00",
        |"taxTypes":[{"taxType":{"reasonForSecurity":"b","taxTypeID":"a","amount":"100.00"}}]}}]}}],
        |"paymentsAndWithdrawals":[{
        |"paymentAndWithdrawal":{"amount":"999","type":"test","bankAccount":"1234567890987"}}]}}],
        |"pendingTransactions":{
        |"declarations":[{
        |"declaration":{
        |"declarationID":"DeclarationID",
        |"importerEORINumber":"importerEORI123",
        |"declarantEORINumber":"declarantEORI123",
        |"declarantReference":"reference123",
        |"postingDate":"2024-01-01",
        |"amount":"500",
        |"taxGroups":[{
        |"taxGroup":{
        |"taxGroupDescription":"Customs",
        |"amount":"200.00",
        |"taxTypes":[{"taxType":{"reasonForSecurity":"b","taxTypeID":"a","amount":"100.00"}}]
        |}}]}}]},"maxTransactionsExceeded":false}""".stripMargin

    lazy val getCashAccountTransactionListingResponseObJsString: String =
      """{"responseCommon":{"status":"OK","processingDate":"2001-12-17T09:30:47Z"},
        |"responseDetail":{
        |"dailyStatements":[{
        |"dailyStatement":{
        |"date":"2024-05-28",
        |"openingBalance":"10000",
        |"closingBalance":"9000",
        |"declarations":[{
        |"declaration":{
        |"declarationID":"DeclarationID",
        |"importerEORINumber":"importerEORI123",
        |"declarantEORINumber":"declarantEORI123",
        |"declarantReference":"reference123",
        |"postingDate":"2024-01-01",
        |"amount":"500",
        |"taxGroups":[{
        |"taxGroup":{
        |"taxGroupDescription":"Customs",
        |"amount":"200.00",
        |"taxTypes":[{"taxType":{"reasonForSecurity":"b","taxTypeID":"a","amount":"100.00"}}]}}]}}],
        |"paymentsAndWithdrawals":[{
        |"paymentAndWithdrawal":{
        |"amount":"999",
        |"type":"test",
        |"bankAccount":"1234567890987"}}]}}],
        |"pendingTransactions":{
        |"declarations":[{
        |"declaration":{
        |"declarationID":"DeclarationID",
        |"importerEORINumber":"importerEORI123",
        |"declarantEORINumber":"declarantEORI123",
        |"declarantReference":"reference123",
        |"postingDate":"2024-01-01",
        |"amount":"500",
        |"taxGroups":[{
        |"taxGroup":{
        |"taxGroupDescription":"Customs",
        |"amount":"200.00",
        |"taxTypes":[{"taxType":{"reasonForSecurity":"b","taxTypeID":"a","amount":"100.00"}}]}}]}}]},
        |"maxTransactionsExceeded":false}}""".stripMargin

    lazy val cashTransactionsResponseObJsString: String =
      """{
        |"getCashAccountTransactionListingResponse":{
        |"responseCommon":{"status":"OK","processingDate":"2001-12-17T09:30:47Z"},
        |"responseDetail":{
        |"dailyStatements":[{
        |"dailyStatement":{
        |"date":"2024-05-28",
        |"openingBalance":"10000",
        |"closingBalance":"9000",
        |"declarations":[{
        |"declaration":{
        |"declarationID":"DeclarationID",
        |"importerEORINumber":"importerEORI123",
        |"declarantEORINumber":"declarantEORI123",
        |"declarantReference":"reference123",
        |"postingDate":"2024-01-01",
        |"amount":"500",
        |"taxGroups":[{
        |"taxGroup":{
        |"taxGroupDescription":"Customs",
        |"amount":"200.00",
        |"taxTypes":[{"taxType":{"reasonForSecurity":"b","taxTypeID":"a","amount":"100.00"}}]}}]}}],
        |"paymentsAndWithdrawals":[{
        |"paymentAndWithdrawal":{
        |"amount":"999",
        |"type":"test",
        |"bankAccount":"1234567890987"}}]}}],
        |"pendingTransactions":{
        |"declarations":[{
        |"declaration":{
        |"declarationID":"DeclarationID",
        |"importerEORINumber":"importerEORI123",
        |"declarantEORINumber":"declarantEORI123",
        |"declarantReference":"reference123",
        |"postingDate":"2024-01-01",
        |"amount":"500",
        |"taxGroups":[{
        |"taxGroup":{
        |"taxGroupDescription":"Customs",
        |"amount":"200.00",
        |"taxTypes":[{"taxType":{"reasonForSecurity":"b","taxTypeID":"a","amount":"100.00"}}]}}]}}]},
        |"maxTransactionsExceeded":false}}}""".stripMargin

    lazy val eoriDataObJsString: String = """{"eoriNumber":"GB123456789","name":"test"}""".stripMargin

    lazy val taxTypeWithSecurityObJsString: String =
      """{"reasonForSecurity":"CRQ","taxTypeID":"A00","amount":9999.99}""".stripMargin

    lazy val taxTypeWithSecurityContainerObJsString: String =
      """{"taxType":{"reasonForSecurity":"CRQ","taxTypeID":"A00","amount":9999.99}}""".stripMargin

    lazy val taxGroupObJsString: String =
      """{
        |"taxGroupDescription":"Customs",
        |"amount":9999.99,
        |"taxTypes":[{"taxType":{"reasonForSecurity":"CRQ","taxTypeID":"A00","amount":9999.99}}]}""".stripMargin

    lazy val taxGroupWrapperObJsString: String =
      """{"taxGroup":{
        |"taxGroupDescription":"Customs",
        |"amount":9999.99,
        |"taxTypes":[{"taxType":{"reasonForSecurity":"CRQ","taxTypeID":"A00","amount":9999.99}}]}}""".stripMargin

    lazy val declarationObJsString: String =
      """{"declaration":{
        |"declarationID":"24GB123456789",
        |"declarantEORINumber":"GB123456789",
        |"declarantRef":"1234567890abcdefgh",
        |"c18OrOverpaymentReference":"RPCSCCCS1",
        |"importersEORINumber":"GB1234567",
        |"postingDate":"2024-05-28",
        |"acceptanceDate":"2024-05-28",
        |"amount":9999.99,
        |"taxGroups":[{"taxGroup":{
        |"taxGroupDescription":"Customs",
        |"amount":9999.99,
        |"taxTypes":[{"taxType":{"reasonForSecurity":"CRQ","taxTypeID":"A00","amount":9999.99}}]
        |}}]}}""".stripMargin

    lazy val declarationWrapperObJsString: String =
      """{"declaration":{
        |"declarationID":"24GB123456789",
        |"declarantEORINumber":"GB123456789",
        |"declarantRef":"1234567890abcdefgh",
        |"c18OrOverpaymentReference":"RPCSCCCS1",
        |"importersEORINumber":"GB1234567",
        |"postingDate":"2024-05-28",
        |"acceptanceDate":"2024-05-28",
        |"amount":9999.99,
        |"taxGroups":[{"taxGroup":{
        |"taxGroupDescription":"Customs",
        |"amount":9999.99,
        |"taxTypes":[{"taxType":{"reasonForSecurity":"CRQ","taxTypeID":"A00","amount":9999.99}}]
        |}}]}}""".stripMargin

    lazy val paymentsWithdrawalsAndTransferContainerObJsString: String =
      """{"paymentsWithdrawalsAndTransfer":{
        |"valueDate":"2024-05-28",
        |"postingDate":"2024-05-28",
        |"paymentReference":"CDSC1234567890",
        |"amount":9999.99,"type":"Payment",
        |"bankAccount":"1234567890987",
        |"sortCode":"123456789"
        |}}""".stripMargin

    lazy val cashTransactionsResponseCommonObJsString: String =
      """{"status":"OK","statusText":"001-Invalid Cash Account","processingDate":"2024-01-17T09:30:47Z"}""".stripMargin
  }
}
