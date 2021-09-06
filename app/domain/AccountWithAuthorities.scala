/*
 * Copyright 2021 HM Revenue & Customs
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
