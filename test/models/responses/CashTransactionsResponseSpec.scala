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

    "generate correct output for Json Writes" in new SetUp {
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

    "generate correct output for Json Reads" in new SetUp {
      import models.responses.PaymentsWithdrawalsAndTransfer.format

      Json.fromJson(Json.parse(paymentsWithdrawalAndTransferJsString)) mustBe
        JsSuccess(paymentsWithdrawalsAndTransferOb)
    }

    "generate correct output for Json Writes" in new SetUp {
      import models.responses.PaymentsWithdrawalsAndTransfer.format

      Json.toJson(paymentsWithdrawalsAndTransferOb) mustBe Json.parse(paymentsWithdrawalAndTransferJsString)
    }
  }

  "CashAccountTransactionSearchResponseDetail" should {

    "generate correct output for Json Reads" in new SetUp {
      import models.responses.CashAccountTransactionSearchResponseDetail.format

      Json.fromJson(Json.parse(cashAccountTransactionSearchResponseDetailJsString)) mustBe
        JsSuccess(cashAccountTransactionSearchResponseDetailOb)
    }

    "generate correct output for Json Writes" in new SetUp {

      Json.toJson(cashAccountTransactionSearchResponseDetailOb) mustBe
        Json.parse(cashAccountTransactionSearchResponseDetailJsString)
    }
  }

  "CashAccountTransactionSearchResponse" should {

    "generate correct output for Json Reads" in new SetUp {
      import models.responses.CashAccountTransactionSearchResponse.format

      Json.fromJson(Json.parse(cashAccountTransactionSearchResponseJsString)) mustBe
        JsSuccess(cashAccountTransactionSearchResponseOb)
    }

    "generate correct output for Json Writes" in new SetUp {
      Json.toJson(cashAccountTransactionSearchResponseOb) mustBe
        Json.parse(cashAccountTransactionSearchResponseJsString)
    }
  }

  "CashAccountTransactionSearchResponseWrapper" should {

    "generate correct output for Json Reads" in new SetUp {
      import models.responses.CashAccountTransactionSearchResponseWrapper.format

      Json.fromJson(Json.parse(cashAccTranSearchResponseWrapperJsString)) mustBe
        JsSuccess(cashAccTranSearchResponseWrapperOb)
    }

    "generate correct output for Json Writes" in new SetUp {
      Json.toJson(cashAccTranSearchResponseWrapperOb) mustBe Json.parse(cashAccTranSearchResponseWrapperJsString)
    }
  }

  trait SetUp {
    val dateString = "2024-05-28"
    val processingDate = "2001-12-17T09:30:47Z"

    val paymentReference = "CDSC1234567890"
    val amount = 9999.99
    val bankAccount = "1234567890987"
    val sortCode = "123456789"
    val can = "12345678909"
    val eoriNumber = "GB123456789"
    val eoriDataName = "test"

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
      maxTransactionsExceeded = None)

    val cashAccountTransactionSearchResponseOb: CashAccountTransactionSearchResponse =
      CashAccountTransactionSearchResponse(resCommonOb, Some(cashAccountTransactionSearchResponseDetailOb))

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

    val cashAccTranSearchResponseWrapperOb: CashAccountTransactionSearchResponseWrapper =
      CashAccountTransactionSearchResponseWrapper(cashAccountTransactionSearchResponseOb)

    val cashAccTranSearchResponseWrapperJsString: String =
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
  }
}
