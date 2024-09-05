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

import models.responses.PaymentType._
import play.api.libs.json.{JsString, JsSuccess, Json}
import utils.SpecBase

class CashTransactionsResponseSpec extends SpecBase {

  "PaymentAndWithdrawalDetail" should {

    "generate correct output for Json Writes" in new Setup {

      import models.responses.CashTransactionsResponse.paymentAndWithdrawalDetailFormat

      Json.toJson(PaymentAndWithdrawalDetail("999", "test", Some("1234567890987"))) mustBe
        Json.parse(paymentAndWithdrawalDetailJsString)
    }
  }

  "PaymentType" should {

    "generate correct output for Json Reads" in {
      val incomingJsValuePayment: JsString = JsString("Payment")
      val incomingJsValueWithdrawal: JsString = JsString("Withdrawal")
      val incomingJsValueTransfer: JsString = JsString("Transfer")

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

        val expected: CashAccountTransactionSearchResponseContainer = etmpStatus422AndMDTP201TranSearchResponseContainerOb

        actual.cashAccountTransactionSearchResponse.responseCommon.returnParameters.get mustBe
          expected.cashAccountTransactionSearchResponse.responseCommon.returnParameters.get

        actual.cashAccountTransactionSearchResponse.responseCommon.status mustBe
          expected.cashAccountTransactionSearchResponse.responseCommon.status

        actual.cashAccountTransactionSearchResponse.responseCommon.processingDate mustBe
          expected.cashAccountTransactionSearchResponse.responseCommon.processingDate

        actual.cashAccountTransactionSearchResponse.responseCommon.maxTransactionsExceeded mustBe empty
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

  trait Setup {
    val dateString = "2024-05-28"
    val processingDate = "2001-12-17T09:30:47Z"

    val paymentReference = "CDSC1234567890"
    val amount = 9999.99
    val bankAccount = "1234567890987"
    val sortCode = "123456789"
    val can = "12345678909"
    val eoriNumber = "GB123456789"
    val eoriDataName = "test"

    val declarationID = "24GB123456789"
    val declarantEORINumber = "GB12345678"
    val declarantRef = "1234567890abcdefgh"
    val c18OrOverpaymentReference = "RPCSCCCS1"
    val importersEORINumber = "GB1234567"

    val paymentsWithdrawalsAndTransferOb: PaymentsWithdrawalsAndTransfer =
      PaymentsWithdrawalsAndTransfer(
        valueDate = dateString,
        postingDate = dateString,
        paymentReference = paymentReference,
        amount = amount,
        `type` = Payment,
        bankAccount = Some(bankAccount),
        sortCode = Some(sortCode)
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
        can,
        eoriDetails = Seq(EoriDataContainer(EoriData(eoriNumber, eoriDataName))),
        declarations = None,
        paymentsWithdrawalsAndTransfers =
          Some(
            Seq(
              PaymentsWithdrawalsAndTransferContainer(PaymentsWithdrawalsAndTransfer(
                dateString,
                dateString,
                paymentReference,
                amount,
                Payment,
                Some(bankAccount),
                Some(sortCode)
              ))
            ))
      )

    val declarationWrapper: DeclarationWrapper = DeclarationWrapper(
      Declaration(
        declarationID,
        declarantEORINumber,
        Some(declarantRef),
        Some(c18OrOverpaymentReference),
        importersEORINumber,
        dateString,
        dateString,
        amount,
        taxGroups =
          Seq(
            TaxGroupWrapper(
              TaxGroup(
                "Customs",
                amount,
                Seq(TaxTypeWithSecurityContainer(TaxTypeWithSecurity(Some("CRQ"), "A00", amount)))))))
    )

    val cashAccountTransactionSearchResponseDetailWithDeclarationOb: CashAccountTransactionSearchResponseDetail =
      CashAccountTransactionSearchResponseDetail(
        can,
        eoriDetails = Seq(EoriDataContainer(EoriData(eoriNumber, eoriDataName))),
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
      maxTransactionsExceeded = None,
      returnParameters = None)

    val cashAccountTransactionSearchResponseOb: CashAccountTransactionSearchResponse =
      CashAccountTransactionSearchResponse(resCommonOb, Some(cashAccountTransactionSearchResponseDetailOb))

    val cashAccountTransactionSearchResponseWithDeclarationOb: CashAccountTransactionSearchResponse =
      CashAccountTransactionSearchResponse(resCommonOb, Some(cashAccountTransactionSearchResponseDetailWithDeclarationOb))

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
      maxTransactionsExceeded = None,
      returnParameters = Some(Seq(ReturnParameter("POSITION", "FAIL")).toArray))

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
  }
}