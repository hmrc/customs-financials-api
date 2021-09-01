/*
 * Copyright 2021 HM Revenue & Customs
 *
 */

package models.responses

import models.{EORI, ErrorResponse, ExceededThresholdErrorException, NoAssociatedDataException}
import play.api.libs.json.Json

case class GuaranteeTransactionsResponse(getGGATransactionResponse: GetGGATransactionResponse)

object GuaranteeTransactionsResponse {

  implicit val thresholdErrorFormat = Json.format[ExceededThresholdErrorException.type]

  implicit val noAssociatedDataFormat = Json.format[NoAssociatedDataException.type]

  implicit val errorResponseFormat = Json.format[ErrorResponse]

  implicit val responseCommonFormat = Json.format[ResponseCommon]

  implicit val defAmountsFormat = Json.format[DefAmounts]

  implicit val taxTypeFormat = Json.format[TaxType]

  implicit val taxTypeGroupFormat = Json.format[TaxTypeGroup]

  implicit val dueDateFormat = Json.format[DueDate]

  implicit val declarationsFormat = Json.format[GuaranteeTransactionDeclaration]

  implicit val responseDetailFormat = Json.format[ResponseDetail]

  implicit val getGGATransactionResponseFormat = Json.format[GetGGATransactionResponse]

  implicit val guaranteeTransactionsResponseFormat = Json.format[GuaranteeTransactionsResponse]

}

case class GetGGATransactionResponse(responseCommon: ResponseCommon, responseDetail: Option[ResponseDetail])

case class ResponseCommon(status: String, statusText: Option[String], processingDate: String)

case class ResponseDetail(openItems: Boolean, declarations: Seq[GuaranteeTransactionDeclaration])

case class GuaranteeTransactionDeclaration(declarationID: String,
                                           postingDate: String,
                                           declarantsRefNumber: Option[String],
                                           declarantsEORINumber: EORI,
                                           importersEORINumber: EORI,
                                           defAmounts: DefAmounts,
                                           interestCharge: Option[String],
                                           c18Reference: Option[String],
                                           dueDates: Seq[DueDate]
                                          )

case class DefAmounts(openAmount: Option[String], totalAmount: String, clearedAmount: Option[String], updateDate: String)

case class DueDate(dueDate: String, reasonForSecurity: Option[String], defAmounts: DefAmounts, taxTypeGroups: Seq[TaxTypeGroup])

case class TaxTypeGroup(taxTypeGroup: String, defAmounts: DefAmounts, taxTypes: Seq[TaxType])

case class TaxType(taxType: String, defAmounts: DefAmounts)
