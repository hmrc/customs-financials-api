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

package models.responses

import play.api.libs.json.{JsSuccess, Json}
import utils.SpecBase

class CashAccountStatementResponseContainerSpec extends SpecBase {

  "CashAccountStatementResponseContainer Writes" should {

    "create a valid json for response object with statusText" in new Setup {

      Json.toJson(casResponseObj01) mustBe Json.parse(casJson01)
      casResponseObj01.cashAccountStatementResponse.responseCommon.statusText mustBe
        Some("003-Request could not be processed")
    }

    "create a valid json for response object without statusText" in new Setup {

      Json.toJson(casResponseContainerObj02) mustBe Json.parse(casJson02)
      casResponseContainerObj02.cashAccountStatementResponse.responseCommon.statusText mustBe None
    }
  }

  "CashAccountStatementResponseContainer Reads" should {

    "create a response Object with statusText from json" in new Setup {

      import CashAccountStatementResponseContainer.cashAccountStatementResponseWrapperFormat

      Json.fromJson(Json.parse(casJson01)) mustBe JsSuccess(casResponseObj01)
    }

    "create a response Object without statusText from json" in new Setup {

      import CashAccountStatementResponseContainer.cashAccountStatementResponseWrapperFormat

      Json.fromJson(Json.parse(casJson02)) mustBe JsSuccess(casResponseContainerObj02)
    }
  }

  "CashAccountStatementErrorResponse Writes" should {

    "create a valid json format for Error Response object" in new Setup {

      Json.toJson(casErrorResponse01) mustBe Json.parse(casErrorJson01)
    }
  }

  "CashAccountStatementErrorResponse Reads" should {

    "create an Error Response object from json" in new Setup {

      import CashAccountStatementErrorResponse.cashAccountStatementErrorResponseFormat

      Json.fromJson(Json.parse(casErrorJson01)) mustBe JsSuccess(casErrorResponse01)
    }
  }

  trait Setup {

    val casJson01: String =
      """
        |{
        |  "cashAccountStatementResponse": {
        |    "responseCommon": {
        |      "status": "OK",
        |      "statusText": "003-Request could not be processed",
        |      "processingDate": "2021-12-17T09:30:47Z",
        |      "returnParameters": [
        |        {
        |          "paramName": "POSITION",
        |          "paramValue": "FAIL"
        |        }
        |      ]
        |    }
        |  }
        |}""".stripMargin

    val casJson02: String =
      """
        |{
        |  "cashAccountStatementResponse": {
        |    "responseCommon": {
        |      "status": "OK",
        |      "processingDate": "2021-12-17T09:30:47Z"
        |    }
        |  }
        |}""".stripMargin


    val casErrorJson01: String =
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

    val returnParamObj: ReturnParameter = ReturnParameter("POSITION", "FAIL")

    val responseCommonObj01: Acc45ResponseCommon = Acc45ResponseCommon(
      "OK", Some("003-Request could not be processed"),
      "2021-12-17T09:30:47Z", Some(Seq(returnParamObj))
    )

    val casResponseObj01: CashAccountStatementResponseContainer = CashAccountStatementResponseContainer(
      CashAccountStatementResponse(responseCommonObj01))

    val responseCommonObj02: Acc45ResponseCommon = Acc45ResponseCommon("OK", None, "2021-12-17T09:30:47Z", None)

    val casResponseContainerObj02: CashAccountStatementResponseContainer = CashAccountStatementResponseContainer(
      CashAccountStatementResponse(responseCommonObj02)
    )

    val errorDetailObj01: ErrorDetail = ErrorDetail(
      timestamp = "2017-02-14T12:58:44Z",
      correlationId = "f1af6020-8f04-4d05-94e7-8a8c7c317b73",
      errorCode = "400",
      errorMessage = "Invalid message : BEFORE TRANSFORMATION",
      source = "Payload",
      sourceFaultDetail = SourceFaultDetail(Seq("object has missing required properties (['originatingSystem'])"))
    )

    val casErrorResponse01: CashAccountStatementErrorResponse = CashAccountStatementErrorResponse(errorDetailObj01)
  }

}
