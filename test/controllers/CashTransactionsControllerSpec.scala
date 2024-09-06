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
import models.requests.CashAccountStatementRequestDetail
import models.responses.{Acc45ResponseCommon, ErrorDetail}
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
      val expectedTaxGroups: Seq[TaxGroup] = Seq(
        TaxGroup("VAT", "-456.78"),
        TaxGroup("Excise", "-789.01"))

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
            Nil))

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

  "CashTransactionControllerSpec.submitCashAccStatementRequest" should {

    "return ResponseCommon for success scenario" in new Setup {

      val acc45ResStr =
        """
          |{
          |  "status": "OK",
          |  "processingDate": "2021-12-17T09:30:47Z"
          |}""".stripMargin

      val response: Acc45ResponseCommon = Json.fromJson[Acc45ResponseCommon](Json.parse(acc45ResStr)).get

      when(mockCashTransactionsService.submitCashAccountStatementRequest(cashAccSttRequest))
        .thenReturn(Future.successful(Right(response)))

      running(app) {
        val result = route(app, submitCashAccStatementRequest).value

        status(result) mustBe OK
        contentAsJson(result) mustBe Json.toJson(response)
      }
    }

    "return ResponseCommon for business error at ETMP" in new Setup {

      val acc45ResStr =
        """
          |{
          |  "status": "OK",
          |  "statusText": "003-Request could not be processed",
          |  "processingDate": "2021-12-17T09:30:47Z",
          |  "returnParameters": [
          |    {
          |      "paramName": "POSITION",
          |      "paramValue": "FAIL"
          |    }
          |  ]
          |}""".stripMargin

      val response = Json.fromJson[Acc45ResponseCommon](Json.parse(acc45ResStr)).get

      when(mockCashTransactionsService.submitCashAccountStatementRequest(cashAccSttRequest))
        .thenReturn(Future.successful(Right(response)))

      running(app) {
        val result = route(app, submitCashAccStatementRequest).value

        status(result) mustBe CREATED
        contentAsJson(result) mustBe Json.toJson(response)
      }
    }

    "return ErrorDetails for invalid Json being sent to ETMP" in new Setup {

      val acc45ResStr =
        """
          |{
          |  "timestamp": "2024-01-21T11:30:47Z",
          |  "correlationId": "f058ebd6-02f7-4d3f-942e-904344e8cde5",
          |  "errorCode": "400",
          |  "errorMessage": "Request could not be processed",
          |  "source": "Backend",
          |  "sourceFaultDetail": {
          |    "detail": [
          |      "Invalid JSON message content used"
          |    ]
          |  }
          |}""".stripMargin

      val response = Json.fromJson[ErrorDetail](Json.parse(acc45ResStr)).get

      when(mockCashTransactionsService.submitCashAccountStatementRequest(cashAccSttRequest))
        .thenReturn(Future.successful(Left(response)))

      running(app) {
        val result = route(app, submitCashAccStatementRequest).value

        status(result) mustBe BAD_REQUEST
        contentAsJson(result) mustBe Json.toJson(response)
      }
    }

    "return ErrorDetails for business error in EIS" in new Setup {

      val acc45ResStr =
        """
          |{
          |  "timestamp": "2024-01-21T11:30:47Z",
          |  "correlationId": "f058ebd6-02f7-4d3f-942e-904344e8cde5",
          |  "errorCode": "500",
          |  "errorMessage": "Internal Server Error",
          |  "source": "Backend",
          |  "sourceFaultDetail": {
          |    "detail": [
          |      "Failure in backend System"
          |    ]
          |  }
          |}""".stripMargin

      val response = Json.fromJson[ErrorDetail](Json.parse(acc45ResStr)).get

      when(mockCashTransactionsService.submitCashAccountStatementRequest(cashAccSttRequest))
        .thenReturn(Future.successful(Left(response)))

      running(app) {
        val result = route(app, submitCashAccStatementRequest).value

        status(result) mustBe INTERNAL_SERVER_ERROR
        contentAsJson(result) mustBe Json.toJson(response)
      }
    }

  }

  trait Setup {

    implicit val ec: ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global

    val mockAuthConnector: CustomAuthConnector = mock[CustomAuthConnector]
    val mockCashTransactionsService: CashTransactionsService = mock[CashTransactionsService]

    val getSummaryRequest: FakeRequest[JsValue] =
      FakeRequest(POST, controllers.routes.CashTransactionsController.getSummary().url)
        .withBody(Json.parse("""{"can":"can1", "from":"2020-01-01", "to":"2020-06-01"}"""))

    val getDetailRequest: FakeRequest[JsValue] =
      FakeRequest(POST, controllers.routes.CashTransactionsController.getDetail().url)
        .withBody(Json.parse("""{"can":"can1", "from":"2020-01-01", "to":"2020-06-01"}"""))

    val cashAccSttRequest = CashAccountStatementRequestDetail("GB123456789012345", "12345678910", "2024-05-10", "2024-05-20")

    val submitCashAccStatementRequest: FakeRequest[JsValue] =
      FakeRequest(POST, controllers.routes.CashTransactionsController.submitCashAccStatementRequest().url)
        .withBody(Json.toJson(cashAccSttRequest))

    val app: Application = GuiceApplicationBuilder().overrides(
      inject.bind[CustomAuthConnector].toInstance(mockAuthConnector),
      inject.bind[CashTransactionsService].toInstance(mockCashTransactionsService)
    ).configure(
      "microservice.metrics.enabled" -> false,
      "metrics.enabled" -> false,
      "auditing.enabled" -> false
    ).build()

    val fromDate: LocalDate = LocalDate.of(YEAR_2020, MONTH_1, DAY_1)
    val toDate: LocalDate = LocalDate.of(YEAR_2020, MONTH_6, DAY_1)
  }
}
