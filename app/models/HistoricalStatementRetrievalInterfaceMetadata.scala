/*
 * Copyright 2021 HM Revenue & Customs
 *
 */

package models

case class HistoricalStatementRetrievalInterfaceMetadata(
                                                          statementRequestID: String,
                                                          eori: EORI,
                                                          statementType: FileRole,
                                                          periodStartYear: String,
                                                          periodStartMonth: String,
                                                          periodEndYear: String,
                                                          periodEndMonth: String,
                                                          DAN: Option[String]
                                                        ) {
  override def toString: String = s"HistoricalStatementRetrievalInterfaceMetadata(statementRequestID: $statementRequestID, eori: xxx, statementType: $statementType, periodStartYear: $periodStartYear, periodStartMonth: $periodStartMonth, periodEndYear: $periodEndYear, periodEndMonth: $periodEndMonth, DAN: $DAN"
}