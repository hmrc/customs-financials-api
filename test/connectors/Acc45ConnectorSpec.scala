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

import config.MetaConfig.Platform.MDTP
import models.requests.{
  CashAccountStatementRequest, CashAccountStatementRequestCommon, CashAccountStatementRequestContainer,
  CashAccountStatementRequestDetail
}
import models.responses.*
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import play.api.Application
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json
import play.api.test.Helpers.*
import uk.gov.hmrc.http.client.{HttpClientV2, RequestBuilder}
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse, NotFoundException}
import utils.SpecBase

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class Acc45ConnectorSpec extends SpecBase {

  "submitCashAccStatementRequest" should {

    "return success response" when {

      "request is successfully processed" in new Setup {

        when(requestBuilder.withBody(any())(any(), any(), any())).thenReturn(requestBuilder)
        when(requestBuilder.setHeader(any[(String, String)]())).thenReturn(requestBuilder)
        when(mockHttpClient.post(any)(any)).thenReturn(requestBuilder)
        when(requestBuilder.execute(any, any)).thenReturn(Future.successful(HttpResponse(OK, casResponseStr01)))

        running(app) {
          connector.submitStatementRequest(reqDetail01).map { response =>
            response mustBe Right(responseCommon01)
          }
        }
      }

      "request is not successful due to business error - 'Request could not be processed'" in new Setup {

        when(requestBuilder.withBody(any())(any(), any(), any())).thenReturn(requestBuilder)
        when(requestBuilder.setHeader(any[(String, String)]())).thenReturn(requestBuilder)
        when(mockHttpClient.post(any)(any)).thenReturn(requestBuilder)
        when(requestBuilder.execute(any, any)).thenReturn(Future.successful(HttpResponse(CREATED, casResponseStr02)))

        running(app) {
          connector.submitStatementRequest(reqDetail01).map { response =>
            response mustBe Right(responseCommon02)
          }

        }
      }

      "request is not successful due to business error - 'Exceeded maximum threshold of transactions'" in new Setup {

        when(requestBuilder.withBody(any())(any(), any(), any())).thenReturn(requestBuilder)
        when(requestBuilder.setHeader(any[(String, String)]())).thenReturn(requestBuilder)
        when(mockHttpClient.post(any)(any)).thenReturn(requestBuilder)
        when(requestBuilder.execute(any, any)).thenReturn(Future.successful(HttpResponse(CREATED, casResponseStr03)))

        running(app) {
          connector.submitStatementRequest(reqDetail01).map { response =>
            response mustBe Right(responseCommon03)
          }

        }
      }

    }

    "return ErrorDetail response" when {

      "requests could not be processed at EIS" in new Setup {

        when(requestBuilder.withBody(any())(any(), any(), any())).thenReturn(requestBuilder)
        when(requestBuilder.setHeader(any[(String, String)]())).thenReturn(requestBuilder)
        when(mockHttpClient.post(any)(any)).thenReturn(requestBuilder)
        when(requestBuilder.execute(any, any)).thenReturn(
          Future.successful(HttpResponse(BAD_REQUEST, casErrorResponseStr01))
        )

        running(app) {
          connector.submitStatementRequest(reqDetail01).map { response =>
            response mustBe Left(errorResponseDetails01)
          }
        }
      }

      "requests has missing required properties" in new Setup {

        when(requestBuilder.withBody(any())(any(), any(), any())).thenReturn(requestBuilder)
        when(requestBuilder.setHeader(any[(String, String)]())).thenReturn(requestBuilder)
        when(mockHttpClient.post(any)(any)).thenReturn(requestBuilder)
        when(requestBuilder.execute(any, any)).thenReturn(
          Future.successful(HttpResponse(BAD_REQUEST, casErrorResponseStr02))
        )

        running(app) {
          connector.submitStatementRequest(reqDetail01).map { response =>
            response mustBe Left(errorResponseDetails02)
          }
        }
      }

      "Backend system faces Internal Server Error" in new Setup {

        when(requestBuilder.withBody(any())(any(), any(), any())).thenReturn(requestBuilder)
        when(requestBuilder.setHeader(any[(String, String)]())).thenReturn(requestBuilder)
        when(mockHttpClient.post(any)(any)).thenReturn(requestBuilder)
        when(requestBuilder.execute(any, any)).thenReturn(
          Future.successful(HttpResponse(INTERNAL_SERVER_ERROR, casErrorResponseStr03))
        )

        running(app) {
          connector.submitStatementRequest(reqDetail01).map { response =>
            response mustBe Left(errorResponseDetails03)
          }

        }
      }

      "Backend system sends ServiceUnavailable error" in new Setup {

        when(requestBuilder.withBody(any())(any(), any(), any())).thenReturn(requestBuilder)
        when(requestBuilder.setHeader(any[(String, String)]())).thenReturn(requestBuilder)
        when(mockHttpClient.post(any)(any)).thenReturn(requestBuilder)
        when(requestBuilder.execute(any, any)).thenReturn(
          Future.successful(HttpResponse(SERVICE_UNAVAILABLE, casErrorResponseStr03))
        )

        running(app) {
          connector.submitStatementRequest(reqDetail01).map { response =>
            val errorDetail: ErrorDetail = response.left.getOrElse(defaultErrorDetail)
            errorDetail.errorCode mustBe SERVICE_UNAVAILABLE.toString
            errorDetail.errorMessage must not be empty
          }
        }
      }

      "Not Found Error is thrown from API call" in new Setup {
        when(requestBuilder.withBody(any())(any(), any(), any())).thenReturn(requestBuilder)
        when(requestBuilder.setHeader(any[(String, String)]())).thenReturn(requestBuilder)
        when(mockHttpClient.post(any)(any)).thenReturn(requestBuilder)
        when(requestBuilder.execute(any, any)).thenReturn(Future.failed(new NotFoundException("error")))

        running(app) {
          connector.submitStatementRequest(reqDetail01).map { response =>
            val errorDetail: ErrorDetail = response.left.getOrElse(defaultErrorDetail)
            errorDetail.errorCode mustBe SERVICE_UNAVAILABLE.toString
            errorDetail.errorMessage must not be empty
          }

        }
      }
    }
  }

  trait Setup {
    implicit val hc: HeaderCarrier = HeaderCarrier()

    val mockHttpClient: HttpClientV2   = mock[HttpClientV2]
    val requestBuilder: RequestBuilder = mock[RequestBuilder]

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

    val app: Application = GuiceApplicationBuilder()
      .overrides(
        bind[HttpClientV2].toInstance(mockHttpClient),
        bind[RequestBuilder].toInstance(requestBuilder)
      )
      .configure("microservice.metrics.enabled" -> false, "metrics.enabled" -> false, "auditing.enabled" -> false)
      .build()

    val connector: Acc45Connector = app.injector.instanceOf[Acc45Connector]
  }
}
