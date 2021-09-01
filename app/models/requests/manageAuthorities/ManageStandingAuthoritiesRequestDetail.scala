/*
 * Copyright 2021 HM Revenue & Customs
 *
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