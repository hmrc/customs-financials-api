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

package models.responses

import models.{EORI, ErrorResponse, ExceededThresholdErrorException, NoAssociatedDataException}
import play.api.libs.json.{JsError, JsObject, Json, OFormat, OWrites, Reads, JsSuccess}

case class GuaranteeTransactionsResponse(getGGATransactionResponse: GetGGATransactionResponse)

object GuaranteeTransactionsResponse {

  implicit val thresholdErrorFormat: OFormat[ExceededThresholdErrorException.type] =
    OFormat[ExceededThresholdErrorException.type](Reads[ExceededThresholdErrorException.type] {
      case JsObject(_) => JsSuccess(ExceededThresholdErrorException)
      case _ => JsError("Empty object expected")
    }, OWrites[ExceededThresholdErrorException.type] { _ =>
      Json.obj()
    })

  implicit val noAssociatedDataFormat: OFormat[NoAssociatedDataException.type] =
    OFormat[NoAssociatedDataException.type](Reads[NoAssociatedDataException.type] {
      case JsObject(_) => JsSuccess(NoAssociatedDataException)
      case _ => JsError("Empty object expected")
    }, OWrites[NoAssociatedDataException.type] { _ =>
      Json.obj()
    })

  implicit val errorResponseFormat: OFormat[ErrorResponse] = Json.format[ErrorResponse]

  implicit val responseCommonFormat: OFormat[ResponseCommon] = Json.format[ResponseCommon]

  implicit val defAmountsFormat: OFormat[DefAmounts] = Json.format[DefAmounts]

  implicit val taxTypeFormat: OFormat[TaxTypeG] = Json.format[TaxTypeG]

  implicit val taxTypeGroupFormat: OFormat[TaxTypeGroup] = Json.format[TaxTypeGroup]

  implicit val dueDateFormat: OFormat[DueDate] = Json.format[DueDate]

  implicit val declarationsFormat: OFormat[GuaranteeTransactionDeclaration] =
    Json.format[GuaranteeTransactionDeclaration]

  implicit val responseDetailFormat: OFormat[ResponseDetail] = Json.format[ResponseDetail]

  implicit val getGGATransactionResponseFormat: OFormat[GetGGATransactionResponse] =
    Json.format[GetGGATransactionResponse]

  implicit val guaranteeTransactionsResponseFormat: OFormat[GuaranteeTransactionsResponse] =
    Json.format[GuaranteeTransactionsResponse]
}

case class GetGGATransactionResponse(responseCommon: ResponseCommon,
                                     responseDetail: Option[ResponseDetail])

case class ResponseCommon(status: String,
                          statusText: Option[String],
                          processingDate: String)

case class ResponseDetail(openItems: Boolean, declarations: Seq[GuaranteeTransactionDeclaration])

case class GuaranteeTransactionDeclaration(declarationID: String,
                                           postingDate: String,
                                           declarantsRefNumber: Option[String],
                                           declarantsEORINumber: EORI,
                                           importersEORINumber: EORI,
                                           defAmounts: DefAmounts,
                                           interestCharge: Option[String],
                                           c18Reference: Option[String],
                                           dueDates: Seq[DueDate])

case class DefAmounts(openAmount: Option[String],
                      totalAmount: String,
                      clearedAmount: Option[String],
                      updateDate: String)

case class DueDate(dueDate: String,
                   reasonForSecurity: Option[String],
                   defAmounts: DefAmounts,
                   taxTypeGroups: Seq[TaxTypeGroup])

case class TaxTypeGroup(taxTypeGroup: String,
                        defAmounts: DefAmounts,
                        taxTypes: Seq[TaxTypeG])

case class TaxTypeG(taxType: String, defAmounts: DefAmounts)
