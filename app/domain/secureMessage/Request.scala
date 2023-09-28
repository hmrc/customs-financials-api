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

import models.{AccountType, HistoricDocumentRequestSearch}
import play.api.libs.json.{Json, OFormat}
import java.time.LocalDate

import domain.secureMessage.SecureMessage._
import play.api.{Logger, LoggerLike}
import utils.Utils.encodeToUTF8Charsets

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

  val log: LoggerLike = Logger(this.getClass)

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
      content = contents(histDoc.params.accountType),
      messageType = "newMessageAlert",
      validFrom = LocalDate.now().toString,
      alertQueue = "DEFAULT")
  }

  private def contents(accountType: String): List[Content] = {
    accountType match {
      case "DutyDefermentStatement" => {
        List(Content("en", AccountType("DutyDefermentStatement"), encodeToUTF8Charsets(DutyDefermentBody)),
          Content("cy", AccountType("DutyDefermentStatement"), encodeToUTF8Charsets(DutyDefermentBody)))
      }
      case "C79Certificate" => {
        List(Content("en", AccountType("C79Certificate"), encodeToUTF8Charsets(C79CertificateBody)),
          Content("cy", AccountType("C79Certificate"), encodeToUTF8Charsets(C79CertificateBody)))
      }
      case "SecurityStatement" => {
        List(Content("en", AccountType("SecurityStatement"), encodeToUTF8Charsets(SecurityBody)),
          Content("cy", AccountType("SecurityStatement"), encodeToUTF8Charsets(SecurityBody)))
      }
      case "PostponedVATStatement" => {
        List(Content("en", AccountType("PostponedVATStatement"), encodeToUTF8Charsets(PostponedVATBody)),
          Content("cy", AccountType("PostponedVATStatement"), encodeToUTF8Charsets(PostponedVATBody)))
      }
      case _ => {
        log.error("Unknwon Account Type found in subjectheader")
        List(Content("en", AccountType("Unkown Account Type"), "An error as occured."),
          Content("cy", AccountType("Unkown Account Type"), "An error as occured."))
      }
    }
  }

  implicit val requestFormat: OFormat[Request] = Json.format[Request]
}
