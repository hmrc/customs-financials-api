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

import connectors.{Acc31Connector, Acc44Connector}
import domain.{Declaration, TaxGroup, _}
import models._
import models.requests.{CashAccountPaymentDetails, CashAccountTransactionSearchRequestDetails, SearchType}
import models.responses.PaymentType.Payment
import models.responses._
import play.api._
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.Helpers._
import uk.gov.hmrc.http.HeaderCarrier
import utils.SpecBase
import utils.TestData.{AMOUNT, BANK_ACCOUNT, CAN, DATE_STRING, EORI_DATA_NAME, PAYMENT_REFERENCE, SORT_CODE}

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

  "retrieveCashAccountTransactions" should {

    "return successful response" in new Setup {
      when(mockAcc44Connector.cashAccountTransactionSearch(cashAccTransactionSearchRequestDetails))
        .thenReturn(Future.successful(Right(cashAccTranSearchResponseContainerOb)))

      running(app) {
        val result = service.retrieveCashAccountTransactions(cashAccTransactionSearchRequestDetails)

        result.map {
          response =>
            response mustBe
              Right(cashAccTranSearchResponseContainerOb.cashAccountTransactionSearchResponse.responseDetail.get)
        }
      }
    }

    "return Left[ErrorDetails] when business error occurs and response details is not present" in new Setup {
      when(mockAcc44Connector.cashAccountTransactionSearch(cashAccTransactionSearchRequestDetails))
        .thenReturn(
          Future.successful(
            Right(cashAccTranSearchResponseContainerOb.copy(
              cashAccountTransactionSearchResponse =
                cashAccountTransactionSearchResponseOb.copy(
                  responseDetail = None,
                  responseCommon = resCommonOb.copy(statusText = Some("001-Invalid Cash Account"))))
            )))

      running(app) {
        val result = service.retrieveCashAccountTransactions(cashAccTransactionSearchRequestDetails)

        result.map {
          response =>
            response mustBe
              Left(ErrorDetail(
                timestamp = cashAccountTransactionSearchResponseOb.responseCommon.processingDate,
                correlationId = "NA",
                errorCode = "001",
                errorMessage = "Invalid Cash Account",
                source = "Backend",
                sourceFaultDetail = SourceFaultDetail(Seq())
              ))
        }
      }
    }
  }

  trait Setup {
    implicit val hc: HeaderCarrier = HeaderCarrier()
    implicit val executionContext: ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global

    val dateFrom: LocalDate = LocalDate.now().minusDays(1)
    val dateTo: LocalDate = LocalDate.now()
    val eoriNumber = "GB123456789"
    val processingDate = "2001-12-17T09:30:47Z"

    val mockAcc31Connector: Acc31Connector = mock[Acc31Connector]
    val mockAcc44Connector: Acc44Connector = mock[Acc44Connector]

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

    val cashAccTransactionSearchRequestDetails: CashAccountTransactionSearchRequestDetails =
      CashAccountTransactionSearchRequestDetails(
        CAN,
        eoriNumber,
        SearchType.P,
        declarationDetails = None,
        cashAccountPaymentDetails = Some(CashAccountPaymentDetails(AMOUNT, Some(DATE_STRING), Some(DATE_STRING))))

    val resCommonOb: CashTransactionsResponseCommon = CashTransactionsResponseCommon(
      status = "OK",
      statusText = None,
      processingDate = processingDate,
      maxTransactionsExceeded = None,
      returnParameters = None)

    val cashAccTranSearchResponseDetailWithPaymentWithdrawalOb: CashAccountTransactionSearchResponseDetail =
      CashAccountTransactionSearchResponseDetail(
        CAN,
        eoriDetails = Seq(EoriDataContainer(EoriData(eoriNumber, EORI_DATA_NAME))),
        declarations = None,
        paymentsWithdrawalsAndTransfers =
          Some(
            Seq(
              PaymentsWithdrawalsAndTransferContainer(PaymentsWithdrawalsAndTransfer(
                DATE_STRING,
                DATE_STRING,
                PAYMENT_REFERENCE,
                AMOUNT,
                Payment,
                Some(BANK_ACCOUNT),
                Some(SORT_CODE)
              ))
            ))
      )

    val cashAccountTransactionSearchResponseOb: CashAccountTransactionSearchResponse =
      CashAccountTransactionSearchResponse(resCommonOb, Some(cashAccTranSearchResponseDetailWithPaymentWithdrawalOb))

    val cashAccTranSearchResponseContainerOb: CashAccountTransactionSearchResponseContainer =
      CashAccountTransactionSearchResponseContainer(cashAccountTransactionSearchResponseOb)

    val app: Application = GuiceApplicationBuilder().overrides(
      inject.bind[Acc31Connector].toInstance(mockAcc31Connector),
      inject.bind[Acc44Connector].toInstance(mockAcc44Connector)
    ).configure(
      "microservice.metrics.enabled" -> false,
      "metrics.enabled" -> false,
      "auditing.enabled" -> false
    ).build()

    val service: CashTransactionsService = app.injector.instanceOf[CashTransactionsService]
  }
}
