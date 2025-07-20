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

import utils.SpecBase
import play.api.libs.json.{JsResultException, JsSuccess, Json}
import config.MetaConfig.Platform.MDTP
import models.{AccountNumber, AccountType, EORI, EmailAddress}
import utils.TestData.{CDS_CASH_ACC_TYPE, DATE_STRING, EORI_VALUE, TEST_ACC_NUMBER, TEST_ACK_REF}

class RequestSpec extends SpecBase {

  "RequestCommon.format" should {
    "generate correct output for Json Reads" in new Setup {

      import RequestCommon.format

      Json.fromJson(Json.parse(requestCommonObJsString)) mustBe JsSuccess(requestCommonOb)
    }

    "Invalid JSON" should {
      "fail" in {
        val invalidJson = "{ \"originatingSystem\": \"MDTP\", \"ackRef\": \"12345\" }"

        intercept[JsResultException] {
          Json.parse(invalidJson).as[RequestCommon]
        }
      }
    }

    "generate correct output for Json Writes" in new Setup {
      Json.toJson(requestCommonOb) mustBe Json.parse(requestCommonObJsString)
    }
  }

  "RequestDetail.format" should {
    "generate correct output for Json Reads" in new Setup {

      import RequestDetail.format

      Json.fromJson(Json.parse(requestDetailObJsString)) mustBe JsSuccess(requestDetailOb)
    }

    "Invalid JSON" should {
      "fail" in {
        val invalidJson = "{ \"eori\": \"GB123456\", \"account\": \"12345\" }"

        intercept[JsResultException] {
          Json.parse(invalidJson).as[RequestDetail]
        }
      }
    }

    "generate correct output for Json Writes" in new Setup {
      Json.toJson(requestDetailOb) mustBe Json.parse(requestDetailObJsString)
    }
  }

  "AmendCorrespondenceAddressRequest.format" should {
    "generate correct output for Json Reads" in new Setup {
      import AmendCorrespondenceAddressRequest.format

      Json.fromJson(Json.parse(amendCorrespondenceAddReqObJsString)) mustBe JsSuccess(amendCorrespondenceAddReqOb)
    }

    "Invalid JSON" should {
      "fail" in {
        val invalidJson = "{ \"status1\": \"pending\", \"eventId1\": \"test_event\" }"

        intercept[JsResultException] {
          Json.parse(invalidJson).as[AmendCorrespondenceAddressRequest]
        }
      }
    }

    "generate correct output for Json Writes" in new Setup {
      Json.toJson(amendCorrespondenceAddReqOb) mustBe Json.parse(amendCorrespondenceAddReqObJsString)
    }
  }

  "ContactDetails.format" should {

    "generate correct output for Json Reads" in new Setup {
      import ContactDetails.format

      Json.fromJson(Json.parse(contactDetailsString)) mustBe JsSuccess(contactDetailsOb)
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
      Json.toJson(contactDetailsOb) mustBe Json.parse(contactDetailsString)
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

  "Request.format" should {
    "generate correct output for Json Reads" in new Setup {

      import Request.format

      Json.fromJson(Json.parse(requestObJsString)) mustBe JsSuccess(requestOb)
    }

    "Invalid JSON" should {
      "fail" in {
        val invalidJson = "{ \"amendContactDetails\": \"pending\"}"

        intercept[JsResultException] {
          Json.parse(invalidJson).as[Request]
        }
      }
    }

    "generate correct output for Json Writes" in new Setup {
      Json.toJson(requestOb) mustBe Json.parse(requestObJsString)
    }
  }

  trait Setup {
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

    val accountDetailsOb: AccountDetails = AccountDetails(CDS_CASH_ACC_TYPE, TEST_ACC_NUMBER)

    val requestCommonOb: RequestCommon =
      RequestCommon(originatingSystem = MDTP, receiptDate = DATE_STRING, acknowledgementReference = TEST_ACK_REF)

    val requestDetailOb: RequestDetail = RequestDetail(
      eori = EORI(EORI_VALUE),
      accountDetails = accountDetailsOb,
      contactDetails = contactDetailsOb,
      reasonForChange = None
    )

    val amendCorrespondenceAddReqOb: AmendCorrespondenceAddressRequest =
      AmendCorrespondenceAddressRequest(requestCommonOb, requestDetailOb)

    val requestOb: Request = Request(amendCorrespondenceAddReqOb)

    val accountDetailsObJsString: String = """{"accountType":"CDSCash","accountNumber":"1234567890987"}""".stripMargin
    val contactDetailsString: String     =
      """{
          |"addressLine1":"1 High Street",
          |"postCode":"AB12 3CD",
          |"telephone":"1234567",
          |"faxNumber":"7654321",
          |"email":"abc@de.com",
          |"addressLine4":"England",
          |"addressLine3":"The County",
          |"contactName":"John Smith",
          |"countryCode":"0044",
          |"addressLine2":"Town"}""".stripMargin

    val requestCommonObJsString: String =
      """{"originatingSystem":"MDTP",
        |"receiptDate":"2024-05-28",
        |"acknowledgementReference":"1234567890abcdefgh"
        |}""".stripMargin

    val requestDetailObJsString: String =
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
        |"email":"abc@de.com"}
        |}""".stripMargin

    val amendCorrespondenceAddReqObJsString: String =
      """{"requestCommon":{
        |"originatingSystem":"MDTP","receiptDate":"2024-05-28","acknowledgementReference":"1234567890abcdefgh"
        |},
        |"requestDetail":{"eori":"testEORI",
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
        |"email":"abc@de.com"}
        |}
        |}""".stripMargin

    val requestObJsString: String =
      """{"amendCorrespondenceAddressRequest":{
        |"requestCommon":{"originatingSystem":"MDTP",
        |"receiptDate":"2024-05-28","acknowledgementReference":"1234567890abcdefgh"},
        |"requestDetail":{"eori":"testEORI",
        |"accountDetails":{"accountType":"CDSCash","accountNumber":"1234567890987"},
        |"contactDetails":{"contactName":"John Smith","addressLine1":"1 High Street",
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
        |}
        |}""".stripMargin
  }
}
