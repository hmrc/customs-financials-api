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

package domain.acc41

import utils.SpecBase
import utils.TestData.{DATE_STRING, TEST_ACK_REF, TEST_EORI}
import config.MetaConfig.Platform.MDTP
import models.EORI
import play.api.libs.json.{JsResultException, JsSuccess, Json}

class RequestSpec extends SpecBase {

  "StandingAuthoritiesForEORIRequest.format" should {

    "generate correct output for Json Reads" in new Setup {
      import StandingAuthoritiesForEORIRequest.format

      Json.fromJson(Json.parse(standingAuthoritiesForEORIRequestObJsString)) mustBe JsSuccess(
        standingAuthoritiesForEORIRequestOb
      )
    }

    "generate correct output for Json Writes" in new Setup {
      Json.toJson(standingAuthoritiesForEORIRequestOb) mustBe Json.parse(standingAuthoritiesForEORIRequestObJsString)
    }

    "throw exception for invalid Json" in {
      val invalidJson = "{ \"account\": \"300\", \"totalAmount\": \"600\" }"

      intercept[JsResultException] {
        Json.parse(invalidJson).as[StandingAuthoritiesForEORIRequest]
      }
    }
  }

  "Request.format" should {
    "generate correct output for Json Reads" in new Setup {
      import Request.format

      Json.fromJson(Json.parse(requestObJsString)) mustBe JsSuccess(requestOb)
    }

    "generate correct output for Json Writes" in new Setup {
      Json.toJson(requestOb) mustBe Json.parse(requestObJsString)
    }

    "throw exception for invalid Json" in {
      val invalidJson = "{ \"account\": \"300\", \"totalAmount\": \"600\" }"

      intercept[JsResultException] {
        Json.parse(invalidJson).as[Request]
      }
    }
  }

  trait Setup {
    val requestCommon: RequestCommon  = RequestCommon(
      receiptDate = DATE_STRING,
      originatingSystem = MDTP,
      acknowledgementReference = TEST_ACK_REF,
      regime = "test_regime"
    )
    val requestDetails: RequestDetail = RequestDetail(TEST_EORI, Some(TEST_EORI))

    val requestOb: Request = Request(requestCommon, requestDetails)

    val requestObJsString: String =
      """{"requestCommon":{
        |"receiptDate":"2024-05-28",
        |"originatingSystem":"MDTP",
        |"acknowledgementReference":"1234567890abcdefgh",
        |"regime":"test_regime"
        |},
        |"requestDetail":{"requestingEORI":"testEORI","alternateEORI":"testEORI"}
        |}""".stripMargin

    val standingAuthoritiesForEORIRequestOb: StandingAuthoritiesForEORIRequest = StandingAuthoritiesForEORIRequest(
      requestOb
    )

    val standingAuthoritiesForEORIRequestObJsString: String =
      """{"standingAuthoritiesForEORIRequest":{
        |"requestCommon":{"receiptDate":"2024-05-28","originatingSystem":"MDTP",
        |"acknowledgementReference":"1234567890abcdefgh","regime":"test_regime"
        |},
        |"requestDetail":{"requestingEORI":"testEORI","alternateEORI":"testEORI"}}}""".stripMargin
  }
}
