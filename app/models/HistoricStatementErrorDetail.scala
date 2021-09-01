/*
 * Copyright 2021 HM Revenue & Customs
 *
 */

package models

case class HistoricStatementErrorDetail(
                                         timestamp: String,
                                         correlationId: String,
                                         errorCode: String,
                                         errorMessage: Option[String],
                                         source: String,
                                         sourceFaultDetail: Option[HistoricStatementSourceFaultDetail]
                                       )

case class HistoricStatementSourceFaultDetail(detail: Seq[String])