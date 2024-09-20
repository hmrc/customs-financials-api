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

import connectors.{Acc31Connector, Acc44Connector, Acc45Connector}
import domain.{Declaration, TaxGroup, _}
import models._
import models.requests.{
  CashAccountPaymentDetails, CashAccountStatementRequestDetail,
  CashAccountTransactionSearchRequestDetails, SearchType
}
import models.responses.PaymentType.Payment
import models.responses._
import play.api._
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json
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
          List(Declaration(
            "someId",
            Some(EORI("someImporterEORI")),
            EORI("someEori"),
            Some("reference"),
            dateTo.toString,
            "10000",
            List(TaxGroup(
              "something",
              twoThousand,
              List(TaxTypeHolder(Some("a"), "b", thousand))
            ))
          )),

          List(CashDailyStatement(
            dateFrom.toString,
            "10000",
            "9000",
            List(Declaration(
              "someId",
              Some(EORI("someImporterEORI")),
              EORI("someEori"),
              Some("reference"),
              dateTo.toString,
              "10000",
              List(TaxGroup(
                "something",
                twoThousand,
                List(TaxTypeHolder(Some("a"), "b", thousand))
              ))
            )),

            List(Transaction("10000", "A21", Some("Bank")))
          )),
          Some(false)
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
        val expectedResult = CashTransactions(Nil, Nil, None)

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
          List(Declaration(
            movementReferenceNumber = "someId",
            importerEori = Some(EORI("someImporterEORI")),
            declarantEori = EORI("someEori"),
            declarantReference = Some("reference"),
            date = dateTo.toString,
            amount = "10000",
            taxGroups = List(TaxGroup(
              taxGroupDescription = "something",
              amount = twoThousand,
              taxTypes = Seq(TaxTypeHolder(
                reasonForSecurity = Some("a"),
                taxTypeID = "b",
                amount = thousand
              ))
            ))
          )),

          List(CashDailyStatement(
            date = dateFrom.toString,
            openingBalance = "10000",
            closingBalance = "9000",
            declarations = List(Declaration(
              movementReferenceNumber = "someId",
              importerEori = Some(EORI("someImporterEORI")),
              declarantEori = EORI("someEori"),
              declarantReference = Some("reference"),
              date = dateTo.toString,
              amount = "10000",
              taxGroups = List(TaxGroup(
                taxGroupDescription = "something",
                amount = twoThousand,
                taxTypes = Seq(TaxTypeHolder(
                  reasonForSecurity = Some("a"),
                  taxTypeID = "b",
                  amount = thousand
                ))
              ))
            )),
            otherTransactions = List(Transaction(
              amount = "10000",
              transactionType = "A21",
              bankAccountNumber = Some("Bank")
            ))
          )),
          Some(false)
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
        val expectedResult = CashTransactions(Nil, Nil, None)
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
  "submitCashAccountStatementRequest" should {

    "return Left with ErrorDetail if api request fails" in new Setup {

      val casErrorDetailStr01: String =
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

    "return Right with ResponseCommon if api request is successful" in new Setup {

      val casResponseCommonStr01: String =
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

      val casResponseCommonStr01: String =
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

    val twoThousand = "2000.00"
    val thousand = "1000.00"

    implicit val hc: HeaderCarrier = HeaderCarrier()
    implicit val executionContext: ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global

    val dateFrom: LocalDate = LocalDate.now().minusDays(1)
    val dateTo: LocalDate = LocalDate.now()
    val eoriNumber = "GB123456789"
    val processingDate = "2001-12-17T09:30:47Z"

    val mockAcc31Connector: Acc31Connector = mock[Acc31Connector]
    val mockAcc44Connector: Acc44Connector = mock[Acc44Connector]
    val mockAcc45Connector: Acc45Connector = mock[Acc45Connector]

    val cashAccSttReqDetail: CashAccountStatementRequestDetail = CashAccountStatementRequestDetail(
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
            Seq(
              TaxGroupContainer(
                TaxGroupDetail("something", twoThousand,
                  Seq(
                    TaxTypeContainer(
                      TaxTypeDetail(
                        reasonForSecurity = Some("a"), taxTypeID = "b", amount = thousand
                      )
                    )
                  )
                )
              )
            )
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
          Seq(
            TaxGroupContainer(
              TaxGroupDetail("something", twoThousand,
                Seq(
                  TaxTypeContainer(
                    TaxTypeDetail(
                      reasonForSecurity = Some("a"), taxTypeID = "b", amount = thousand
                    )
                  )
                )
              )
            )
          )
        )
      ))
    )

    val cashTransactionsResponseDetail: CashTransactionsResponseDetail = CashTransactionsResponseDetail(
      Some(Seq(dailyStatement)),
      Some(pending),
      Some(false))

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
      inject.bind[Acc44Connector].toInstance(mockAcc44Connector),
      inject.bind[Acc45Connector].toInstance(mockAcc45Connector)
    ).configure(
      "microservice.metrics.enabled" -> false,
      "metrics.enabled" -> false,
      "auditing.enabled" -> false
    ).build()

    val service: CashTransactionsService = app.injector.instanceOf[CashTransactionsService]
  }
}
