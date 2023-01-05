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
