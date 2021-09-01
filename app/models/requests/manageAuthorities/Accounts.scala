/*
 * Copyright 2021 HM Revenue & Customs
 *
 */

package models.requests.manageAuthorities

case class Accounts(cash: Option[String],
                    dutyDeferments: Seq[String],
                    guarantee: Option[String])






