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
import models.responses.*
import models.responses.ErrorCode.{code400, code500}
import models.responses.ErrorSource.mdtp
import models.responses.PaymentType.Payment
import models.responses.SourceFaultDetailMsg.REQUEST_SCHEMA_VALIDATION_ERROR
import play.api.{Application, Configuration}
import play.api.http.Status.*
import uk.gov.hmrc.http.HeaderCarrier
import utils.{SpecBase, WireMockSupportProvider}
import utils.TestData.*
import utils.Utils.emptyString
import com.typesafe.config.ConfigFactory
import play.api.libs.json.Json
import com.github.tomakehurst.wiremock.client.WireMock.{
  badRequest, created, equalTo, matchingJsonPath, ok, post, serverError, serviceUnavailable, urlPathMatching
}
import com.github.tomakehurst.wiremock.http.RequestMethod.POST
import config.MetaConfig.Platform.MDTP

import scala.concurrent.ExecutionContext.Implicits.global


class Acc44ConnectorSpec extends SpecBase with WireMockSupportProvider {

  "cashAccountTransactionSearch" should {

    "return success response" when {

      "response has declarations" in new Setup {
        wireMockServer.stubFor(
          post(urlPathMatching(acc44CashTransactionSearchEndpointUrl))
            .withHeader(X_FORWARDED_HOST, equalTo(MDTP))
            .withHeader(CONTENT_TYPE, equalTo(CONTENT_TYPE_APPLICATION_JSON))
            .withHeader(ACCEPT, equalTo(CONTENT_TYPE_APPLICATION_JSON))
            .withHeader(AUTHORIZATION, equalTo(AUTH_BEARER_TOKEN_VALUE))
            .withRequestBody(
              matchingJsonPath("$.cashAccountTransactionSearchRequest[?(@.requestCommon.originatingSystem == 'MDTP')]")
            )
            .withRequestBody(
              matchingJsonPath("$.cashAccountTransactionSearchRequest[?(@.requestDetail.can == '12345678909')]")
            )
            .withRequestBody(
              matchingJsonPath("$.cashAccountTransactionSearchRequest[?(@.requestDetail.ownerEORI == 'GB123456789')]")
            )
            .withRequestBody(
              matchingJsonPath("$.cashAccountTransactionSearchRequest[?(@.requestDetail.searchType == 'P')]")
            )
            .willReturn(
              ok(Json.toJson(cashAccTranSearchResponseContainerWithDeclarationOb).toString)
            )
        )

        val result: Either[ErrorDetail, CashAccountTransactionSearchResponseContainer] =
          await(connector.cashAccountTransactionSearch(cashAccTransactionSearchRequestDetails))

        result mustBe Right(cashAccTranSearchResponseContainerWithDeclarationOb)

        verifyExactlyOneEndPointUrlHit(acc44CashTransactionSearchEndpointUrl, POST)
      }

      "response has paymentsWithdrawalsAndTransfers" in new Setup {

        wireMockServer.stubFor(
          post(urlPathMatching(acc44CashTransactionSearchEndpointUrl))
            .withHeader(X_FORWARDED_HOST, equalTo(MDTP))
            .withHeader(CONTENT_TYPE, equalTo(CONTENT_TYPE_APPLICATION_JSON))
            .withHeader(ACCEPT, equalTo(CONTENT_TYPE_APPLICATION_JSON))
            .withHeader(AUTHORIZATION, equalTo(AUTH_BEARER_TOKEN_VALUE))
            .withRequestBody(
              matchingJsonPath("$.cashAccountTransactionSearchRequest[?(@.requestCommon.originatingSystem == 'MDTP')]")
            )
            .withRequestBody(
              matchingJsonPath("$.cashAccountTransactionSearchRequest[?(@.requestDetail.can == '12345678909')]")
            )
            .withRequestBody(
              matchingJsonPath("$.cashAccountTransactionSearchRequest[?(@.requestDetail.ownerEORI == 'GB123456789')]")
            )
            .withRequestBody(
              matchingJsonPath("$.cashAccountTransactionSearchRequest[?(@.requestDetail.searchType == 'P')]")
            )
            .willReturn(
              ok(Json.toJson(cashAccTranSearchResponseContainerOb).toString)
            )
        )

        val result: Either[ErrorDetail, CashAccountTransactionSearchResponseContainer] =
          await(connector.cashAccountTransactionSearch(cashAccTransactionSearchRequestDetails))

        result mustBe Right(cashAccTranSearchResponseContainerOb)

        verifyExactlyOneEndPointUrlHit(acc44CashTransactionSearchEndpointUrl, POST)
      }
    }

    "return Error response" when {

      "Request fails to validate against schema for the invalid CAN" in new Setup {
        val result: Either[ErrorDetail, CashAccountTransactionSearchResponseContainer] =
          await(connector.cashAccountTransactionSearch(cashAccTransactionSearchRequestDetailsInvalid))

        val canFieldSchemaPath = "/cashAccountTransactionSearchRequest/requestDetail/can"
        val lengthErrorMsg     = "length: 18, maximum allowed: 11"

        val expectedErrorMsg: String =
          s"""($canFieldSchemaPath: string "123456789091234567" is too long ($lengthErrorMsg))""".stripMargin

        val sourceFaultDetail: SourceFaultDetail = SourceFaultDetail(Seq(REQUEST_SCHEMA_VALIDATION_ERROR))
        val correlationId                        = "MDTP_ID"

        val defaultErrorDetails: ErrorDetail =
          ErrorDetail(emptyString, correlationId, BAD_REQUEST.toString, emptyString, mdtp, sourceFaultDetail)

        val actualErrorDetails: ErrorDetail = result.swap.getOrElse(defaultErrorDetails)

        actualErrorDetails.errorMessage mustBe expectedErrorMsg
        actualErrorDetails.correlationId mustBe correlationId
        actualErrorDetails.errorCode mustBe BAD_REQUEST.toString
        actualErrorDetails.source mustBe mdtp
        actualErrorDetails.sourceFaultDetail mustBe sourceFaultDetail
      }

      "EIS returns 201 to MDTP without responseDetails in success response" in new Setup {

        wireMockServer.stubFor(
          post(urlPathMatching(acc44CashTransactionSearchEndpointUrl))
            .withHeader(X_FORWARDED_HOST, equalTo(MDTP))
            .withHeader(CONTENT_TYPE, equalTo(CONTENT_TYPE_APPLICATION_JSON))
            .withHeader(ACCEPT, equalTo(CONTENT_TYPE_APPLICATION_JSON))
            .withHeader(AUTHORIZATION, equalTo(AUTH_BEARER_TOKEN_VALUE))
            .withRequestBody(
              matchingJsonPath("$.cashAccountTransactionSearchRequest[?(@.requestCommon.originatingSystem == 'MDTP')]")
            )
            .withRequestBody(
              matchingJsonPath("$.cashAccountTransactionSearchRequest[?(@.requestDetail.can == '12345678909')]")
            )
            .withRequestBody(
              matchingJsonPath("$.cashAccountTransactionSearchRequest[?(@.requestDetail.ownerEORI == 'GB123456789')]")
            )
            .withRequestBody(
              matchingJsonPath("$.cashAccountTransactionSearchRequest[?(@.requestDetail.searchType == 'P')]")
            )
            .willReturn(created.withBody(Json.toJson(cashAccTranSearchResponseContainerWith201EISCodeOb).toString))
        )

        val result: Either[ErrorDetail, CashAccountTransactionSearchResponseContainer] =
          await(connector.cashAccountTransactionSearch(cashAccTransactionSearchRequestDetails))

        result mustBe Right(cashAccTranSearchResponseContainerWith201EISCodeOb)

        verifyExactlyOneEndPointUrlHit(acc44CashTransactionSearchEndpointUrl, POST)
      }

      "EIS returns 201 to MDTP with errorDetails" in new Setup {

        wireMockServer.stubFor(
          post(urlPathMatching(acc44CashTransactionSearchEndpointUrl))
            .withHeader(X_FORWARDED_HOST, equalTo(MDTP))
            .withHeader(CONTENT_TYPE, equalTo(CONTENT_TYPE_APPLICATION_JSON))
            .withHeader(ACCEPT, equalTo(CONTENT_TYPE_APPLICATION_JSON))
            .withHeader(AUTHORIZATION, equalTo(AUTH_BEARER_TOKEN_VALUE))
            .withRequestBody(
              matchingJsonPath("$.cashAccountTransactionSearchRequest[?(@.requestCommon.originatingSystem == 'MDTP')]")
            )
            .withRequestBody(
              matchingJsonPath("$.cashAccountTransactionSearchRequest[?(@.requestDetail.can == '12345678909')]")
            )
            .withRequestBody(
              matchingJsonPath("$.cashAccountTransactionSearchRequest[?(@.requestDetail.ownerEORI == 'GB123456789')]")
            )
            .withRequestBody(
              matchingJsonPath("$.cashAccountTransactionSearchRequest[?(@.requestDetail.searchType == 'P')]")
            )
            .willReturn(created.withBody(Json.toJson(ErrorDetailContainer(errorDetails)).toString))
        )

        val result: Either[ErrorDetail, CashAccountTransactionSearchResponseContainer] =
          await(connector.cashAccountTransactionSearch(cashAccTransactionSearchRequestDetails))

        result mustBe Left(errorDetails)

        verifyExactlyOneEndPointUrlHit(acc44CashTransactionSearchEndpointUrl, POST)
      }

      "api call produces Http status 500 due to backEnd error" in new Setup {

        wireMockServer.stubFor(
          post(urlPathMatching(acc44CashTransactionSearchEndpointUrl))
            .withHeader(X_FORWARDED_HOST, equalTo(MDTP))
            .withHeader(CONTENT_TYPE, equalTo(CONTENT_TYPE_APPLICATION_JSON))
            .withHeader(ACCEPT, equalTo(CONTENT_TYPE_APPLICATION_JSON))
            .withHeader(AUTHORIZATION, equalTo(AUTH_BEARER_TOKEN_VALUE))
            .withRequestBody(
              matchingJsonPath("$.cashAccountTransactionSearchRequest[?(@.requestCommon.originatingSystem == 'MDTP')]")
            )
            .withRequestBody(
              matchingJsonPath("$.cashAccountTransactionSearchRequest[?(@.requestDetail.can == '12345678909')]")
            )
            .withRequestBody(
              matchingJsonPath("$.cashAccountTransactionSearchRequest[?(@.requestDetail.ownerEORI == 'GB123456789')]")
            )
            .withRequestBody(
              matchingJsonPath("$.cashAccountTransactionSearchRequest[?(@.requestDetail.searchType == 'P')]")
            )
            .willReturn(badRequest.withBody(Json.toJson(ErrorDetailContainer(errorDetails)).toString))
        )

        val result: Either[ErrorDetail, CashAccountTransactionSearchResponseContainer] =
          await(connector.cashAccountTransactionSearch(cashAccTransactionSearchRequestDetails))

        result mustBe Left(errorDetails)

        verifyExactlyOneEndPointUrlHit(acc44CashTransactionSearchEndpointUrl, POST)
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

        wireMockServer.stubFor(
          post(urlPathMatching(acc44CashTransactionSearchEndpointUrl))
            .withHeader(X_FORWARDED_HOST, equalTo(MDTP))
            .withHeader(CONTENT_TYPE, equalTo(CONTENT_TYPE_APPLICATION_JSON))
            .withHeader(ACCEPT, equalTo(CONTENT_TYPE_APPLICATION_JSON))
            .withHeader(AUTHORIZATION, equalTo(AUTH_BEARER_TOKEN_VALUE))
            .withRequestBody(
              matchingJsonPath("$.cashAccountTransactionSearchRequest[?(@.requestCommon.originatingSystem == 'MDTP')]")
            )
            .withRequestBody(
              matchingJsonPath("$.cashAccountTransactionSearchRequest[?(@.requestDetail.can == '12345678909')]")
            )
            .withRequestBody(
              matchingJsonPath("$.cashAccountTransactionSearchRequest[?(@.requestDetail.ownerEORI == 'GB123456789')]")
            )
            .withRequestBody(
              matchingJsonPath("$.cashAccountTransactionSearchRequest[?(@.requestDetail.searchType == 'P')]")
            )
            .willReturn(serverError.withBody(Json.toJson(ErrorDetailContainer(errorDetails)).toString))
        )

        val result: Either[ErrorDetail, CashAccountTransactionSearchResponseContainer] =
          await(connector.cashAccountTransactionSearch(cashAccTransactionSearchRequestDetails))

        result mustBe Left(errorDetails)

        verifyExactlyOneEndPointUrlHit(acc44CashTransactionSearchEndpointUrl, POST)
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

        wireMockServer.stubFor(
          post(urlPathMatching(acc44CashTransactionSearchEndpointUrl))
            .withHeader(X_FORWARDED_HOST, equalTo(MDTP))
            .withHeader(CONTENT_TYPE, equalTo(CONTENT_TYPE_APPLICATION_JSON))
            .withHeader(ACCEPT, equalTo(CONTENT_TYPE_APPLICATION_JSON))
            .withHeader(AUTHORIZATION, equalTo(AUTH_BEARER_TOKEN_VALUE))
            .withRequestBody(
              matchingJsonPath("$.cashAccountTransactionSearchRequest[?(@.requestCommon.originatingSystem == 'MDTP')]")
            )
            .withRequestBody(
              matchingJsonPath("$.cashAccountTransactionSearchRequest[?(@.requestDetail.can == '12345678909')]")
            )
            .withRequestBody(
              matchingJsonPath("$.cashAccountTransactionSearchRequest[?(@.requestDetail.ownerEORI == 'GB123456789')]")
            )
            .withRequestBody(
              matchingJsonPath("$.cashAccountTransactionSearchRequest[?(@.requestDetail.searchType == 'P')]")
            )
            .willReturn(badRequest.withBody(Json.toJson(ErrorDetailContainer(errorDetails)).toString))
        )

        val result: Either[ErrorDetail, CashAccountTransactionSearchResponseContainer] =
          await(connector.cashAccountTransactionSearch(cashAccTransactionSearchRequestDetails))

        result mustBe Left(errorDetails)

        verifyExactlyOneEndPointUrlHit(acc44CashTransactionSearchEndpointUrl, POST)
      }

      "INTERNAL_SERVER_ERROR is returned from ETMP" in new Setup {

        wireMockServer.stubFor(
          post(urlPathMatching(acc44CashTransactionSearchEndpointUrl))
            .withHeader(X_FORWARDED_HOST, equalTo(MDTP))
            .withHeader(CONTENT_TYPE, equalTo(CONTENT_TYPE_APPLICATION_JSON))
            .withHeader(ACCEPT, equalTo(CONTENT_TYPE_APPLICATION_JSON))
            .withHeader(AUTHORIZATION, equalTo(AUTH_BEARER_TOKEN_VALUE))
            .withRequestBody(
              matchingJsonPath("$.cashAccountTransactionSearchRequest[?(@.requestCommon.originatingSystem == 'MDTP')]")
            )
            .withRequestBody(
              matchingJsonPath("$.cashAccountTransactionSearchRequest[?(@.requestDetail.can == '12345678909')]")
            )
            .withRequestBody(
              matchingJsonPath("$.cashAccountTransactionSearchRequest[?(@.requestDetail.ownerEORI == 'GB123456789')]")
            )
            .withRequestBody(
              matchingJsonPath("$.cashAccountTransactionSearchRequest[?(@.requestDetail.searchType == 'P')]")
            )
            .willReturn(serverError.withBody(Json.toJson(ErrorDetailContainer(errorDetails)).toString))
        )

        val result: Either[ErrorDetail, CashAccountTransactionSearchResponseContainer] =
          await(connector.cashAccountTransactionSearch(cashAccTransactionSearchRequestDetails))

        result mustBe Left(errorDetails)

        verifyExactlyOneEndPointUrlHit(acc44CashTransactionSearchEndpointUrl, POST)
      }

      "4xx error is returned from ETMP" in new Setup {

        wireMockServer.stubFor(
          post(urlPathMatching(acc44CashTransactionSearchEndpointUrl))
            .withHeader(X_FORWARDED_HOST, equalTo(MDTP))
            .withHeader(CONTENT_TYPE, equalTo(CONTENT_TYPE_APPLICATION_JSON))
            .withHeader(ACCEPT, equalTo(CONTENT_TYPE_APPLICATION_JSON))
            .withHeader(AUTHORIZATION, equalTo(AUTH_BEARER_TOKEN_VALUE))
            .withRequestBody(
              matchingJsonPath("$.cashAccountTransactionSearchRequest[?(@.requestCommon.originatingSystem == 'MDTP')]")
            )
            .withRequestBody(
              matchingJsonPath("$.cashAccountTransactionSearchRequest[?(@.requestDetail.can == '12345678909')]")
            )
            .withRequestBody(
              matchingJsonPath("$.cashAccountTransactionSearchRequest[?(@.requestDetail.ownerEORI == 'GB123456789')]")
            )
            .withRequestBody(
              matchingJsonPath("$.cashAccountTransactionSearchRequest[?(@.requestDetail.searchType == 'P')]")
            )
            .willReturn(badRequest.withBody(Json.toJson(ErrorDetailContainer(errorDetails)).toString))
        )

        val result: Either[ErrorDetail, CashAccountTransactionSearchResponseContainer] =
          await(connector.cashAccountTransactionSearch(cashAccTransactionSearchRequestDetails))

        result mustBe Left(errorDetails)

        verifyExactlyOneEndPointUrlHit(acc44CashTransactionSearchEndpointUrl, POST)
      }

      "api call produces Http status code apart from 200, 400, 500 due to backEnd error with errorDetails" in new Setup {

        wireMockServer.stubFor(
          post(urlPathMatching(acc44CashTransactionSearchEndpointUrl))
            .withHeader(X_FORWARDED_HOST, equalTo(MDTP))
            .withHeader(CONTENT_TYPE, equalTo(CONTENT_TYPE_APPLICATION_JSON))
            .withHeader(ACCEPT, equalTo(CONTENT_TYPE_APPLICATION_JSON))
            .withHeader(AUTHORIZATION, equalTo(AUTH_BEARER_TOKEN_VALUE))
            .withRequestBody(
              matchingJsonPath("$.cashAccountTransactionSearchRequest[?(@.requestCommon.originatingSystem == 'MDTP')]")
            )
            .withRequestBody(
              matchingJsonPath("$.cashAccountTransactionSearchRequest[?(@.requestDetail.can == '12345678909')]")
            )
            .withRequestBody(
              matchingJsonPath("$.cashAccountTransactionSearchRequest[?(@.requestDetail.ownerEORI == 'GB123456789')]")
            )
            .withRequestBody(
              matchingJsonPath("$.cashAccountTransactionSearchRequest[?(@.requestDetail.searchType == 'P')]")
            )
            .willReturn(serviceUnavailable.withBody(Json.toJson(ErrorDetailContainer(errorDetails)).toString))
        )

        val result: Either[ErrorDetail, CashAccountTransactionSearchResponseContainer] =
          await(connector.cashAccountTransactionSearch(cashAccTransactionSearchRequestDetails))

        result mustBe Left(errorDetails)

        verifyExactlyOneEndPointUrlHit(acc44CashTransactionSearchEndpointUrl, POST)
      }

      "api call produces Http status code apart from 200, 400, 500 due to backEnd error with object other " +
        "than errorDetails" ignore new Setup {

          val cashAccTranSearchResponseContainerWithNoResponseDetailsOb: CashAccountTransactionSearchResponseContainer =
            cashAccTranSearchResponseContainerOb.copy(
              cashAccountTransactionSearchResponse =
                cashAccTranSearchResponseContainerOb.cashAccountTransactionSearchResponse.copy(responseDetail = None)
            )

          wireMockServer.stubFor(
            post(urlPathMatching(acc44CashTransactionSearchEndpointUrl))
              .withHeader(X_FORWARDED_HOST, equalTo(MDTP))
              .withHeader(CONTENT_TYPE, equalTo("application/json"))
              .withHeader(ACCEPT, equalTo("application/json"))
              .withHeader(AUTHORIZATION, equalTo(AUTH_BEARER_TOKEN_VALUE))
              .withRequestBody(
                matchingJsonPath(
                  "$.cashAccountTransactionSearchRequest[?(@.requestCommon.originatingSystem == 'MDTP')]"
                )
              )
              .withRequestBody(
                matchingJsonPath("$.cashAccountTransactionSearchRequest[?(@.requestDetail.can == '12345678909')]")
              )
              .withRequestBody(
                matchingJsonPath("$.cashAccountTransactionSearchRequest[?(@.requestDetail.ownerEORI == 'GB123456789')]")
              )
              .withRequestBody(
                matchingJsonPath("$.cashAccountTransactionSearchRequest[?(@.requestDetail.searchType == 'P')]")
              )
              .willReturn(
                serviceUnavailable.withBody(
                  Json.toJson(cashAccTranSearchResponseContainerWithNoResponseDetailsOb).toString
                )
              )
          )

          val result: Either[ErrorDetail, CashAccountTransactionSearchResponseContainer] =
            await(connector.cashAccountTransactionSearch(cashAccTransactionSearchRequestDetails))

          result mustBe Left(errorDetails)

          verifyExactlyOneEndPointUrlHit(acc44CashTransactionSearchEndpointUrl, POST)
        }
    }
  }

  override def config: Configuration = Configuration(
    ConfigFactory.parseString(
      s"""
         |microservice {
         |  services {
         |  acc44 {
         |            host = $wireMockHost
         |            port = $wireMockPort
         |        }
         |  }
         |}
         |""".stripMargin
    )
  )

  trait Setup {
    implicit val hc: HeaderCarrier = HeaderCarrier()

    val acc44CashTransactionSearchEndpointUrl = "/customs-financials-hods-stub/accounts/cashaccounttransactionsearch/v1"

    val app: Application          = application().configure(config).build()
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
        cashAccountPaymentDetails = Some(CashAccountPaymentDetails(AMOUNT, Some(DATE_STRING), Some(DATE_STRING)))
      )

    val cashAccTransactionSearchRequestDetailsInvalid: CashAccountTransactionSearchRequestDetails =
      CashAccountTransactionSearchRequestDetails(
        INVALID_CAN,
        EORI_NUMBER,
        SearchType.P,
        declarationDetails = None,
        cashAccountPaymentDetails = Some(CashAccountPaymentDetails(AMOUNT, Some(DATE_STRING), Some(DATE_STRING)))
      )

    val cashAccTranSearchResponseDetailWithPaymentWithdrawalOb: CashAccountTransactionSearchResponseDetail =
      CashAccountTransactionSearchResponseDetail(
        CAN,
        eoriDetails = Seq(EoriDataContainer(EoriData(EORI_NUMBER, EORI_DATA_NAME))),
        declarations = None,
        paymentsWithdrawalsAndTransfers = Some(
          Seq(
            PaymentsWithdrawalsAndTransferContainer(
              PaymentsWithdrawalsAndTransfer(
                DATE_STRING,
                DATE_STRING,
                PAYMENT_REFERENCE,
                AMOUNT,
                Payment,
                Some(BANK_ACCOUNT),
                Some(SORT_CODE)
              )
            )
          )
        )
      )

    val cashAccTranSearchResponseDetailWithDeclarationsOb: CashAccountTransactionSearchResponseDetail =
      CashAccountTransactionSearchResponseDetail(
        CAN,
        eoriDetails = Seq(EoriDataContainer(EoriData(EORI_NUMBER, EORI_DATA_NAME))),
        declarations = Some(
          Seq(
            DeclarationWrapper(
              Declaration(
                DECLARATION_ID,
                EORI_NUMBER,
                Some(DECLARANT_REF),
                Some(C18_OR_OVER_PAYMENT_REFERENCE),
                IMPORTERS_EORI_NUMBER,
                DATE_STRING,
                DATE_STRING,
                AMOUNT,
                Seq(
                  TaxGroupWrapper(
                    TaxGroup(
                      "Customs",
                      AMOUNT,
                      Seq(TaxTypeWithSecurityContainer(TaxTypeWithSecurity(Some("CRQ"), "A00", AMOUNT)))
                    )
                  )
                )
              )
            )
          )
        )
      )

    val resCommonOb: CashTransactionsResponseCommon = CashTransactionsResponseCommon(
      status = "OK",
      statusText = None,
      processingDate = PROCESSING_DATE,
      returnParameters = None
    )

    val resCommonEIS201CodeOb: CashTransactionsResponseCommon = CashTransactionsResponseCommon(
      status = "OK",
      statusText = Some("001-Invalid Cash Account"),
      processingDate = PROCESSING_DATE,
      returnParameters = Some(Seq(ReturnParameter("POSITION", "FAIL")).toArray)
    )

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
