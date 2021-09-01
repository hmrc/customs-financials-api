/*
 * Copyright 2021 HM Revenue & Customs
 *
 */

package domain

import models.EORI
import play.api.libs.json.Json

case class Amounts(openAmount: Option[String], totalAmount: String, clearedAmount: Option[String], updateDate: String)

case class DueDate(dueDate: String, reasonForSecurity: Option[String], amounts: Amounts, taxTypeGroups: Seq[TaxTypeGroup])

case class TaxTypeGroup(taxTypeGroup: String, amounts: Amounts, taxType: TaxType)

case class TaxType(taxType: String, amounts: Amounts)

case class GuaranteeTransaction(date: String,
                                movementReferenceNumber: String,
                                balance: Option[String],
                                uniqueConsignmentReference: Option[String],
                                declarantEori: EORI,
                                consigneeEori: EORI,
                                originalCharge: String,
                                dischargedAmount: Option[String],
                                interestCharge: Option[String],
                                c18Reference: Option[String],
                                dueDates: Seq[DueDate])

object GuaranteeTransaction {

  implicit val amountsFormat = Json.format[Amounts]

  implicit val taxTypeFormat = Json.format[TaxType]

  implicit val taxTypeGroupFormat = Json.format[TaxTypeGroup]

  implicit val dueDateFormat = Json.format[DueDate]

  implicit val guaranteeTransactionFormat = Json.format[GuaranteeTransaction]
}
