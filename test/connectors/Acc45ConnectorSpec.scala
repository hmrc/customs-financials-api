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
import models.requests.{CashAccountStatementRequest, CashAccountStatementRequestCommon,
  CashAccountStatementRequestContainer, CashAccountStatementRequestDetail}
import models.responses.{Acc45ResponseCommon, CashAccountStatementErrorResponse,
  CashAccountStatementResponseContainer, ErrorDetail}
import play.api.Application
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json
import play.api.test.Helpers._
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient, HttpResponse}
import utils.SpecBase

import scala.concurrent.Future

class Acc45ConnectorSpec extends SpecBase {

  "submitCashAccStatementRequest" should {

    "return success response" when {

      "request is successfully processed" in new Setup {

        when[Future[HttpResponse]](mockHttpClient.POST(any, any, any)(any, any, any, any))
          .thenReturn(Future.successful(HttpResponse(OK, casResponseStr01)))

        running(app) {
          val result = await(connector.submitStatementRequest(reqDetail01))
          result mustBe Right(responseCommon01)
        }
      }

      "request is not successful due to business error - 'Request could not be processed'" in new Setup {

        when[Future[HttpResponse]](mockHttpClient.POST(any, any, any)(any, any, any, any))
          .thenReturn(Future.successful(HttpResponse(CREATED, casResponseStr02)))

        running(app) {
          val result = await(connector.submitStatementRequest(reqDetail01))
          result mustBe Right(responseCommon02)
        }
      }

      "request is not successful due to business error - 'Exceeded maximum threshold of transactions'" in new Setup {

        when[Future[HttpResponse]](mockHttpClient.POST(any, any, any)(any, any, any, any))
          .thenReturn(Future.successful(HttpResponse(CREATED, casResponseStr03)))

        running(app) {
          val result = await(connector.submitStatementRequest(reqDetail01))
          result mustBe Right(responseCommon03)
        }
      }

    }

    "return ErrorDetail response" when {

      "requests could not be processed at EIS" in new Setup {

        when[Future[HttpResponse]](mockHttpClient.POST(any, any, any)(any, any, any, any))
          .thenReturn(Future.successful(HttpResponse(BAD_REQUEST, casErrorResponseStr01)))

        running(app) {
          val result = await(connector.submitStatementRequest(reqDetail01))
          result mustBe Left(errorResponseDetails01)
        }
      }

      "requests has missing required properties" in new Setup {

        when[Future[HttpResponse]](mockHttpClient.POST(any, any, any)(any, any, any, any))
          .thenReturn(Future.successful(HttpResponse(BAD_REQUEST, casErrorResponseStr02)))

        running(app) {
          val result = await(connector.submitStatementRequest(reqDetail01))
          result mustBe Left(errorResponseDetails02)
        }
      }

      "Backend system faces Internal Server Error" in new Setup {

        when[Future[HttpResponse]](mockHttpClient.POST(any, any, any)(any, any, any, any))
          .thenReturn(Future.successful(HttpResponse(INTERNAL_SERVER_ERROR, casErrorResponseStr03)))

        running(app) {
          val result = await(connector.submitStatementRequest(reqDetail01))
          result mustBe Left(errorResponseDetails03)
        }
      }
    }

  }

  trait Setup {
    implicit val hc: HeaderCarrier = HeaderCarrier()
    val mockHttpClient: HttpClient = mock[HttpClient]

    val reqCommon = CashAccountStatementRequestCommon(MDTP, "2021-01-01T10:00:00Z", "601bb176b8e411e")
    val reqDetail01 = CashAccountStatementRequestDetail("GB123456789012345", "12345678910", "2024-05-10", "2024-05-20")
    val request01: CashAccountStatementRequestContainer =
      CashAccountStatementRequestContainer(CashAccountStatementRequest(reqCommon, reqDetail01))

    val casResponseStr01 =
      """
        |{
        |    "cashAccountStatementResponse": {
        |        "responseCommon": {
        |            "status": "OK",
        |            "processingDate": "2021-12-17T09:30:47Z"
        |        }
        |    }
        |}""".stripMargin

    val casResponseStr02 =
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

    val casResponseStr03 =
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

    val casErrorResponseStr01 =
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

    val casErrorResponseStr02 =
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

    val casErrorResponseStr03 =
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

    val responseCommon01: Acc45ResponseCommon = Json.fromJson[CashAccountStatementResponseContainer](
      Json.parse(casResponseStr01)).get.cashAccountStatementResponse.responseCommon

    val responseCommon02: Acc45ResponseCommon = Json.fromJson[CashAccountStatementResponseContainer](
      Json.parse(casResponseStr02)).get.cashAccountStatementResponse.responseCommon

    val responseCommon03: Acc45ResponseCommon = Json.fromJson[CashAccountStatementResponseContainer](
      Json.parse(casResponseStr03)).get.cashAccountStatementResponse.responseCommon

    val errorResponseDetails01: ErrorDetail = Json.fromJson[CashAccountStatementErrorResponse](
      Json.parse(casErrorResponseStr01)).get.errorDetail

    val errorResponseDetails02: ErrorDetail = Json.fromJson[CashAccountStatementErrorResponse](
      Json.parse(casErrorResponseStr02)).get.errorDetail

    val errorResponseDetails03: ErrorDetail = Json.fromJson[CashAccountStatementErrorResponse](
      Json.parse(casErrorResponseStr03)).get.errorDetail

    val app: Application = GuiceApplicationBuilder()
      .overrides(bind[HttpClient].toInstance(mockHttpClient))
      .configure(
        "microservice.metrics.enabled" -> false,
        "metrics.enabled" -> false,
        "auditing.enabled" -> false)
      .build()

    val connector: Acc45Connector = app.injector.instanceOf[Acc45Connector]
  }
}
