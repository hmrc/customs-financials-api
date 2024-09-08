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

package controllers

import domain.CashDailyStatement._
import domain._
import models.requests.SearchType.P
import models.requests.{CashAccountPaymentDetails, CashAccountTransactionSearchRequestDetails}
import models.responses.ErrorCode.{code400, code500}
import models.responses.EtmpErrorCode.code001
import models.responses.PaymentType.Payment
import models.responses._
import models.{EORI, ExceededThresholdErrorException, NoAssociatedDataException}
import org.mockito.ArgumentMatchers.{eq => is}
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.{JsValue, Json}
import play.api.test.Helpers._
import play.api.test._
import play.api.{Application, inject}
import services.CashTransactionsService
import utils.SpecBase
import utils.TestData.{DAY_1, MONTH_1, MONTH_6, YEAR_2020}

import java.time.LocalDate
import scala.concurrent.{ExecutionContext, Future}

class CashTransactionsControllerSpec extends SpecBase {

  "CashTransactionControllerSpec.getSummary" should {

    "delegate to the service and return a list of cash daily statements with a 200 status code" in new Setup {

      import domain.Declaration

      val aListOfCashDailyStatements: Seq[CashDailyStatement] =
        Seq(
          CashDailyStatement("date",
            "openingBalance",
            "closingBalance",
            Seq(Declaration("mrn",
              Some(EORI("importerEORI")),
              EORI("declarantEori"),
              Some("declarantReference"),
              "postingDate",
              "amount",
              Nil)),
            Seq(Transaction("12.34", "Payment", None), Transaction("12.34", "Withdrawal", Some("77665544")))))

      val aListOfPendingTransactions: Seq[Declaration] =
        Seq(Declaration("pendingDeclarationID",
          Some(EORI("pendingImporterEORI")),
          EORI("pendingDeclarantEORINumber"),
          Some("pendingDeclarantReference"),
          "pendingPostingDate",
          "pendingAmount",
          Nil))

      val expectedCashTransactions: CashTransactions =
        CashTransactions(aListOfPendingTransactions, aListOfCashDailyStatements)

      when(mockCashTransactionsService.retrieveCashTransactionsSummary(is("can1"), is(fromDate), is(toDate)))
        .thenReturn(Future.successful(Right(expectedCashTransactions)))

      running(app) {
        val result = route(app, getSummaryRequest).value

        status(result) mustBe OK
        contentAsJson(result) mustBe Json.toJson(expectedCashTransactions)
      }
    }

    "return not found with no associated data" in new Setup {
      when(mockCashTransactionsService.retrieveCashTransactionsSummary(is("can1"), is(fromDate), is(toDate)))
        .thenReturn(Future.successful(Left(NoAssociatedDataException)))

      running(app) {
        val result = route(app, getSummaryRequest).value
        status(result) mustBe NOT_FOUND
      }
    }
  }

  "CashTransactionControllerSpec.getDetail" should {
    "delegate to the service and return a list of cash daily statements with a 200 status code" in new Setup {

      import domain.{Declaration, TaxGroup}

      val expectedTaxGroups: Seq[TaxGroup] = Seq(
        TaxGroup("VAT", fourHundred,
          Seq(
            TaxTypeHolder(
              reasonForSecurity = "a",
              taxTypeID = "b",
              amount = tenThousand
            )
          )
        ),

        TaxGroup("Excise", sevenHundred,
          Seq(
            TaxTypeHolder(
              reasonForSecurity = "a",
              taxTypeID = "b",
              amount = tenThousand
            )
          )
        )
      )

      val aListOfCashDailyStatements: Seq[CashDailyStatement] =
        Seq(
          CashDailyStatement("date",
            "openingBalance",
            "closingBalance",
            Seq(Declaration("mrn",
              Some(EORI("importerEORI")),
              EORI("declarantEori"),
              Some("declarantReference"),
              "postingDate",
              "amount",
              expectedTaxGroups)),
            Seq(Transaction("12.34", "Payment", None), Transaction("12.34", "Withdrawal", Some("77665544"))))
        )

      val aListOfPendingTransactions: Seq[Declaration] =
        Seq(
          Declaration(
            "pendingDeclarationID",
            Some(EORI("pendingImporterEORI")),
            EORI("pendingDeclarantEORINumber"),
            Some("pendingDeclarantReference"),
            "pendingPostingDate",
            "pendingAmount",
            expectedTaxGroups
          )
        )

      val expectedCashTransactions: CashTransactions =
        CashTransactions(aListOfPendingTransactions, aListOfCashDailyStatements)

      when(mockCashTransactionsService.retrieveCashTransactionsDetail(is("can1"), is(fromDate), is(toDate)))
        .thenReturn(Future.successful(Right(expectedCashTransactions)))

      running(app) {
        val result = route(app, getDetailRequest).value
        status(result) mustBe OK
        contentAsJson(result) mustBe Json.toJson(expectedCashTransactions)
      }
    }

    "retrieve cash transactions call fails with no associated data error" in new Setup {
      when(mockCashTransactionsService.retrieveCashTransactionsDetail(is("can1"), is(fromDate), is(toDate)))
        .thenReturn(Future.successful(Left(NoAssociatedDataException)))

      running(app) {
        val result = route(app, getDetailRequest).value
        status(result) mustBe NOT_FOUND
      }
    }

    "retrieve cash transactions call fails with exceeded threshold error" in new Setup {
      when(mockCashTransactionsService.retrieveCashTransactionsDetail(is("can1"), is(fromDate), is(toDate)))
        .thenReturn(Future.successful(Left(ExceededThresholdErrorException)))

      running(app) {
        val result = route(app, getDetailRequest).value
        status(result) mustBe REQUEST_ENTITY_TOO_LARGE
      }
    }
  }

  "retrieveCashAccountTransactions" should {

    "return CashAccountTransactionSearchResponseDetail for the successful response" in new Setup {
      when(mockCashTransactionsService.retrieveCashAccountTransactions(any))
        .thenReturn(Future.successful(Right(cashAccountTransactionSearchResponseDetailOb)))

      running(app) {
        val result = route(app, retrieveCashAccountTransactions).value

        status(result) mustBe OK
        contentAsJson(result) mustBe Json.toJson(cashAccountTransactionSearchResponseDetailOb)
      }
    }

    "return error response for 400 error code" in new Setup {
      when(mockCashTransactionsService.retrieveCashAccountTransactions(any))
        .thenReturn(Future.successful(Left(errorDetails)))

      running(app) {
        val result = route(app, retrieveCashAccountTransactions).value

        status(result) mustBe BAD_REQUEST
        contentAsJson(result) mustBe Json.toJson(errorDetails)
      }
    }

    "return error response for 500 error code" in new Setup {
      when(mockCashTransactionsService.retrieveCashAccountTransactions(any))
        .thenReturn(
          Future.successful(
            Left(errorDetails.copy(errorCode = code500, errorMessage = "Error connecting to the server"))))

      running(app) {
        val result = route(app, retrieveCashAccountTransactions).value

        status(result) mustBe INTERNAL_SERVER_ERROR
        contentAsJson(result) mustBe
          Json.toJson(errorDetails.copy(errorCode = code500, errorMessage = "Error connecting to the server"))
      }
    }

    "return error response for ETMP error codes" in new Setup {
      when(mockCashTransactionsService.retrieveCashAccountTransactions(any))
        .thenReturn(
          Future.successful(
            Left(errorDetails.copy(
              errorCode = code001, errorMessage = "Invalid Cash Account"))))

      running(app) {
        val result = route(app, retrieveCashAccountTransactions).value

        status(result) mustBe PRECONDITION_FAILED
        contentAsJson(result) mustBe
          Json.toJson(errorDetails.copy(
            errorCode = code001, errorMessage = "Invalid Cash Account"))
      }
    }

    "return error response for 503 error code" in new Setup {
      when(mockCashTransactionsService.retrieveCashAccountTransactions(any))
        .thenReturn(
          Future.successful(
            Left(errorDetails.copy(
              errorCode = SERVICE_UNAVAILABLE.toString, errorMessage = "Error connecting to the server"))))

      running(app) {
        val result = route(app, retrieveCashAccountTransactions).value

        status(result) mustBe SERVICE_UNAVAILABLE
        contentAsJson(result) mustBe
          Json.toJson(errorDetails.copy(
            errorCode = SERVICE_UNAVAILABLE.toString, errorMessage = "Error connecting to the server"))
      }
    }
  }

  trait Setup {

    val sevenHundred: Double = -789.01
    val fourHundred: Double = -456.78
    val tenThousand = 10000.00

    implicit val ec: ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global

    val mockAuthConnector: CustomAuthConnector = mock[CustomAuthConnector]
    val mockCashTransactionsService: CashTransactionsService = mock[CashTransactionsService]

    val dateFromString = "2024-05-28"
    val dateToString = "2024-05-28"

    val can = "12345678901"
    val ownerEORI = "test_eori"
    val ownerEoriGB = "GB1234678900"
    val amount = 999.90

    val fromDate: LocalDate = LocalDate.of(YEAR_2020, MONTH_1, DAY_1)
    val toDate: LocalDate = LocalDate.of(YEAR_2020, MONTH_6, DAY_1)

    val dateFrom: LocalDate = LocalDate.now().minusDays(1)
    val dateTo: LocalDate = LocalDate.now()

    val eoriNumber = "GB123456789"
    val dateString = "2024-05-28"
    val eoriDataName = "test"
    val paymentReference = "CDSC1234567890"
    val bankAccount = "1234567890987"
    val sortCode = "123456789"

    val cashAccountPaymentDetailsOb: CashAccountPaymentDetails =
      CashAccountPaymentDetails(amount, Some(dateFromString), Some(dateToString))

    val cashTranSearchRequestDetailsWithSearchTypePOb: CashAccountTransactionSearchRequestDetails =
      CashAccountTransactionSearchRequestDetails(can, ownerEoriGB, P, None, Some(cashAccountPaymentDetailsOb))

    val errorDetails: ErrorDetail = ErrorDetail(
      timestamp = "2024-01-17T09:30:47Z",
      correlationId = "f058ebd6-02f7-4d3f-942e-904344e8cde5",
      errorCode = code400,
      errorMessage = "Request could not be processed",
      source = "Backend",
      sourceFaultDetail = SourceFaultDetail(Seq())
    )

    val getSummaryRequest: FakeRequest[JsValue] =
      FakeRequest(POST, controllers.routes.CashTransactionsController.getSummary().url)
        .withBody(Json.parse("""{"can":"can1", "from":"2020-01-01", "to":"2020-06-01"}"""))

    val getDetailRequest: FakeRequest[JsValue] =
      FakeRequest(POST, controllers.routes.CashTransactionsController.getDetail().url)
        .withBody(Json.parse("""{"can":"can1", "from":"2020-01-01", "to":"2020-06-01"}"""))

    val retrieveCashAccountTransactions: FakeRequest[JsValue] =
      FakeRequest(POST, controllers.routes.CashTransactionsController.retrieveCashAccountTransactions().url)
        .withBody(Json.toJson(cashTranSearchRequestDetailsWithSearchTypePOb))

    val app: Application = GuiceApplicationBuilder().overrides(
      inject.bind[CustomAuthConnector].toInstance(mockAuthConnector),
      inject.bind[CashTransactionsService].toInstance(mockCashTransactionsService)
    ).configure(
      "microservice.metrics.enabled" -> false,
      "metrics.enabled" -> false,
      "auditing.enabled" -> false
    ).build()

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

  }
}
