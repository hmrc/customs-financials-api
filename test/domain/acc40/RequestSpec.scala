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

package domain.acc40

import utils.SpecBase
import play.api.libs.json.{JsResultException, JsSuccess, JsValue, Json}
import utils.TestData.{DATE_STRING, EORI_NUMBER, TEST_ACK_REF, TEST_EORI}
import config.MetaConfig.Platform.MDTP
import models.EORI

class RequestSpec extends SpecBase {

  "SearchAuthoritiesRequest JSON writes" should {

    "should correctly serialize to JSON" in {
      val requestCommon = RequestCommon(
        receiptDate = "2024-03-11T12:00:00Z",
        originatingSystem = "TestSystem",
        acknowledgementReference = "ABC123456789",
        regime = "CDS"
      )

      val requestDetail = RequestDetail(
        requestingEORI = EORI("GB744638982004"),
        searchType = "0",
        searchID = EORI("GB744638982004")
      )

      val request                  = domain.acc40.Request(requestCommon, requestDetail)
      val searchAuthoritiesRequest = domain.acc40.SearchAuthoritiesRequest(request)

      val json: JsValue = Json.toJson(searchAuthoritiesRequest)

      val expectedJson = Json.parse(
        """
               {
                 "searchAuthoritiesRequest": {
                   "requestCommon": {
                     "receiptDate": "2024-03-11T12:00:00Z",
                     "originatingSystem": "TestSystem",
                     "acknowledgementReference": "ABC123456789",
                     "regime": "CDS"
                   },
                   "requestDetail": {
                     "requestingEORI": "GB744638982004",
                     "searchType": "0",
                     "searchID": "GB744638982004"
                   }
                 }
               }
               """
      )

      json mustBe expectedJson

      (json \ "searchAuthoritiesRequest" \ "requestCommon" \ "receiptDate").as[String] mustBe "2024-03-11T12:00:00Z"
      (json \ "searchAuthoritiesRequest" \ "requestCommon" \ "originatingSystem").as[String] mustBe "TestSystem"
      (json \ "searchAuthoritiesRequest" \ "requestCommon" \ "acknowledgementReference")
        .as[String] mustBe "ABC123456789"
      (json \ "searchAuthoritiesRequest" \ "requestCommon" \ "regime").as[String] mustBe "CDS"

      (json \ "searchAuthoritiesRequest" \ "requestDetail" \ "requestingEORI").as[String] mustBe "GB744638982004"
      (json \ "searchAuthoritiesRequest" \ "requestDetail" \ "searchType").as[String] mustBe "0"
      (json \ "searchAuthoritiesRequest" \ "requestDetail" \ "searchID").as[String] mustBe "GB744638982004"
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
    val requestDetails: RequestDetail = RequestDetail(TEST_EORI, "D", TEST_EORI)

    val requestOb: Request = Request(requestCommon, requestDetails)

    val requestObJsString: String =
      """{"requestCommon":{
        |"receiptDate":"2024-05-28",
        |"originatingSystem":"MDTP",
        |"acknowledgementReference":"1234567890abcdefgh",
        |"regime":"test_regime"
        |},
        |"requestDetail":{"requestingEORI":"testEORI","searchType":"D","searchID":"testEORI"}}""".stripMargin
  }
}
