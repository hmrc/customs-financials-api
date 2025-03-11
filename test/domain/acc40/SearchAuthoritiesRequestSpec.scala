/*
 * Copyright 2025 HM Revenue & Customs
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

import models.EORI
import org.scalatest.matchers.should.Matchers.shouldBe
import play.api.libs.json.{JsValue, Json}
import utils.SpecBase

class SearchAuthoritiesRequestSpec extends SpecBase {
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

      json shouldBe expectedJson

      (json \ "searchAuthoritiesRequest" \ "requestCommon" \ "receiptDate").as[String]       shouldBe "2024-03-11T12:00:00Z"
      (json \ "searchAuthoritiesRequest" \ "requestCommon" \ "originatingSystem").as[String] shouldBe "TestSystem"
      (json \ "searchAuthoritiesRequest" \ "requestCommon" \ "acknowledgementReference")
        .as[String]                                                                          shouldBe "ABC123456789"
      (json \ "searchAuthoritiesRequest" \ "requestCommon" \ "regime").as[String]            shouldBe "CDS"

      (json \ "searchAuthoritiesRequest" \ "requestDetail" \ "requestingEORI").as[String] shouldBe "GB744638982004"
      (json \ "searchAuthoritiesRequest" \ "requestDetail" \ "searchType").as[String]     shouldBe "0"
      (json \ "searchAuthoritiesRequest" \ "requestDetail" \ "searchID").as[String]       shouldBe "GB744638982004"
    }
  }
}
