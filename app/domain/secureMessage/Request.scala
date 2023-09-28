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

package domain.secureMessage

import domain.secureMessage
import models.{AccountType, HistoricDocumentRequestSearch}
import play.api.libs.json.{Json, OFormat}
import java.time.LocalDate

case class SecureMessageRequest(secureMessageRequest: Request)

object SecureMessageRequest {
  implicit val format: OFormat[SecureMessageRequest] = Json.format[SecureMessageRequest]
}

case class Request(
  externalRef: ExternalReference,
  recipient: Recipient,
  tags: Tags,
  content: List[Content],
  messageType: String,
  validFrom: String,
  alertQueue: String
)

object Request {
  def apply(histDoc: HistoricDocumentRequestSearch): Request = {
    Request(externalRef = ExternalReference(histDoc.searchID.toString, "mdtp"),
      recipient = Recipient(
        regime = "cds",
        taxIdentifier = TaxIdentifier("HMRC-CUS-ORG", histDoc.currentEori),
        params = Params(
          histDoc.params.periodStartMonth,
          histDoc.params.periodStartYear,
          histDoc.params.periodEndMonth,
          histDoc.params.periodEndYear,
          "Financials"),
        email = "test@test.com"),
      tags = Tags("CDS Financials"),
      content = contents(subjectHeader(histDoc.params.accountType)),
      messageType = "newMessageAlert",
      validFrom = LocalDate.now().toString,
      alertQueue = "DEFAULT")
  }

  private def subjectHeader(accountType: String): AccountType =
    accountType match {
      case "DutyDefermentStatement" => AccountType("DutyDefermentStatement")
      case "C79Certificate" => AccountType("C79Certificate")
      case "SecurityStatement" => AccountType("SecurityStatement")
      case "PostponedVATStatement" => AccountType("PostponedVATStatement")
    }

  private def contents(subjectHeader: AccountType): List[Content] =
    List(Content("en", subjectHeader, secureMessage.SecureMessage.body),
      Content("cy", subjectHeader, secureMessage.SecureMessage.body))

  implicit val requestFormat: OFormat[Request] = Json.format[Request]
}