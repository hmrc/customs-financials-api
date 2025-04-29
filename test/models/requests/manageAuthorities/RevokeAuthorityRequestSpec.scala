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

package models.requests.manageAuthorities

import models.{AccountNumber, EORI}
import models.requests.manageAuthorities.RevokeAccountType
import play.api.libs.json.{JsSuccess, Json}
import utils.SpecBase

class RevokeAuthorityRequestSpec extends SpecBase {

  "RevokeAuthorityRequest" should {
    "generate correct output for Json reads" in new SetUp {
      import RevokeAuthorityRequest.format

      Json.fromJson(Json.parse(revokeAuthorityRequestJsonString)) mustBe JsSuccess(
        revokeAuthorityRequestObject
      )
    }

    "generate correct output for Json writes" in new SetUp {
      import RevokeAuthorityRequest.format

      Json.toJson(revokeAuthorityRequestObject) mustBe Json.parse(
        revokeAuthorityRequestJsonString
      )
    }
  }

  trait SetUp {
    val revokeAuthorityRequestJsonString: String =
      """
        |{
        |"accountNumber": "123456",
        |"accountType": "DutyDeferment",
        |"authorisedEori": "GB12345",
        |"authorisedUser": {
        |"userName": "testName"
        |"userRole: "testRole"
        |},
        |"ownerEori": "GB98765"
        |""".stripMargin

    val revokeAuthorityRequestObject: RevokeAuthorityRequest = RevokeAuthorityRequest(
      accountNumber = AccountNumber("123456"),
      accountType = CdsDutyDefermentAccount,
      authorisedEori = EORI("GB12345"),
      authorisedUser = AuthorisedUser("testName", "testRole"),
      ownerEori = EORI("GB98765")
    )
  }
}
