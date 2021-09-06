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