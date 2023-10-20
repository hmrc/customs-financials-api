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

import domain.secureMessage.SecureMessage._
import models.{EmailAddress, HistoricDocumentRequestSearch, Params}
import play.api.libs.json.{Json, OFormat}
import play.api.{Logger, LoggerLike}
import utils.Utils.{encodeToUTF8Charsets, englishLangKey, welshLangKey}

import java.time.LocalDate

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
      content = contents(histDoc.params, company),
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

  private def contents(params: Params, company: String): List[Content] = {
    params.accountType match {
      case "DutyDefermentStatement" =>
        List(
          Content(englishLangKey, SubjectDutyDef,
            encodeToUTF8Charsets(DutyDefermentBody(company, DateRange(params, englishLangKey), englishLangKey))),
          Content(welshLangKey, SubjectDutyDefCy,
            encodeToUTF8Charsets(DutyDefermentBody(company, DateRange(params, welshLangKey), welshLangKey))))

      case "C79Certificate" =>
        List(
          Content(englishLangKey, SubjectCert,
            encodeToUTF8Charsets(C79CertificateBody(company, DateRange(params, englishLangKey), englishLangKey))),
          Content(welshLangKey, SubjectCertCy,
            encodeToUTF8Charsets(C79CertificateBody(company, DateRange(params, welshLangKey), welshLangKey))))

      case "SecurityStatement" =>
        List(
          Content(englishLangKey, SubjectSecurity,
            encodeToUTF8Charsets(SecurityBody(company, DateRange(params, englishLangKey), englishLangKey))),
          Content(welshLangKey, SubjectSecurityCy,
            encodeToUTF8Charsets(SecurityBody(company, DateRange(params, welshLangKey), welshLangKey))))

      case "PostponedVATStatement" =>
        List(
          Content(englishLangKey, SubjectImport,
            encodeToUTF8Charsets(PostponedVATBody(company, DateRange(params, englishLangKey), englishLangKey))),
          Content(welshLangKey, SubjectImportCy,
            encodeToUTF8Charsets(PostponedVATBody(company, DateRange(params, welshLangKey), welshLangKey))))
    }
  }

  implicit val requestFormat: OFormat[Request] = Json.format[Request]
}
