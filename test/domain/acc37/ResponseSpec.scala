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

package domain.acc37

import models.responses.ErrorSource.mdtp
import models.responses.SourceFaultDetailMsg.REQUEST_SCHEMA_VALIDATION_ERROR
import play.api.http.Status.BAD_REQUEST
import utils.SpecBase
import play.api.libs.json.{JsResultException, JsSuccess, Json}
import utils.TestData.{CORRELATION_ID, TEST_STATUS}
import utils.Utils.emptyString
import domain.acc37.{ErrorDetail, SourceFaultDetail}

class ResponseSpec extends SpecBase {

  "ReturnParameter.format" should {
    "generate correct output for Json Reads" in new Setup {
      import ReturnParameter.returnParameter

      Json.fromJson(Json.parse(returnParamJsString)) mustBe JsSuccess(returnParamOb)
    }

    "generate correct output for Json Writes" in new Setup {
      Json.toJson(returnParamOb) mustBe Json.parse(returnParamJsString)
    }

    "throw exception for invalid Json" in {
      val invalidJson = "{ \"status\": \"pending\", \"eventId1\": \"test_event\" }"

      intercept[JsResultException] {
        Json.parse(invalidJson).as[ReturnParameter]
      }
    }
  }

  "ResponseCommon.format" should {

    "generate correct output for Json Reads" in new Setup {
      import ResponseCommon.responseCommonFormat

      Json.fromJson(Json.parse(resCommonJsString)) mustBe JsSuccess(responseCommonOb)
    }

    "Invalid JSON" should {
      "fail" in {
        val invalidJson = "{ \"status1\": \"pending\", \"eventId1\": \"test_event\" }"

        intercept[JsResultException] {
          Json.parse(invalidJson).as[ResponseCommon]
        }
      }
    }

    "generate correct output for Json Writes" in new Setup {
      Json.toJson(responseCommonOb) mustBe Json.parse(resCommonJsString)
    }
  }

  "AmendCorrespondenceAddressResponse.updateResponseFormat" should {

    "generate correct output for Json Reads" in new Setup {
      import AmendCorrespondenceAddressResponse.updateResponseFormat

      Json.fromJson(Json.parse(amendCorrespondenceAddressResObJsString)) mustBe JsSuccess(
        amendCorrespondenceAddressResOb
      )
    }

    "Invalid JSON" should {
      "fail" in {
        val invalidJson = "{ \"responseCommon1\": \"pending\"}"

        intercept[JsResultException] {
          Json.parse(invalidJson).as[AmendCorrespondenceAddressResponse]
        }
      }
    }

    "generate correct output for Json Writes" in new Setup {
      Json.toJson(amendCorrespondenceAddressResOb) mustBe Json.parse(amendCorrespondenceAddressResObJsString)
    }
  }

  "SourceFaultDetail.errorDetailsFormat" should {
    "generate correct output for Json Reads" in new Setup {
      import SourceFaultDetail.errorDetailFormat

      val sourceFaultDetailResultOb: SourceFaultDetail = Json.parse(sourceFaultDetailJsString).as[SourceFaultDetail]
      sourceFaultDetailResultOb.detail.length must be > 0
    }

    "Invalid JSON" should {
      "fail" in {
        val invalidJson = "{ \"errorDet\": \"exception\"}"

        intercept[JsResultException] {
          Json.parse(invalidJson).as[SourceFaultDetail]
        }
      }
    }

    "generate correct output for Json Writes" in new Setup {
      Json.toJson(sourceFaultDetailOb) mustBe Json.parse(sourceFaultDetailJsString)
    }
  }

  "ErrorDetail.errorDetailsFormat" should {
    "generate correct output for Json Reads" in new Setup {
      import ErrorDetail.errorDetailFormat

      val errorDetailResultOb: ErrorDetail = Json.parse(errorDetailJsString).as[ErrorDetail]

      errorDetailResultOb.correlationId mustBe CORRELATION_ID
      errorDetailResultOb.errorCode mustBe BAD_REQUEST.toString
      errorDetailResultOb.source mustBe mdtp
      errorDetailResultOb.errorMessage mustBe empty
      errorDetailResultOb.timestamp mustBe empty

      errorDetailResultOb.sourceFaultDetail.detail.length must be > 0
    }

    "Invalid JSON" should {
      "fail" in {
        val invalidJson = "{ \"errorDet\": \"exception\"}"

        intercept[JsResultException] {
          Json.parse(invalidJson).as[ErrorDetail]
        }
      }
    }

    "generate correct output for Json Writes" in new Setup {
      Json.toJson(errorDetailOb) mustBe Json.parse(errorDetailJsString)
    }
  }

  "ErrorResponse.errorResponseFormat" should {
    "generate correct output for Json Reads" in new Setup {
      import ErrorResponse.errorResponseFormat

      val errorResponseResultOb: ErrorResponse = Json.parse(errorResponseJsString).as[ErrorResponse]

      val errorDetailResultOb: ErrorDetail = errorResponseResultOb.errorDetail

      errorDetailResultOb.correlationId mustBe CORRELATION_ID
      errorDetailResultOb.errorCode mustBe BAD_REQUEST.toString
      errorDetailResultOb.source mustBe mdtp
      errorDetailResultOb.errorMessage mustBe empty
      errorDetailResultOb.timestamp mustBe empty

      errorDetailResultOb.sourceFaultDetail.detail.length must be > 0
    }

    "Invalid JSON" should {
      "fail" in {
        val invalidJson = "{ \"errorDet\": \"exception\"}"

        intercept[JsResultException] {
          Json.parse(invalidJson).as[ErrorResponse]
        }
      }
    }

    "generate correct output for Json Writes" in new Setup {
      Json.toJson(errorResponseOb) mustBe Json.parse(errorResponseJsString)
    }
  }

  trait Setup {
    val returnParamOb: ReturnParameter = ReturnParameter("test_param", "test_param_value")
    val returnParamJsString: String    = """{"paramName":"test_param","paramValue":"test_param_value"}""".stripMargin

    val resCommonJsString: String =
      """{"status":"pending","statusText":"test_status","processingDate":"test_data"}""".stripMargin

    val responseCommonOb: ResponseCommon = ResponseCommon(
      status = TEST_STATUS,
      statusText = Some("test_status"),
      processingDate = "test_data",
      returnParameters = None
    )

    val amendCorrespondenceAddressResObJsString: String =
      """{"responseCommon":{"status":"pending","statusText":"test_status","processingDate":"test_data"}}""".stripMargin

    val amendCorrespondenceAddressResOb: AmendCorrespondenceAddressResponse = AmendCorrespondenceAddressResponse(
      responseCommonOb
    )

    val sourceFaultDetailOb: SourceFaultDetail = SourceFaultDetail(Seq(REQUEST_SCHEMA_VALIDATION_ERROR).toArray)
    val sourceFaultDetailJsString: String      =
      """{"detail":["Failure while validating request against schema"]}""".stripMargin

    val errorDetailOb: ErrorDetail =
      ErrorDetail(emptyString, CORRELATION_ID, BAD_REQUEST.toString, emptyString, mdtp, sourceFaultDetailOb)

    val errorDetailJsString: String =
      """{"timestamp":"",
        |"correlationId":"MDTP_ID",
        |"errorCode":"400",
        |"errorMessage":"",
        |"source":"MDTP",
        |"sourceFaultDetail":{"detail":["Failure while validating request against schema"]}
        |}""".stripMargin

    val errorResponseOb: ErrorResponse = ErrorResponse(errorDetailOb)
    val errorResponseJsString: String  =
      """{"errorDetail":{
        |"timestamp":"",
        |"correlationId":"MDTP_ID",
        |"errorCode":"400",
        |"errorMessage":"",
        |"source":"MDTP",
        |"sourceFaultDetail":{"detail":["Failure while validating request against schema"]}
        |}
        |}""".stripMargin
  }
}
