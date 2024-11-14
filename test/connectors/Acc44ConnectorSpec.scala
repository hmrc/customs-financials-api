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

import models.requests.{CashAccountPaymentDetails, CashAccountTransactionSearchRequestDetails, SearchType}
import models.responses.ErrorCode.{code400, code500}
import models.responses.ErrorSource.mdtp
import models.responses.PaymentType.Payment
import models.responses.SourceFaultDetailMsg.REQUEST_SCHEMA_VALIDATION_ERROR
import models.responses._
import play.api.Application
import play.api.http.Status._
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.{JsValue, Json}
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient, HttpResponse}
import utils.SpecBase
import utils.TestData._
import utils.Utils.emptyString

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class Acc44ConnectorSpec extends SpecBase {

  "cashAccountTransactionSearch" should {

    "return success response" when {

      "response has declarations" in new Setup {
        when[Future[CashAccountTransactionSearchResponseContainer]](
          mockHttpClient.POST(any, any, any)(any, any, any, any))
          .thenReturn(Future.successful(cashAccTranSearchResponseContainerWithDeclarationOb))

        connector.cashAccountTransactionSearch(cashAccTransactionSearchRequestDetails).map {
          successResponse => successResponse mustBe Right(cashAccTranSearchResponseContainerWithDeclarationOb)
        }
      }

      "response has paymentsWithdrawalsAndTransfers" in new Setup {
        when[Future[CashAccountTransactionSearchResponseContainer]](
          mockHttpClient.POST(any, any, any)(any, any, any, any))
          .thenReturn(Future.successful(cashAccTranSearchResponseContainerOb))

        connector.cashAccountTransactionSearch(cashAccTransactionSearchRequestDetails).map {
          successResponse => successResponse mustBe Right(cashAccTranSearchResponseContainerOb)
        }

      }
    }

    "return Error response" when {

      "Request fails to validate against schema for the invalid CAN" in new Setup {
        val result: Either[ErrorDetail, CashAccountTransactionSearchResponseContainer] =
          await(connector.cashAccountTransactionSearch(cashAccTransactionSearchRequestDetailsInvalid))

        val canFieldSchemaPath = "/cashAccountTransactionSearchRequest/requestDetail/can"
        val lengthErrorMsg = "length: 18, maximum allowed: 11"

        val expectedErrorMsg: String =
          s"""($canFieldSchemaPath: string "123456789091234567" is too long ($lengthErrorMsg))""".stripMargin

        val sourceFaultDetail: SourceFaultDetail = SourceFaultDetail(Seq(REQUEST_SCHEMA_VALIDATION_ERROR))
        val correlationId = "MDTP_ID"

        val defaultErrorDetails: ErrorDetail = ErrorDetail(
          emptyString,
          correlationId,
          BAD_REQUEST.toString,
          emptyString,
          mdtp,
          sourceFaultDetail)

        val actualErrorDetails: ErrorDetail = result.swap.getOrElse(defaultErrorDetails)

        actualErrorDetails.errorMessage mustBe expectedErrorMsg
        actualErrorDetails.correlationId mustBe correlationId
        actualErrorDetails.errorCode mustBe BAD_REQUEST.toString
        actualErrorDetails.source mustBe mdtp
        actualErrorDetails.sourceFaultDetail mustBe sourceFaultDetail
      }

      "EIS returns 201 to MDTP without responseDetails in success response" in new Setup {
        when[Future[HttpResponse]](mockHttpClient.POST(any, any, any)(any, any, any, any))
          .thenReturn(
            Future.successful(
              HttpResponse(CREATED, Json.toJson(cashAccTranSearchResponseContainerWith201EISCodeOb), Map())))

        connector.cashAccountTransactionSearch(cashAccTransactionSearchRequestDetails).map {
          successResponse => successResponse mustBe Right(cashAccTranSearchResponseContainerWith201EISCodeOb)
        }
      }

      "EIS returns 201 to MDTP with errorDetails" in new Setup {
        when[Future[HttpResponse]](mockHttpClient.POST(any, any, any)(any, any, any, any))
          .thenReturn(
            Future.successful(
              HttpResponse(CREATED, Json.toJson(ErrorDetailContainer(errorDetails)), Map())))

        connector.cashAccountTransactionSearch(cashAccTransactionSearchRequestDetails).map {
          successResponse => successResponse mustBe Left(errorDetails)
        }
      }

      "api call produces Http status 500 due to backEnd error" in new Setup {

        when[Future[HttpResponse]](mockHttpClient.POST(any, any, any)(any, any, any, any))
          .thenReturn(
            Future.successful(HttpResponse(BAD_REQUEST, Json.toJson(ErrorDetailContainer(errorDetails)), Map())))

        connector.cashAccountTransactionSearch(cashAccTransactionSearchRequestDetails).map {
          failureRes =>
            failureRes mustBe Left(errorDetails)
        }
      }

      "request times out with Http status 500 due to EIS system error" in new Setup {
        override val errorDetails: ErrorDetail =
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
        override val errorDetails: ErrorDetail =
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
        when[Future[HttpResponse]](mockHttpClient.POST(any, any, any)(any, any, any, any))
          .thenReturn(
            Future.successful(
              HttpResponse(INTERNAL_SERVER_ERROR, Json.toJson(ErrorDetailContainer(errorDetails)), Map())))

        connector.cashAccountTransactionSearch(cashAccTransactionSearchRequestDetails).map {
          failureRes => failureRes mustBe Left(errorDetails)
        }
      }

      "4xx error is returned from ETMP" in new Setup {
        when[Future[HttpResponse]](mockHttpClient.POST(any, any, any)(any, any, any, any))
          .thenReturn(
            Future.successful(HttpResponse(BAD_REQUEST, Json.toJson(ErrorDetailContainer(errorDetails)), Map())))

        connector.cashAccountTransactionSearch(cashAccTransactionSearchRequestDetails).map {
          failureRes => failureRes mustBe Left(errorDetails)
        }
      }

      "api call produces Http status code apart from 200, 400, 500 due to backEnd error with errorDetails" in new Setup {

        when[Future[HttpResponse]](mockHttpClient.POST(any, any, any)(any, any, any, any))
          .thenReturn(
            Future.successful(
              HttpResponse(SERVICE_UNAVAILABLE, Json.toJson(ErrorDetailContainer(errorDetails)), Map())))

        connector.cashAccountTransactionSearch(cashAccTransactionSearchRequestDetails).map {
          failureRes =>
            failureRes mustBe Left(errorDetails)
        }
      }

      "api call produces Http status code apart from 200, 400, 500 due to backEnd error with object other " +
        "than errorDetails" in new Setup {

        val cashAccTranSearchResponseContainerWithNoResponseDetailsOb: CashAccountTransactionSearchResponseContainer =
          cashAccTranSearchResponseContainerOb.copy(
            cashAccountTransactionSearchResponse =
              cashAccTranSearchResponseContainerOb
                .cashAccountTransactionSearchResponse.copy(responseDetail = None))

        when[Future[HttpResponse]](mockHttpClient.POST(any, any, any)(any, any, any, any))
          .thenReturn(
            Future.successful(
              HttpResponse(
                SERVICE_UNAVAILABLE,
                Json.toJson(cashAccTranSearchResponseContainerWithNoResponseDetailsOb), Map())))

        connector.cashAccountTransactionSearch(cashAccTransactionSearchRequestDetails).map {
          failureRes =>
            failureRes mustBe Left(errorDetails)
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

    val errorDetails: ErrorDetail =
      ErrorDetail(
        "2024-01-21T11:30:47Z",
        "f058ebd6-02f7-4d3f-942e-904344e8cde5",
        code500,
        "Internal Server Error",
        "Backend",
        SourceFaultDetail(Seq("Failure in backend System"))
      )

    val cashAccTransactionSearchRequestDetails: CashAccountTransactionSearchRequestDetails =
      CashAccountTransactionSearchRequestDetails(
        CAN,
        EORI_NUMBER,
        SearchType.P,
        declarationDetails = None,
        cashAccountPaymentDetails = Some(CashAccountPaymentDetails(AMOUNT, Some(DATE_STRING), Some(DATE_STRING))))

    val cashAccTransactionSearchRequestDetailsInvalid: CashAccountTransactionSearchRequestDetails =
      CashAccountTransactionSearchRequestDetails(
        INVALID_CAN,
        EORI_NUMBER,
        SearchType.P,
        declarationDetails = None,
        cashAccountPaymentDetails = Some(CashAccountPaymentDetails(AMOUNT, Some(DATE_STRING), Some(DATE_STRING))))

    val cashAccTranSearchResponseDetailWithPaymentWithdrawalOb: CashAccountTransactionSearchResponseDetail =
      CashAccountTransactionSearchResponseDetail(
        CAN,
        eoriDetails = Seq(EoriDataContainer(EoriData(EORI_NUMBER, EORI_DATA_NAME))),
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

    val cashAccTranSearchResponseDetailWithDeclarationsOb: CashAccountTransactionSearchResponseDetail =
      CashAccountTransactionSearchResponseDetail(
        CAN,
        eoriDetails = Seq(EoriDataContainer(EoriData(EORI_NUMBER, EORI_DATA_NAME))),
        declarations = Some(Seq(DeclarationWrapper(Declaration(DECLARATION_ID,
          EORI_NUMBER,
          Some(DECLARANT_REF),
          Some(C18_OR_OVER_PAYMENT_REFERENCE),
          IMPORTERS_EORI_NUMBER,
          DATE_STRING,
          DATE_STRING,
          AMOUNT,
          Seq(TaxGroupWrapper(
            TaxGroup(
              "Customs",
              AMOUNT,
              Seq(TaxTypeWithSecurityContainer(TaxTypeWithSecurity(Some("CRQ"), "A00", AMOUNT)))))))))
        ))

    val resCommonOb: CashTransactionsResponseCommon = CashTransactionsResponseCommon(
      status = "OK",
      statusText = None,
      processingDate = PROCESSING_DATE,
      returnParameters = None)

    val resCommonEIS201CodeOb: CashTransactionsResponseCommon = CashTransactionsResponseCommon(
      status = "OK",
      statusText = Some("001-Invalid Cash Account"),
      processingDate = PROCESSING_DATE,
      returnParameters = Some(Seq(ReturnParameter("POSITION", "FAIL")).toArray))

    val cashAccountTransactionSearchResponseOb: CashAccountTransactionSearchResponse =
      CashAccountTransactionSearchResponse(resCommonOb, Some(cashAccTranSearchResponseDetailWithPaymentWithdrawalOb))

    val cashAccountTransactionSearchResponseEIS201Ob: CashAccountTransactionSearchResponse =
      CashAccountTransactionSearchResponse(resCommonOb)

    val cashAccountTransactionSearchResponseWithDeclarationOb: CashAccountTransactionSearchResponse =
      CashAccountTransactionSearchResponse(resCommonOb, Some(cashAccTranSearchResponseDetailWithDeclarationsOb))

    val cashAccountTransactionSearchResponseWith201EISCodeOb: CashAccountTransactionSearchResponse =
      CashAccountTransactionSearchResponse(resCommonEIS201CodeOb)

    val cashAccTranSearchResponseContainerOb: CashAccountTransactionSearchResponseContainer =
      CashAccountTransactionSearchResponseContainer(cashAccountTransactionSearchResponseOb)

    val cashAccTranSearchResponseContainerWithDeclarationOb: CashAccountTransactionSearchResponseContainer =
      CashAccountTransactionSearchResponseContainer(cashAccountTransactionSearchResponseWithDeclarationOb)

    val cashAccTranSearchResponseContainerWith201EISCodeOb: CashAccountTransactionSearchResponseContainer =
      CashAccountTransactionSearchResponseContainer(cashAccountTransactionSearchResponseEIS201Ob)
  }
}
