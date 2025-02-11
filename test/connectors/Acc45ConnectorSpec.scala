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

import models.requests.{
  CashAccountStatementRequest, CashAccountStatementRequestCommon, CashAccountStatementRequestContainer,
  CashAccountStatementRequestDetail
}
import models.responses.*
import play.api.{Application, Configuration}
import play.api.test.Helpers.*
import uk.gov.hmrc.http.HeaderCarrier
import utils.{SpecBase, WireMockSupportProvider}
import com.typesafe.config.ConfigFactory
import play.api.libs.json.Json
import com.github.tomakehurst.wiremock.client.WireMock.{
  aResponse, badRequest, created, equalTo, matchingJsonPath, ok, post, serverError, serviceUnavailable, urlPathMatching
}
import com.github.tomakehurst.wiremock.http.Fault
import com.github.tomakehurst.wiremock.http.RequestMethod.POST
import config.MetaConfig.Platform.MDTP

class Acc45ConnectorSpec extends SpecBase with WireMockSupportProvider {

  "submitCashAccStatementRequest" should {

    "return success response" when {

      "request is successfully processed" in new Setup {

        wireMockServer.stubFor(
          post(urlPathMatching(acc45CashAccountStatementRequestEndpointUrl))
            .withHeader(X_FORWARDED_HOST, equalTo(MDTP))
            .withHeader(CONTENT_TYPE, equalTo(CONTENT_TYPE_APPLICATION_JSON))
            .withHeader(ACCEPT, equalTo(CONTENT_TYPE_APPLICATION_JSON))
            .withHeader(AUTHORIZATION, equalTo(AUTH_BEARER_TOKEN_VALUE))
            .withRequestBody(
              matchingJsonPath("$.cashAccountStatementRequest[?(@.requestCommon.originatingSystem == 'MDTP')]")
            )
            .withRequestBody(
              matchingJsonPath("$.cashAccountStatementRequest[?(@.requestDetail.can == '12345678910')]")
            )
            .withRequestBody(
              matchingJsonPath("$.cashAccountStatementRequest[?(@.requestDetail.eori == 'GB123456789012345')]")
            )
            .willReturn(ok(casResponseStr01))
        )

        val result: Either[ErrorDetail, Acc45ResponseCommon] = await(connector.submitStatementRequest(reqDetail01))
        result mustBe Right(responseCommon01)

        verifyExactlyOneEndPointUrlHit(acc45CashAccountStatementRequestEndpointUrl, POST)
      }

      "request is not successful due to business error - 'Request could not be processed'" in new Setup {

        wireMockServer.stubFor(
          post(urlPathMatching(acc45CashAccountStatementRequestEndpointUrl))
            .withHeader(X_FORWARDED_HOST, equalTo(MDTP))
            .withHeader(CONTENT_TYPE, equalTo(CONTENT_TYPE_APPLICATION_JSON))
            .withHeader(ACCEPT, equalTo(CONTENT_TYPE_APPLICATION_JSON))
            .withHeader(AUTHORIZATION, equalTo(AUTH_BEARER_TOKEN_VALUE))
            .withRequestBody(
              matchingJsonPath("$.cashAccountStatementRequest[?(@.requestCommon.originatingSystem == 'MDTP')]")
            )
            .withRequestBody(
              matchingJsonPath("$.cashAccountStatementRequest[?(@.requestDetail.can == '12345678910')]")
            )
            .withRequestBody(
              matchingJsonPath("$.cashAccountStatementRequest[?(@.requestDetail.eori == 'GB123456789012345')]")
            )
            .willReturn(created.withBody(casResponseStr02))
        )

        val result: Either[ErrorDetail, Acc45ResponseCommon] = await(connector.submitStatementRequest(reqDetail01))
        result mustBe Right(responseCommon02)

        verifyExactlyOneEndPointUrlHit(acc45CashAccountStatementRequestEndpointUrl, POST)
      }

      "request is not successful due to business error - 'Exceeded maximum threshold of transactions'" in new Setup {

        wireMockServer.stubFor(
          post(urlPathMatching(acc45CashAccountStatementRequestEndpointUrl))
            .withHeader(X_FORWARDED_HOST, equalTo(MDTP))
            .withHeader(CONTENT_TYPE, equalTo(CONTENT_TYPE_APPLICATION_JSON))
            .withHeader(ACCEPT, equalTo(CONTENT_TYPE_APPLICATION_JSON))
            .withHeader(AUTHORIZATION, equalTo(AUTH_BEARER_TOKEN_VALUE))
            .withRequestBody(
              matchingJsonPath("$.cashAccountStatementRequest[?(@.requestCommon.originatingSystem == 'MDTP')]")
            )
            .withRequestBody(
              matchingJsonPath("$.cashAccountStatementRequest[?(@.requestDetail.can == '12345678910')]")
            )
            .withRequestBody(
              matchingJsonPath("$.cashAccountStatementRequest[?(@.requestDetail.eori == 'GB123456789012345')]")
            )
            .willReturn(created.withBody(casResponseStr03))
        )

        val result: Either[ErrorDetail, Acc45ResponseCommon] = await(connector.submitStatementRequest(reqDetail01))
        result mustBe Right(responseCommon03)

        verifyExactlyOneEndPointUrlHit(acc45CashAccountStatementRequestEndpointUrl, POST)
      }
    }

    "return ErrorDetail response" when {

      "requests could not be processed at EIS" in new Setup {
        wireMockServer.stubFor(
          post(urlPathMatching(acc45CashAccountStatementRequestEndpointUrl))
            .withHeader(X_FORWARDED_HOST, equalTo(MDTP))
            .withHeader(CONTENT_TYPE, equalTo(CONTENT_TYPE_APPLICATION_JSON))
            .withHeader(ACCEPT, equalTo(CONTENT_TYPE_APPLICATION_JSON))
            .withHeader(AUTHORIZATION, equalTo(AUTH_BEARER_TOKEN_VALUE))
            .withRequestBody(
              matchingJsonPath("$.cashAccountStatementRequest[?(@.requestCommon.originatingSystem == 'MDTP')]")
            )
            .withRequestBody(
              matchingJsonPath("$.cashAccountStatementRequest[?(@.requestDetail.can == '12345678910')]")
            )
            .withRequestBody(
              matchingJsonPath("$.cashAccountStatementRequest[?(@.requestDetail.eori == 'GB123456789012345')]")
            )
            .willReturn(badRequest.withBody(casErrorResponseStr01))
        )

        val result: Either[ErrorDetail, Acc45ResponseCommon] = await(connector.submitStatementRequest(reqDetail01))
        result mustBe Left(errorResponseDetails01)

        verifyExactlyOneEndPointUrlHit(acc45CashAccountStatementRequestEndpointUrl, POST)
      }

      "requests has missing required properties" in new Setup {

        wireMockServer.stubFor(
          post(urlPathMatching(acc45CashAccountStatementRequestEndpointUrl))
            .withHeader(X_FORWARDED_HOST, equalTo(MDTP))
            .withHeader(CONTENT_TYPE, equalTo(CONTENT_TYPE_APPLICATION_JSON))
            .withHeader(ACCEPT, equalTo(CONTENT_TYPE_APPLICATION_JSON))
            .withHeader(AUTHORIZATION, equalTo(AUTH_BEARER_TOKEN_VALUE))
            .withRequestBody(
              matchingJsonPath("$.cashAccountStatementRequest[?(@.requestCommon.originatingSystem == 'MDTP')]")
            )
            .withRequestBody(
              matchingJsonPath("$.cashAccountStatementRequest[?(@.requestDetail.can == '12345678910')]")
            )
            .withRequestBody(
              matchingJsonPath("$.cashAccountStatementRequest[?(@.requestDetail.eori == 'GB123456789012345')]")
            )
            .willReturn(badRequest.withBody(casErrorResponseStr02))
        )

        val result: Either[ErrorDetail, Acc45ResponseCommon] = await(connector.submitStatementRequest(reqDetail01))
        result mustBe Left(errorResponseDetails02)

        verifyExactlyOneEndPointUrlHit(acc45CashAccountStatementRequestEndpointUrl, POST)
      }

      "Backend system faces Internal Server Error" in new Setup {

        wireMockServer.stubFor(
          post(urlPathMatching(acc45CashAccountStatementRequestEndpointUrl))
            .withHeader(X_FORWARDED_HOST, equalTo(MDTP))
            .withHeader(CONTENT_TYPE, equalTo(CONTENT_TYPE_APPLICATION_JSON))
            .withHeader(ACCEPT, equalTo(CONTENT_TYPE_APPLICATION_JSON))
            .withHeader(AUTHORIZATION, equalTo(AUTH_BEARER_TOKEN_VALUE))
            .withRequestBody(
              matchingJsonPath("$.cashAccountStatementRequest[?(@.requestCommon.originatingSystem == 'MDTP')]")
            )
            .withRequestBody(
              matchingJsonPath("$.cashAccountStatementRequest[?(@.requestDetail.can == '12345678910')]")
            )
            .withRequestBody(
              matchingJsonPath("$.cashAccountStatementRequest[?(@.requestDetail.eori == 'GB123456789012345')]")
            )
            .willReturn(serverError.withBody(casErrorResponseStr03))
        )

        val result: Either[ErrorDetail, Acc45ResponseCommon] = await(connector.submitStatementRequest(reqDetail01))
        result mustBe Left(errorResponseDetails03)

        verifyExactlyOneEndPointUrlHit(acc45CashAccountStatementRequestEndpointUrl, POST)
      }

      "Backend system sends ServiceUnavailable error" in new Setup {

        wireMockServer.stubFor(
          post(urlPathMatching(acc45CashAccountStatementRequestEndpointUrl))
            .withHeader(X_FORWARDED_HOST, equalTo(MDTP))
            .withHeader(CONTENT_TYPE, equalTo(CONTENT_TYPE_APPLICATION_JSON))
            .withHeader(ACCEPT, equalTo(CONTENT_TYPE_APPLICATION_JSON))
            .withHeader(AUTHORIZATION, equalTo(AUTH_BEARER_TOKEN_VALUE))
            .withRequestBody(
              matchingJsonPath("$.cashAccountStatementRequest[?(@.requestCommon.originatingSystem == 'MDTP')]")
            )
            .withRequestBody(
              matchingJsonPath("$.cashAccountStatementRequest[?(@.requestDetail.can == '12345678910')]")
            )
            .withRequestBody(
              matchingJsonPath("$.cashAccountStatementRequest[?(@.requestDetail.eori == 'GB123456789012345')]")
            )
            .willReturn(serviceUnavailable.withBody(casErrorResponseStr03))
        )

        val result: Either[ErrorDetail, Acc45ResponseCommon] = await(connector.submitStatementRequest(reqDetail01))

        val errorDetail: ErrorDetail = result.left.getOrElse(defaultErrorDetail)
        errorDetail.errorCode mustBe SERVICE_UNAVAILABLE.toString
        errorDetail.errorMessage must not be empty

        verifyExactlyOneEndPointUrlHit(acc45CashAccountStatementRequestEndpointUrl, POST)
      }

      "SERVICE_UNAVAILABLE error is thrown when the api fails" in new Setup {

        wireMockServer.stubFor(
          post(urlPathMatching(acc45CashAccountStatementRequestEndpointUrl))
            .withHeader(X_FORWARDED_HOST, equalTo(MDTP))
            .withHeader(CONTENT_TYPE, equalTo(CONTENT_TYPE_APPLICATION_JSON))
            .withHeader(ACCEPT, equalTo(CONTENT_TYPE_APPLICATION_JSON))
            .withHeader(AUTHORIZATION, equalTo(AUTH_BEARER_TOKEN_VALUE))
            .withRequestBody(
              matchingJsonPath("$.cashAccountStatementRequest[?(@.requestCommon.originatingSystem == 'MDTP')]")
            )
            .withRequestBody(
              matchingJsonPath("$.cashAccountStatementRequest[?(@.requestDetail.can == '12345678910')]")
            )
            .withRequestBody(
              matchingJsonPath("$.cashAccountStatementRequest[?(@.requestDetail.eori == 'GB123456789012345')]")
            )
            .willReturn(aResponse().withFault(Fault.CONNECTION_RESET_BY_PEER))
        )

        val result: Either[ErrorDetail, Acc45ResponseCommon] = await(connector.submitStatementRequest(reqDetail01))

        val errorDetail: ErrorDetail = result.left.getOrElse(defaultErrorDetail)
        errorDetail.errorCode mustBe SERVICE_UNAVAILABLE.toString
        errorDetail.errorMessage must not be empty

        verifyEndPointUrlHit(acc45CashAccountStatementRequestEndpointUrl, POST)
      }
    }
  }

  override def config: Configuration = Configuration(
    ConfigFactory.parseString(
      s"""
         |microservice {
         |  services {
         |  acc45 {
         |            host = $wireMockHost
         |            port = $wireMockPort
         |        }
         |  }
         |}
         |""".stripMargin
    )
  )

  trait Setup {
    implicit val hc: HeaderCarrier                  = HeaderCarrier()
    val acc45CashAccountStatementRequestEndpointUrl =
      "/customs-financials-hods-stub/accounts/cashaccountstatementrequest/v1"

    val defaultErrorDetail: ErrorDetail              = ErrorDetail(
      "2024-01-21T11:30:47Z",
      "f058ebd6",
      "500",
      "Internal Server Error",
      "MDTP",
      SourceFaultDetail(Seq("Failure in backend System"))
    )
    val reqCommon: CashAccountStatementRequestCommon =
      CashAccountStatementRequestCommon(MDTP, "2021-01-01T10:00:00Z", "601bb176b8e411e")

    val reqDetail01: CashAccountStatementRequestDetail =
      CashAccountStatementRequestDetail("GB123456789012345", "12345678910", "2024-05-10", "2024-05-20")

    val request01: CashAccountStatementRequestContainer =
      CashAccountStatementRequestContainer(CashAccountStatementRequest(reqCommon, reqDetail01))

    val casResponseStr01: String =
      """
        |{
        |    "cashAccountStatementResponse": {
        |        "responseCommon": {
        |            "status": "OK",
        |            "processingDate": "2021-12-17T09:30:47Z"
        |        }
        |    }
        |}""".stripMargin

    val casResponseStr02: String =
      """
        |{
        |    "cashAccountStatementResponse": {
        |        "responseCommon": {
        |            "status": "OK",
        |            "statusText": "003-Request could not be processed",
        |            "processingDate": "2021-12-17T09:30:47Z",
        |            "returnParameters": [
        |                {
        |                    "paramName": "POSITION",
        |                    "paramValue": "FAIL"
        |                }
        |            ]
        |        }
        |    }
        |}""".stripMargin

    val casResponseStr03: String =
      """
        |{
        |    "cashAccountStatementResponse": {
        |        "responseCommon": {
        |            "status": "OK",
        |            "statusText": "602-Exceeded maximum threshold of transactions",
        |            "processingDate": "2021-12-17T09:30:47Z",
        |            "returnParameters": [
        |                {
        |                    "paramName": "POSITION",
        |                    "paramValue": "FAIL"
        |                }
        |            ]
        |        }
        |    }
        |}""".stripMargin

    val casErrorResponseStr01: String =
      """
        |{
        |  "errorDetail": {
        |    "timestamp": "2024-01-21T11:30:47Z",
        |    "correlationId": "f058ebd6-02f7-4d3f-942e-904344e8cde5",
        |    "errorCode": "400",
        |    "errorMessage": "Request could not be processed",
        |    "source": "Backend",
        |    "sourceFaultDetail": {
        |      "detail": [
        |        "Invalid JSON message content used"
        |      ]
        |    }
        |  }
        |}""".stripMargin

    val casErrorResponseStr02: String =
      """
        |{
        |  "errorDetail": {
        |    "timestamp": "2017-02-14T12:58:44Z",
        |    "correlationId": "f1af6020-8f04-4d05-94e7-8a8c7c317b73",
        |    "errorCode": "400",
        |    "errorMessage": "Invalid message : BEFORE TRANSFORMATION",
        |    "source": "Payload",
        |    "sourceFaultDetail": {
        |      "detail": [
        |        "object has missing required properties (['originatingSystem'])"
        |      ]
        |    }
        |  }
        |}""".stripMargin

    val casErrorResponseStr03: String =
      """
        |{
        |  "errorDetail": {
        |    "timestamp": "2024-01-21T11:30:47Z",
        |    "correlationId": "f058ebd6-02f7-4d3f-942e-904344e8cde5",
        |    "errorCode": "500",
        |    "errorMessage": "Internal Server Error",
        |    "source": "Backend",
        |    "sourceFaultDetail": {
        |      "detail": [
        |        "Failure in backend System"
        |      ]
        |    }
        |  }
        |}""".stripMargin

    val casErrorResponseStr04: String =
      """
        |  "errorDetail": {
        |    "timestamp": "2024-01-21T11:30:47Z",
        |    "correlationId": "f058ebd6-02f7-4d3f-942e-904344e8cde5",
        |    "errorMessage": "Internal Server Error",
        |    "source": "Backend",
        |    "sourceFaultDetail": {
        |      "detail": [
        |        "Failure in backend System"
        |      ]
        |    }
        |  }""".stripMargin

    val responseCommon01: Acc45ResponseCommon = Json
      .fromJson[CashAccountStatementResponseContainer](Json.parse(casResponseStr01))
      .get
      .cashAccountStatementResponse
      .responseCommon

    val responseCommon02: Acc45ResponseCommon = Json
      .fromJson[CashAccountStatementResponseContainer](Json.parse(casResponseStr02))
      .get
      .cashAccountStatementResponse
      .responseCommon

    val responseCommon03: Acc45ResponseCommon = Json
      .fromJson[CashAccountStatementResponseContainer](Json.parse(casResponseStr03))
      .get
      .cashAccountStatementResponse
      .responseCommon

    val errorResponseDetails01: ErrorDetail =
      Json.fromJson[CashAccountStatementErrorResponse](Json.parse(casErrorResponseStr01)).get.errorDetail

    val errorResponseDetails02: ErrorDetail =
      Json.fromJson[CashAccountStatementErrorResponse](Json.parse(casErrorResponseStr02)).get.errorDetail

    val errorResponseDetails03: ErrorDetail =
      Json.fromJson[CashAccountStatementErrorResponse](Json.parse(casErrorResponseStr03)).get.errorDetail

    val app: Application          = application().configure(config).build()
    val connector: Acc45Connector = app.injector.instanceOf[Acc45Connector]
  }
}
