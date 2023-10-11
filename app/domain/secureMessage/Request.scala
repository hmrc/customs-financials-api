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

import models.{AccountType, HistoricDocumentRequestSearch, EmailAddress}
import play.api.libs.json.{Json, OFormat}
import java.time.LocalDate
import domain.secureMessage.SecureMessage._
import play.api.{Logger, LoggerLike}
import utils.Utils.encodeToUTF8Charsets

case class Request(externalRef: ExternalReference,
                    recipient: Recipient,
                    tags: Tags,
                    content: List[Content],
                    messageType: String,
                    validFrom: String,
                    alertQueue: String)

object Request {

  val log: LoggerLike = Logger(this.getClass)

  def apply(histDoc: HistoricDocumentRequestSearch, email: EmailAddress, company: String): Request = {

    Request(externalRef = ExternalReference(histDoc.searchID.toString, "mdtp"),
      recipient = Recipient(
        regime = "cds",
        taxIdentifier = TaxIdentifier("HMRC-CUS-ORG", histDoc.currentEori),
        fullName = company,
        email = email.value),
      tags = Tags("CDS Financials"),
      content = contents(histDoc.params.accountType, company),
      messageType = MessageTemplate(histDoc.params.accountType),
      validFrom = LocalDate.now().toString,
      alertQueue = "DEFAULT")
  }

  private def MessageTemplate(id: String): String = {
    id match {
      case "DutyDefermentStatement" => DutyDefermentTemplate
      case "C79Certificate" => C79CertificateTemplate
      case "SecurityStatement" => SecurityTemplate
      case "PostponedVATStatement" => PostponedVATemplate
      case _ => "Unknown Template"
    }
  }

  private def contents(accountType: String, company: String): List[Content] = {
    accountType match {
      case "DutyDefermentStatement" => {
        List(Content("en", SubjectDutyDef, encodeToUTF8Charsets(DutyDefermentBody(company))),
          Content("cy", SubjectDutyDef, encodeToUTF8Charsets(DutyDefermentBody(company))))
      }
      case "C79Certificate" => {
        List(Content("en", SubjectCert, encodeToUTF8Charsets(C79CertificateBody(company))),
          Content("cy", SubjectCert, encodeToUTF8Charsets(C79CertificateBody(company))))
      }
      case "SecurityStatement" => {
        List(Content("en", SubjectSecurity, encodeToUTF8Charsets(SecurityBody(company))),
          Content("cy", SubjectSecurity, encodeToUTF8Charsets(SecurityBody(company))))
      }
      case "PostponedVATStatement" => {
        List(Content("en", SubjectImport, encodeToUTF8Charsets(PostponedVATBody(company))),
          Content("cy", SubjectImport, encodeToUTF8Charsets(PostponedVATBody(company))))
      }
    }
  }

  implicit val requestFormat: OFormat[Request] = Json.format[Request]
}
