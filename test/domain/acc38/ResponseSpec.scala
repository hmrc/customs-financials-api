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

package domain.acc38

import play.api.http.Status.BAD_REQUEST
import utils.SpecBase
import play.api.libs.json.{JsResultException, JsSuccess, Json}
import utils.TestData.{CDS_CASH_ACC_TYPE, CORRELATION_ID, TEST_ACC_NUMBER, TEST_EORI, TEST_STATUS}
import utils.Utils.emptyString
import domain.acc38.{AccountDetails, ContactDetails, ErrorDetail, SourceFaultDetail}
import models.responses.ErrorSource.mdtp
import models.EmailAddress
import models.responses.SourceFaultDetailMsg.REQUEST_SCHEMA_VALIDATION_ERROR

class ResponseSpec extends SpecBase {

  "ReturnParameter.format" should {
    "generate correct output for Json Reads" in new Setup {
      import ReturnParameter.format

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
      import ResponseCommon.format

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

  "ResponseDetail.format" should {

    "generate correct output for Json Reads" in new Setup {
      import ResponseDetail.format

      Json.fromJson(Json.parse(responseDetailsObJsString)) mustBe JsSuccess(responseDetailsOb)
    }

    "Invalid JSON" should {
      "fail" in {
        val invalidJson = "{ \"status1\": \"pending\", \"eventId1\": \"test_event\" }"

        intercept[JsResultException] {
          Json.parse(invalidJson).as[ResponseDetail]
        }
      }
    }

    "generate correct output for Json Writes" in new Setup {
      Json.toJson(responseDetailsOb) mustBe Json.parse(responseDetailsObJsString)
    }
  }

  "GetCorrespondenceAddressResponse.format" should {

    "generate correct output for Json Reads" in new Setup {
      import GetCorrespondenceAddressResponse.format

      Json.fromJson(Json.parse(getCorrespondenceAddressResponseObJsString)) mustBe JsSuccess(
        getCorrespondenceAddressResOb
      )
    }

    "Invalid JSON" should {
      "fail" in {
        val invalidJson = "{ \"responseCommon1\": \"pending\"}"

        intercept[JsResultException] {
          Json.parse(invalidJson).as[GetCorrespondenceAddressResponse]
        }
      }
    }

    "generate correct output for Json Writes" in new Setup {
      Json.toJson(getCorrespondenceAddressResOb) mustBe Json.parse(getCorrespondenceAddressResponseObJsString)
    }
  }

  "ContactDetails.format" should {

    "generate correct output for Json Reads" in new Setup {
      import ContactDetails.format

      Json.fromJson(Json.parse(contactDetailsObJsString)) mustBe JsSuccess(contactDetailsOb)
    }

    "Invalid JSON" should {
      "fail" in {
        val invalidJson = "{ \"contactName\": \"Jon\", \"addressLine\": \"London Strret\" }"

        intercept[JsResultException] {
          Json.parse(invalidJson).as[ContactDetails]
        }
      }
    }

    "generate correct output for Json Writes" in new Setup {
      Json.toJson(contactDetailsOb) mustBe Json.parse(contactDetailsObJsString)
    }
  }

  "AccountDetails.format" should {

    "generate correct output for Json Reads" in new Setup {
      import AccountDetails.format

      Json.fromJson(Json.parse(accountDetailsObJsString)) mustBe JsSuccess(accountDetailsOb)
    }

    "Invalid JSON" should {
      "fail" in {
        val invalidJson = "{ \"accountTyp1\": \"Jon\", \"accountNum\": \"1234567\" }"

        intercept[JsResultException] {
          Json.parse(invalidJson).as[AccountDetails]
        }
      }
    }

    "generate correct output for Json Writes" in new Setup {
      Json.toJson(accountDetailsOb) mustBe Json.parse(accountDetailsObJsString)
    }
  }

  "SourceFaultDetail.format" should {
    "generate correct output for Json Reads" in new Setup {

      import SourceFaultDetail.format

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

  "ErrorDetail.format" should {
    "generate correct output for Json Reads" in new Setup {

      import ErrorDetail.format

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

  "ErrorResponse.format" should {
    "generate correct output for Json Reads" in new Setup {

      import ErrorResponse.format

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

    val accountDetailsOb: AccountDetails = AccountDetails(CDS_CASH_ACC_TYPE, TEST_ACC_NUMBER)
    val contactDetailsOb: ContactDetails = ContactDetails(
      contactName = Some("John Smith"),
      addressLine1 = "1 High Street",
      addressLine2 = Some("Town"),
      addressLine3 = Some("The County"),
      addressLine4 = Some("England"),
      postCode = Some("AB12 3CD"),
      countryCode = "0044",
      telephone = Some("1234567"),
      faxNumber = Some("7654321"),
      email = Some(EmailAddress("abc@de.com"))
    )

    val contactDetailsObJsString: String =
      """{"contactName":"John Smith",
        |"addressLine1":"1 High Street",
        |"addressLine2":"Town",
        |"addressLine3":"The County",
        |"addressLine4":"England",
        |"postCode":"AB12 3CD",
        |"countryCode":"0044",
        |"telephone":"1234567",
        |"faxNumber":"7654321",
        |"email":"abc@de.com"
        |}""".stripMargin

    val accountDetailsObJsString: String = """{"accountType":"CDSCash","accountNumber":"1234567890987"}""".stripMargin

    val resCommonJsString: String =
      """{"status":"pending","statusText":"test_status","processingDate":"test_data"}""".stripMargin

    val responseCommonOb: ResponseCommon = ResponseCommon(
      status = TEST_STATUS,
      statusText = Some("test_status"),
      processingDate = "test_data",
      returnParameters = None
    )

    val responseDetailsOb: ResponseDetail = ResponseDetail(
      eori = TEST_EORI,
      accountDetails = accountDetailsOb,
      contactDetails = contactDetailsOb
    )

    val responseDetailsObJsString: String =
      """{"eori":"testEORI",
        |"accountDetails":{"accountType":"CDSCash","accountNumber":"1234567890987"},
        |"contactDetails":{
        |"contactName":"John Smith",
        |"addressLine1":"1 High Street",
        |"addressLine2":"Town",
        |"addressLine3":"The County",
        |"addressLine4":"England",
        |"postCode":"AB12 3CD",
        |"countryCode":"0044",
        |"telephone":"1234567",
        |"faxNumber":"7654321",
        |"email":"abc@de.com"
        |}
        |}""".stripMargin

    val getCorrespondenceAddressResponseObJsString: String =
      """{"responseCommon":{"status":"pending","statusText":"test_status","processingDate":"test_data"},
        |"responseDetail":{
        |"eori":"testEORI",
        |"accountDetails":{"accountType":"CDSCash","accountNumber":"1234567890987"},
        |"contactDetails":{
        |"contactName":"John Smith",
        |"addressLine1":"1 High Street",
        |"addressLine2":"Town",
        |"addressLine3":"The County",
        |"addressLine4":"England",
        |"postCode":"AB12 3CD",
        |"countryCode":"0044",
        |"telephone":"1234567",
        |"faxNumber":"7654321",
        |"email":"abc@de.com"
        |}
        |}
        |}""".stripMargin

    val getCorrespondenceAddressResOb: GetCorrespondenceAddressResponse =
      GetCorrespondenceAddressResponse(responseCommonOb, Some(responseDetailsOb))

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
