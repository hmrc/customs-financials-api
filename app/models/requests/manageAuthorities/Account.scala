/*
 * Copyright 2021 HM Revenue & Customs
 *
 */

package models.requests.manageAuthorities

import models.AccountNumber
import play.api.libs.json.{Json, OWrites}

case class Account(accountNumber: AccountNumber,
                   authorities: Seq[Authority])

object Account {
  implicit val writes: OWrites[Account] = Json.writes[Account]
}
