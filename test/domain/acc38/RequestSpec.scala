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

import utils.SpecBase
import play.api.libs.json.{JsResultException, JsSuccess, Json}
import utils.TestData.{CDS_CASH_ACC_TYPE, DATE_STRING, TEST_ACC_NUMBER, TEST_ACK_REF, TEST_EORI}
import config.MetaConfig.Platform.MDTP

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

  "GetCorrespondenceAddressRequest.format" should {
    "generate correct output for Json Reads" in new Setup {
      import GetCorrespondenceAddressRequest.format

      Json.fromJson(Json.parse(getCorrespondenceAddressRequestJsString)) mustBe JsSuccess(
        getCorrespondenceAddressRequestOb
      )
    }

    "Invalid JSON" should {
      "fail" in {
        val invalidJson = "{ \"requestComm\": \"pending\", \"reqObject\": \"test_event\" }"

        intercept[JsResultException] {
          Json.parse(invalidJson).as[GetCorrespondenceAddressRequest]
        }
      }
    }

    "generate correct output for Json Writes" in new Setup {
      Json.toJson(getCorrespondenceAddressRequestOb) mustBe Json.parse(getCorrespondenceAddressRequestJsString)
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
    val accountDetailsOb: AccountDetails = AccountDetails(CDS_CASH_ACC_TYPE, TEST_ACC_NUMBER)

    val requestCommonOb: RequestCommon =
      RequestCommon(originatingSystem = MDTP, receiptDate = DATE_STRING, acknowledgementReference = TEST_ACK_REF)

    val requestDetailOb: RequestDetail = RequestDetail(
      eori = TEST_EORI,
      accountDetails = accountDetailsOb,
      referenceDate = None
    )

    val getCorrespondenceAddressRequestOb: GetCorrespondenceAddressRequest =
      GetCorrespondenceAddressRequest(requestCommonOb, requestDetailOb)

    val requestOb: Request = Request(getCorrespondenceAddressRequestOb)

    val accountDetailsObJsString: String = """{"accountType":"CDSCash","accountNumber":"1234567890987"}""".stripMargin

    val requestCommonObJsString: String =
      """{"originatingSystem":"MDTP",
        |"receiptDate":"2024-05-28",
        |"acknowledgementReference":"1234567890abcdefgh"
        |}""".stripMargin

    val requestDetailObJsString: String =
      """{"eori":"testEORI","accountDetails":{"accountType":"CDSCash","accountNumber":"1234567890987"}}""".stripMargin

    val getCorrespondenceAddressRequestJsString: String =
      """{"requestCommon":{
        |"originatingSystem":"MDTP","receiptDate":"2024-05-28","acknowledgementReference":"1234567890abcdefgh"
        |},
        |"requestDetail":{
        |"eori":"testEORI","accountDetails":{"accountType":"CDSCash","accountNumber":"1234567890987"
        |}
        |}}""".stripMargin

    val requestObJsString: String =
      """{"getCorrespondenceAddressRequest":{
        |"requestCommon":
        |{"originatingSystem":"MDTP","receiptDate":"2024-05-28","acknowledgementReference":"1234567890abcdefgh"},
        |"requestDetail":
        |{"eori":"testEORI","accountDetails":{"accountType":"CDSCash","accountNumber":"1234567890987"}
        |}
        |}
        |}""".stripMargin
  }
}
