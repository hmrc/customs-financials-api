/*
 * Copyright 2021 HM Revenue & Customs
 *
 */

package domain

import models.{AccountNumber, AccountStatus, AccountType}
import play.api.libs.json.{Json, OFormat}

case class AccountWithAuthorities(accountType: AccountType,
                                  accountNumber: AccountNumber,
                                  accountStatus: AccountStatus,
                                  authorities: Seq[StandingAuthority])

object AccountWithAuthorities {
  implicit val standingAuthorityFormat: OFormat[StandingAuthority] = Json.format[StandingAuthority]
  implicit val accountWithAuthoritiesFormat: OFormat[AccountWithAuthorities] = Json.format[AccountWithAuthorities]
}
