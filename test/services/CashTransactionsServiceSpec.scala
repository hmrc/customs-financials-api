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

package services

import connectors.{Acc31Connector, Acc45Connector}
import domain._
import models._
import models.requests.CashAccountStatementRequestDetail
import models.responses._
import play.api._
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json
import play.api.test.Helpers._
import uk.gov.hmrc.http.HeaderCarrier
import utils.SpecBase

import java.time.LocalDate
import scala.concurrent._

class CashTransactionsServiceSpec extends SpecBase {

  "retrieveCashTransactionsSummary" should {

    "return Left with an error if the api request failed" in new Setup {
      when(mockAcc31Connector.retrieveCashTransactions("can", dateFrom, dateTo)).thenReturn(
        Future.successful(Left(NoAssociatedDataException))
      )
      running(app) {
        val result = await(service.retrieveCashTransactionsSummary("can", dateFrom, dateTo))
        result mustBe Left(NoAssociatedDataException)
      }
    }

    "return Right with the Cash transactions on a successful response from the API" in new Setup {
      when(mockAcc31Connector.retrieveCashTransactions("can", dateFrom, dateTo))
        .thenReturn(Future.successful(Right(Some(cashTransactionsResponseDetail))))

      running(app) {
        val result = await(service.retrieveCashTransactionsSummary("can", dateFrom, dateTo))
        val expectedResult = CashTransactions(
          List(Declaration("someId", Some(EORI("someImporterEORI")),
            EORI("someEori"), Some("reference"), dateTo.toString, "10000", List.empty)),
          List(CashDailyStatement(
            dateFrom.toString,
            "10000",
            "9000",
            List(Declaration("someId", Some(EORI("someImporterEORI")),
              EORI("someEori"), Some("reference"), dateTo.toString, "10000", List.empty)),
            List(Transaction("10000", "A21", Some("Bank"))))
          )
        )
        result mustBe Right(expectedResult)
      }
    }

    "return Right with nil transactions on successful response with no responseDetail" in new Setup {
      when(mockAcc31Connector.retrieveCashTransactions("can", dateFrom, dateTo)).thenReturn(
        Future.successful(Right(None))
      )
      running(app) {
        val result = await(service.retrieveCashTransactionsSummary("can", dateFrom, dateTo))
        val expectedResult = CashTransactions(Nil, Nil)

        result mustBe Right(expectedResult)
      }
    }
  }

  "retrieveCashTransactionsDetail" should {

    "return Left with an error if the api request failed" in new Setup {
      when(mockAcc31Connector.retrieveCashTransactions("can", dateFrom, dateTo)).thenReturn(
        Future.successful(Left(NoAssociatedDataException))
      )
      running(app) {
        val result = await(service.retrieveCashTransactionsDetail("can", dateFrom, dateTo))
        result mustBe Left(NoAssociatedDataException)
      }
    }

    "return Right with the Cash transactions on a successful response from the API" in new Setup {
      when(mockAcc31Connector.retrieveCashTransactions("can", dateFrom, dateTo)).thenReturn(
        Future.successful(Right(Some(cashTransactionsResponseDetail)))
      )
      running(app) {
        val result = await(service.retrieveCashTransactionsDetail("can", dateFrom, dateTo))

        val expectedResult = CashTransactions(
          List(Declaration("someId", Some(EORI("someImporterEORI")), EORI("someEori"),
            Some("reference"), dateTo.toString, "10000", List.empty)),
          List(CashDailyStatement(
            dateFrom.toString,
            "10000",
            "9000",
            List(Declaration("someId",
              Some(EORI("someImporterEORI")),
              EORI("someEori"),
              Some("reference"),
              dateTo.toString,
              "10000",
              List(TaxGroup("something", "10000")))),
            List(Transaction("10000", "A21", Some("Bank"))))
          )
        )

        result mustBe Right(expectedResult)
      }
    }

    "return Right with nil transactions on successful response with no responseDetail" in new Setup {
      when(mockAcc31Connector.retrieveCashTransactions("can", dateFrom, dateTo)).thenReturn(
        Future.successful(Right(None))
      )
      running(app) {
        val result = await(service.retrieveCashTransactionsDetail("can", dateFrom, dateTo))
        val expectedResult = CashTransactions(Nil, Nil)
        result mustBe Right(expectedResult)
      }
    }
  }

  "submitCashAccountStatementRequest" should {

    "return Left with ErrorDetail if api request failed" in new Setup {

      val casErrorDetailStr01 =
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

      val casErrorResponseDetails01: ErrorDetail = Json.fromJson[ErrorDetail](
        Json.parse(casErrorDetailStr01)).get

      when(mockAcc45Connector.submitStatementRequest(cashAccSttReqDetail)).thenReturn(
        Future.successful(Left(casErrorResponseDetails01))
      )

      running(app) {
        val result = await(service.submitCashAccountStatementRequest(cashAccSttReqDetail))
        result mustBe Left(casErrorResponseDetails01)
      }
    }

    "return Right with ResponseCommon if api request successful" in new Setup {

      val casResponseCommonStr01 =
        """
          |{
          |  "status": "OK",
          |  "processingDate": "2021-12-17T09:30:47Z"
          |}""".stripMargin

      val casResponseCommon01: Acc45ResponseCommon = Json.fromJson[Acc45ResponseCommon](
        Json.parse(casResponseCommonStr01)).get

      when(mockAcc45Connector.submitStatementRequest(cashAccSttReqDetail)).thenReturn(
        Future.successful(Right(casResponseCommon01))
      )

      running(app) {
        val result = await(service.submitCashAccountStatementRequest(cashAccSttReqDetail))
        result mustBe Right(casResponseCommon01)
      }
    }

    "return Right with ResponseCommon if api fails at ETMP" in new Setup {

      val casResponseCommonStr01 =
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

      val casResponseCommon01: Acc45ResponseCommon = Json.fromJson[Acc45ResponseCommon](
        Json.parse(casResponseCommonStr01)).get

      when(mockAcc45Connector.submitStatementRequest(cashAccSttReqDetail)).thenReturn(
        Future.successful(Right(casResponseCommon01))
      )

      running(app) {
        val result = await(service.submitCashAccountStatementRequest(cashAccSttReqDetail))
        result mustBe Right(casResponseCommon01)
      }
    }

  }

  trait Setup {
    implicit val hc: HeaderCarrier = HeaderCarrier()
    implicit val executionContext: ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global

    val dateFrom: LocalDate = LocalDate.now().minusDays(1)
    val dateTo: LocalDate = LocalDate.now()
    val mockAcc31Connector: Acc31Connector = mock[Acc31Connector]
    val mockAcc45Connector: Acc45Connector = mock[Acc45Connector]

    val cashAccSttReqDetail = CashAccountStatementRequestDetail(
      "GB123456789012345", "12345678910", "2024-05-10", "2024-05-20")

    val dailyStatement: DailyStatementContainer = DailyStatementContainer(
      DailyStatementDetail(
        dateFrom.toString,
        "10000",
        "9000",
        Some(Seq(DeclarationContainer(
          DeclarationDetail(
            "someId",
            Some(EORI("someImporterEORI")),
            EORI("someEori"),
            Some("reference"),
            dateTo.toString,
            "10000",
            Seq(TaxGroupContainer(
              TaxGroupDetail("something", "10000")
            ))
          )
        ))),
        Some(Seq(PaymentAndWithdrawalContainer(
          PaymentAndWithdrawalDetail("10000", "A21", Some("Bank"))
        )))
      )
    )

    val pending: PendingTransactions = PendingTransactions(
      Seq(DeclarationContainer(
        DeclarationDetail(
          "someId",
          Some(EORI("someImporterEORI")),
          EORI("someEori"),
          Some("reference"),
          dateTo.toString,
          "10000",
          Seq(TaxGroupContainer(
            TaxGroupDetail("something", "10000")
          ))
        )
      ))
    )

    val cashTransactionsResponseDetail: CashTransactionsResponseDetail = CashTransactionsResponseDetail(
      Some(Seq(dailyStatement)),
      Some(pending))

    val app: Application = GuiceApplicationBuilder().overrides(
      inject.bind[Acc31Connector].toInstance(mockAcc31Connector),
      inject.bind[Acc45Connector].toInstance(mockAcc45Connector)
    ).configure(
      "microservice.metrics.enabled" -> false,
      "metrics.enabled" -> false,
      "auditing.enabled" -> false
    ).build()

    val service: CashTransactionsService = app.injector.instanceOf[CashTransactionsService]
  }
}
