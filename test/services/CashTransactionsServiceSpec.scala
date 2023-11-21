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

import connectors.Acc31Connector
import domain._
import models._
import models.responses._
import play.api._
import play.api.inject.guice.GuiceApplicationBuilder
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
      when(mockAcc31Connector.retrieveCashTransactions("can", dateFrom, dateTo)).thenReturn(
        Future.successful(Right(Some(cashTransactionsResponseDetail)))
      )
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
            List(Declaration("someId", Some(EORI("someImporterEORI")), EORI("someEori"), Some("reference"), dateTo.toString, "10000", List(TaxGroup("something", "10000")))),
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

  trait Setup {
    implicit val hc: HeaderCarrier = HeaderCarrier()
    implicit val executionContext: ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global

    val dateFrom: LocalDate = LocalDate.now().minusDays(1)
    val dateTo: LocalDate = LocalDate.now()
    val mockAcc31Connector: Acc31Connector = mock[Acc31Connector]

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
      Some(pending)
    )

    val app: Application = GuiceApplicationBuilder().overrides(
      inject.bind[Acc31Connector].toInstance(mockAcc31Connector)
    ).configure(
      "microservice.metrics.enabled" -> false,
      "metrics.enabled" -> false,
      "auditing.enabled" -> false
    ).build()

    val service: CashTransactionsService = app.injector.instanceOf[CashTransactionsService]
  }

}