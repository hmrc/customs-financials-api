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

import domain.StandingAuthority
import models.EORI
import models.requests.manageAuthorities.{Accounts, AuthorisedUser, GrantAuthorityRequest}
import play.api.libs.json.{JsSuccess, Json}
import utils.SpecBase

class GrantAuthorityRequestSpec extends SpecBase {

  "GrantAuthorityRequest" should {
    "generate correct output for Json Reads" in new SetUp {
      import GrantAuthorityRequest.grantAuthorityRequestFormat

      Json.fromJson(Json.parse(grantAuthorityRequestJsonString)) mustBe JsSuccess(
        grantAuthorityRequestObject
      )
    }

    "generate correct output for Json Writes" in new SetUp {
      import GrantAuthorityRequest.grantAuthorityRequestFormat

      Json.toJson(grantAuthorityRequestObject) mustBe Json.parse(grantAuthorityRequestJsonString)
    }
  }

  trait SetUp {

    val grantAuthorityRequestJsonString: String =
      """
        |{
        |"accounts": {
        |"cash": "cash-account",
        |"dutyDeferments": ["dd-data-1", "dd-data-2"],
        |"guarantee": "guarantee-account"
        |},
        |"authority": {
        |"authorisedEori": "GB12345",
        |"authorisedFromDate": "someStartDate",
        |"authorisedToDate": "someDate",
        |"viewBalance": true
        |},
        |"authorisedUser": {
        |"userName": "testName",
        |"userRole": "testRole"
        |},
        |"editRequest": true,
        |"ownerEori": "GB12345"
        |}
        |""".stripMargin

    val grantAuthorityRequestObject: GrantAuthorityRequest = GrantAuthorityRequest(
      accounts = Accounts(
        cash = Some("cash-account"),
        dutyDeferments = Seq("dd-data-1", "dd-data-2"),
        guarantee = Some("guarantee-account")
      ),
      authority = StandingAuthority(
        authorisedEori = EORI("GB12345"),
        authorisedFromDate = "someStartDate",
        authorisedToDate = Some("someDate"),
        viewBalance = true
      ),
      authorisedUser = AuthorisedUser(
        userName = "testName",
        userRole = "testRole"
      ),
      editRequest = true,
      ownerEori = EORI("GB12345")
    )
  }
}
