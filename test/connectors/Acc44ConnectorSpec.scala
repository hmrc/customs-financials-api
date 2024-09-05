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

package connectors

import models.requests.{CashAccountPaymentDetails, CashAccountTransactionSearchRequestContainer, CashAccountTransactionSearchRequestDetails, SearchType}
import models.responses.ErrorCode.{code400, code500}
import models.responses.PaymentType.Payment
import models.responses._
import play.api.Application
import play.api.http.Status.{BAD_REQUEST, INTERNAL_SERVER_ERROR, OK}
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.{JsValue, Json}
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient, HttpResponse, UpstreamErrorResponse}
import utils.SpecBase

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class Acc44ConnectorSpec extends SpecBase {

  "cashAccountTransactionSearch" should {

    "return success response" when {

      "response has declarations" in new Setup {
        when[Future[CashAccountTransactionSearchResponseContainer]](mockHttpClient.POST(any, any, any)(any, any, any, any))
          .thenReturn(Future.successful(cashAccTranSearchResponseContainerWithDeclarationOb))

        connector.cashAccountTransactionSearch(cashAccTransactionSearchRequestDetails).map {
          successResponse => successResponse mustBe Right(cashAccTranSearchResponseContainerWithDeclarationOb)
        }
      }

      "response has paymentsWithdrawalsAndTransfers" in new Setup {
        when[Future[CashAccountTransactionSearchResponseContainer]](mockHttpClient.POST(any, any, any)(any, any, any, any))
          .thenReturn(Future.successful(cashAccTranSearchResponseContainerOb))

        connector.cashAccountTransactionSearch(cashAccTransactionSearchRequestDetails).map {
          successResponse => successResponse mustBe Right(cashAccTranSearchResponseContainerOb)
        }

      }
    }

    "return Error response" when {

      "Request fails to validate against schema" in new Setup {
        intercept[RuntimeException] {
          connector.cashAccountTransactionSearch(cashAccTransactionSearchRequestDetailsInvalid).map {
            failureRes => failureRes
          }
        }
      }

      "EIS returns 201 to MDTP" in new Setup {
        when[Future[CashAccountTransactionSearchResponseContainer]](mockHttpClient.POST(any, any, any)(any, any, any, any))
          .thenReturn(Future.successful(cashAccTranSearchResponseContainerWith201EISCodeOb))

        connector.cashAccountTransactionSearch(cashAccTransactionSearchRequestDetails).map {
          successResponse => successResponse mustBe Right(cashAccTranSearchResponseContainerWith201EISCodeOb)
        }
      }

      "api call produces Http status 500 due to backEnd error" in new Setup {
        val errorDetails: ErrorDetail =
          ErrorDetail(
            "2024-01-21T11:30:47Z",
            "f058ebd6-02f7-4d3f-942e-904344e8cde5",
            code500,
            "Internal Server Error",
            "Backend",
            SourceFaultDetail(Seq("Failure in backend System"))
          )

        val errorDetailJsString: String = """{
                                    |"errorDetail": {
                                    |"timestamp": "2024-01-21T11:30:47Z",
                                    |"correlationId": "f058ebd6-02f7-4d3f-942e-904344e8cde5",
                                    |"errorCode": "500",
                                    |"errorMessage": "Internal Server Error",
                                    |"source": "Backend",
                                    |"sourceFaultDetail": {
                                    |"detail": [
                                    |"Failure in backend System"
                                    |]
                                    |}
                                    |}
                                    |}""".stripMargin

        when[Future[CashAccountTransactionSearchRequestContainer]](mockHttpClient.POST(any, any, any)(any, any, any, any))
          .thenReturn(Future.failed(UpstreamErrorResponse(errorDetailJsString, INTERNAL_SERVER_ERROR)))

        connector.cashAccountTransactionSearch(cashAccTransactionSearchRequestDetails).map {
          failureRes =>
            failureRes mustBe Left(errorDetails)
        }
      }

      "request times out with Http status 500 due to EIS system error" in new Setup {
        val errorDetails: ErrorDetail =
          ErrorDetail(
            "2024-01-21T11:30:47Z",
            "f058ebd6-02f7-4d3f-942e-904344e8cde5",
            code500,
            "Internal Server Error",
            "Backend",
            SourceFaultDetail(Seq("Failure in backend System"))
          )

        val errorDetailJsString: JsValue = Json.toJson(errorDetails)

        when[Future[HttpResponse]](mockHttpClient.POST(any, any, any)(any, any, any, any))
          .thenReturn(Future.successful(HttpResponse(OK, errorDetailJsString, Map())))

        connector.cashAccountTransactionSearch(cashAccTransactionSearchRequestDetails).map {
          failureRes => failureRes mustBe Left(errorDetails)
        }
      }

      "request times out with Http status 400 due to EIS schema error" in new Setup {
        val errorDetails: ErrorDetail =
          ErrorDetail(
            "2024-01-21T11:30:47Z",
            "f058ebd6-02f7-4d3f-942e-904344e8cde5",
            code400,
            "Request could not be processed",
            "Backend",
            SourceFaultDetail(Seq("Failure in backend System"))
          )

        val errorDetailJsString: JsValue = Json.toJson(errorDetails)

        when[Future[HttpResponse]](mockHttpClient.POST(any, any, any)(any, any, any, any))
          .thenReturn(Future.successful(HttpResponse(OK, errorDetailJsString, Map())))

        connector.cashAccountTransactionSearch(cashAccTransactionSearchRequestDetails).map {
          failureRes => failureRes mustBe Left(errorDetails)
        }
      }

      "INTERNAL_SERVER_ERROR is returned from ETMP" in new Setup {
        val errorDetails: ErrorDetail =
          ErrorDetail(
            "2024-01-21T11:30:47Z",
            "f058ebd6-02f7-4d3f-942e-904344e8cde5",
            code500,
            "Request could not be processed",
            "ETMP",
            SourceFaultDetail(Seq("Failure while calling ETMP"))
          )

        when[Future[HttpResponse]](mockHttpClient.POST(any, any, any)(any, any, any, any))
          .thenReturn(Future.failed(UpstreamErrorResponse("ETMP Error", INTERNAL_SERVER_ERROR)))

        connector.cashAccountTransactionSearch(cashAccTransactionSearchRequestDetails).map {
          failureRes => failureRes mustBe Left(errorDetails)
        }
      }

      "4xx error is returned from ETMP" in new Setup {
        val errorDetails: ErrorDetail =
          ErrorDetail(
            "2024-01-21T11:30:47Z",
            "f058ebd6-02f7-4d3f-942e-904344e8cde5",
            code400,
            "Request could not be processed",
            "ETMP",
            SourceFaultDetail(Seq("Failure while calling ETMP"))
          )

        when[Future[HttpResponse]](mockHttpClient.POST(any, any, any)(any, any, any, any))
          .thenReturn(Future.failed(UpstreamErrorResponse("ETMP Error", BAD_REQUEST)))

        connector.cashAccountTransactionSearch(cashAccTransactionSearchRequestDetails).map {
          failureRes => failureRes mustBe Left(errorDetails)
        }
      }
    }
  }

  trait Setup {
    implicit val hc: HeaderCarrier = HeaderCarrier()

    val mockHttpClient: HttpClient = mock[HttpClient]

    val app: Application = GuiceApplicationBuilder().overrides(
      bind[HttpClient].toInstance(mockHttpClient)
    ).configure(
      "microservice.metrics.enabled" -> false,
      "metrics.enabled" -> false,
      "auditing.enabled" -> false
    ).build()

    val connector: Acc44Connector = app.injector.instanceOf[Acc44Connector]

    val dateString = "2024-05-28"
    val processingDate = "2001-12-17T09:30:47Z"

    val paymentReference = "CDSC1234567890"
    val amount = 9999.99
    val bankAccount = "1234567890987"
    val sortCode = "123456789"
    val can = "12345678909"
    val invalidCan = "123456789091234567"
    val eoriNumber = "GB123456789"
    val eoriDataName = "test"

    val declarationID = "24GB123456789"
    val declarantRef = "1234567890abcdefgh"
    val c18OrOverpaymentReference = "RPCSCCCS1"
    val importersEORINumber = "GB1234567"

    val cashAccTransactionSearchRequestDetails: CashAccountTransactionSearchRequestDetails =
      CashAccountTransactionSearchRequestDetails(
        can,
        eoriNumber,
        SearchType.P,
        declarationDetails = None,
        cashAccountPaymentDetails = Some(CashAccountPaymentDetails(amount, Some(dateString), Some(dateString))))

    val cashAccTransactionSearchRequestDetailsInvalid: CashAccountTransactionSearchRequestDetails =
      CashAccountTransactionSearchRequestDetails(
        invalidCan,
        eoriNumber,
        SearchType.P,
        declarationDetails = None,
        cashAccountPaymentDetails = Some(CashAccountPaymentDetails(amount, Some(dateString), Some(dateString))))

    val cashAccTranSearchResponseDetailWithPaymentWithdrawalOb: CashAccountTransactionSearchResponseDetail =
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

    val cashAccTranSearchResponseDetailWithDeclarationsOb: CashAccountTransactionSearchResponseDetail =
      CashAccountTransactionSearchResponseDetail(
        can,
        eoriDetails = Seq(EoriDataContainer(EoriData(eoriNumber, eoriDataName))),
        declarations = Some(Seq(DeclarationWrapper(Declaration(declarationID,
          eoriNumber,
          Some(declarantRef),
          Some(c18OrOverpaymentReference),
          importersEORINumber,
          dateString,
          dateString,
          amount,
          Seq(TaxGroupWrapper(
            TaxGroup(
              "Customs",
              amount,
              Seq(TaxTypeWithSecurityContainer(TaxTypeWithSecurity(Some("CRQ"), "A00", amount)))))))))
      ))

    val resCommonOb: CashTransactionsResponseCommon = CashTransactionsResponseCommon(
      status = "OK",
      statusText = None,
      processingDate = processingDate,
      maxTransactionsExceeded = None,
      returnParameters = None)

    val resCommonEIS201CodeOb: CashTransactionsResponseCommon = CashTransactionsResponseCommon(
      status = "OK",
      statusText = Some("001-Invalid Cash Account"),
      processingDate = processingDate,
      maxTransactionsExceeded = None,
      returnParameters = Some(Seq(ReturnParameter("POSITION", "FAIL")).toArray))

    val cashAccountTransactionSearchResponseOb: CashAccountTransactionSearchResponse =
      CashAccountTransactionSearchResponse(resCommonOb, Some(cashAccTranSearchResponseDetailWithPaymentWithdrawalOb))

    val cashAccountTransactionSearchResponseWithDeclarationOb: CashAccountTransactionSearchResponse =
      CashAccountTransactionSearchResponse(resCommonOb, Some(cashAccTranSearchResponseDetailWithDeclarationsOb))

    val cashAccountTransactionSearchResponseWith201EISCodeOb: CashAccountTransactionSearchResponse =
      CashAccountTransactionSearchResponse(resCommonEIS201CodeOb)

    val cashAccTranSearchResponseContainerOb: CashAccountTransactionSearchResponseContainer =
      CashAccountTransactionSearchResponseContainer(cashAccountTransactionSearchResponseOb)

    val cashAccTranSearchResponseContainerWithDeclarationOb: CashAccountTransactionSearchResponseContainer =
      CashAccountTransactionSearchResponseContainer(cashAccountTransactionSearchResponseWithDeclarationOb)

    val cashAccTranSearchResponseContainerWith201EISCodeOb: CashAccountTransactionSearchResponseContainer =
      CashAccountTransactionSearchResponseContainer(cashAccountTransactionSearchResponseOb)
  }
}
