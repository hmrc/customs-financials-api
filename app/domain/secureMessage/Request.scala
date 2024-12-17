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

import config.MetaConfig.Platform.{ENROLMENT_KEY, SOURCE_MDTP}
import domain.secureMessage.SecureMessage.*
import models.{EmailAddress, HistoricDocumentRequestSearch, Params}
import play.api.libs.json.{JsValue, Json, OFormat, Writes}
import play.api.libs.ws.BodyWritable
import play.api.{Logger, LoggerLike}
import utils.Utils.{encodeToUTF8Charsets, englishLangKey, welshLangKey}

import java.time.LocalDate

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

  def apply(histDoc: HistoricDocumentRequestSearch, email: EmailAddress, company: String): Request =
    Request(
      externalRef = ExternalReference(histDoc.searchID.toString, SOURCE_MDTP),
      recipient = Recipient(
        regime = "cds",
        taxIdentifier = TaxIdentifier(ENROLMENT_KEY, histDoc.currentEori),
        name = Name(company),
        email = email.value
      ),
      tags = Tags("CDS Financials"),
      content = contents(histDoc.params, company),
      messageType = messageTemplate(histDoc.params.accountType),
      validFrom = LocalDate.now().toString,
      alertQueue = "DEFAULT"
    )

  private def messageTemplate(id: String): String =
    id match {
      case "DutyDefermentStatement" => DutyDefermentTemplate
      case "C79Certificate"         => C79CertificateTemplate
      case "SecurityStatement"      => SecurityTemplate
      case "PostponedVATStatement"  => PostponedVATemplate
      case _                        => "Unknown Template"
    }

  private def contents(params: Params, company: String): List[Content] = {

    val en = DateRange(params, englishLangKey)
    val cy = DateRange(params, welshLangKey)

    params.accountType match {
      case "DutyDefermentStatement" =>
        List(
          Content(
            englishLangKey,
            s"$SubjectDutyDef${en.dateAsNumber}",
            encodeToUTF8Charsets(dutyDefermentBody(company, en, englishLangKey))
          ),
          Content(
            welshLangKey,
            s"$SubjectDutyDefCy${cy.dateAsNumber}",
            encodeToUTF8Charsets(dutyDefermentBody(company, cy, welshLangKey))
          )
        )

      case "C79Certificate" =>
        List(
          Content(
            englishLangKey,
            s"$SubjectCert${en.dateAsNumber}",
            encodeToUTF8Charsets(c79CertificateBody(company, en, englishLangKey))
          ),
          Content(
            welshLangKey,
            s"$SubjectCertCy${cy.dateAsNumber}",
            encodeToUTF8Charsets(c79CertificateBody(company, cy, welshLangKey))
          )
        )

      case "SecurityStatement" =>
        List(
          Content(
            englishLangKey,
            s"$SubjectSecurity${en.dateAsNumber}",
            encodeToUTF8Charsets(securityBody(company, en, englishLangKey))
          ),
          Content(
            welshLangKey,
            s"$SubjectSecurityCy${cy.dateAsNumber}",
            encodeToUTF8Charsets(securityBody(company, cy, welshLangKey))
          )
        )

      case "PostponedVATStatement" =>
        List(
          Content(
            englishLangKey,
            s"$SubjectImport${en.dateAsNumber}",
            encodeToUTF8Charsets(postponedVATBody(company, en, englishLangKey))
          ),
          Content(
            welshLangKey,
            s"$SubjectImportCy${cy.dateAsNumber}",
            encodeToUTF8Charsets(postponedVATBody(company, cy, welshLangKey))
          )
        )
    }
  }

  implicit val requestFormat: OFormat[Request] = Json.format[Request]

  implicit def jsonBodyWritable[T](implicit
    writes: Writes[T],
    jsValueBodyWritable: BodyWritable[JsValue]
  ): BodyWritable[T] = jsValueBodyWritable.map(writes.writes)
}
