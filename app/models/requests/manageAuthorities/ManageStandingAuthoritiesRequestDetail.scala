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

package models.requests.manageAuthorities

import models.{AccountNumber, EORI}
import play.api.libs.json.{Json, OWrites}

case class ManageStandingAuthoritiesRequestDetail(ownerEori: EORI,
                                                  isGrant: Boolean,
                                                  authorisedUser: String,
                                                  authorisedUserRole: String,
                                                  accounts: Seq[Account])

object ManageStandingAuthoritiesRequestDetail {

  def grantAuthority(grantAuthorityRequest: GrantAuthorityRequest, eori: EORI): ManageStandingAuthoritiesRequestDetail = {

    val accountNumbers = (
      grantAuthorityRequest.accounts.cash
        +: grantAuthorityRequest.accounts.dutyDeferments.map(Some(_))
        :+ grantAuthorityRequest.accounts.guarantee
      ).collect {
        case Some(accountNumber) => accountNumber
      }

    val authority = Authority(
      grantAuthorityRequest.authority.authorisedEori,
      Some(grantAuthorityRequest.authority.authorisedFromDate),
      grantAuthorityRequest.authority.authorisedToDate,
      Some(grantAuthorityRequest.authority.viewBalance)
    )

    ManageStandingAuthoritiesRequestDetail(
      eori, isGrant = true,
      grantAuthorityRequest.authorisedUser.userName, grantAuthorityRequest.authorisedUser.userRole,
      accountNumbers.map(number => Account(AccountNumber(number), Seq(authority)))
    )
  }
  def revokeAuthority(revokeAuthorityRequest: RevokeAuthorityRequest, eori: EORI): ManageStandingAuthoritiesRequestDetail = {

    val accountNumbers = List(revokeAuthorityRequest.accountNumber)

    val authority = Authority(revokeAuthorityRequest.authorisedEori, None, None, None)

    ManageStandingAuthoritiesRequestDetail(
      eori, isGrant = false,
      revokeAuthorityRequest.authorisedUser.userName, revokeAuthorityRequest.authorisedUser.userRole,
      accountNumbers.map(accountNumber => Account(accountNumber, Seq(authority)))
    )
  }

  implicit val writes: OWrites[ManageStandingAuthoritiesRequestDetail] = Json.writes[ManageStandingAuthoritiesRequestDetail]
}