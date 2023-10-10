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

import models.AccountType
import play.api.libs.json.{Json, OFormat}

case class Body(eori: String)

case class ExternalReference(id: String, source: String)

object ExternalReference {
  implicit val extRefFormat: OFormat[ExternalReference] = Json.format[ExternalReference]
}

case class Recipient(regime: String, taxIdentifier: TaxIdentifier,
                     fullName: String, email: String)

object Recipient {
  implicit val recipientFormat: OFormat[Recipient] = Json.format[Recipient]
}

case class TaxIdentifier(name: String, value: String)

object TaxIdentifier {
  implicit val taxFormat: OFormat[TaxIdentifier] = Json.format[TaxIdentifier]
}

case class Tags(notificationType: String)

object Tags {
  implicit val tagsFormat: OFormat[Tags] = Json.format[Tags]
}

case class Content(lang: String, subject: AccountType, body: String)

object Content {
  implicit val contentFormat: OFormat[Content] = Json.format[Content]
}

object SecureMessage {

  val SignOff = "From the Customs Declaration Service"
  val WereNotFound = " were not found.<br/><br/>"
  val CheckIfYourDeclartions = " Check if your declarations were made using CHIEF and contact"
  val ImportVATCerts = "Import VAT certificates for declarations"
  val RequestChief = " request CHIEF statements.<br/><br/>"

  val TwoReasons = "There are 2 possible reasons for this:<br/><br/>" +
    "Statements are only created for the periods in which you imported goods." +
    " Check that you imported goods during the dates you requested.<br/><br/>"

  val MadeUsingCustoms = "made using Customs Handling of Import and Export Freight (CHIEF) " +
    "cannot be requested using the Customs Declaration Service."

  val YouRequestedFor = "you requested for"

  def DutyDefermentBody(companyName: String): String = s"Dear ${companyName}<br/><br/>" +
    s"The duty deferment statements ${YouRequestedFor} September 2022 to October 2022" +
    s"${WereNotFound}${TwoReasons}" +
    s"${ImportVATCerts} ${MadeUsingCustoms}" +
    " You can get duty deferment statements for declarations made using CHIEF" +
    s" from Duty Deferment Electronic Statements (DDES).<br/><br/>${SignOff}"

  def C79CertificateBody(companyName: String): String = s"Dear ${companyName}<br/><br/>" +
    s"The import VAT certificates ${YouRequestedFor} January 2022 to April 2022" +
    s"${WereNotFound}${TwoReasons}${ImportVATCerts} ${MadeUsingCustoms}" +
    s"${CheckIfYourDeclartions} cbc-c79requests@hmrc.gov.uk to" +
    s"${RequestChief}${SignOff}"

  def SecurityBody(companyName: String): String = s"Dear ${companyName}<br/><br/>" +
    s"The notification of adjustment statements ${YouRequestedFor} March 2021 to May 2021" +
    s"${WereNotFound}${TwoReasons}" +
    s"Notification of adjustment statements for declarations ${MadeUsingCustoms}" +
    s" (Insert guidance on how to get CHIEF NOA statements).<br/><br/>${SignOff}"

  def PostponedVATBody(companyName: String): String = s"Dear ${companyName}<br/><br/>" +
    s"The postponed import VAT statements ${YouRequestedFor} February 2022 to March 2022" +
    s"${WereNotFound}${TwoReasons}" +
    s"Postponed import VAT statements for declarations ${MadeUsingCustoms}" +
    s"${CheckIfYourDeclartions} pvaenquiries@hmrc.gov.uk to" +
    s"${RequestChief}${SignOff}"

  val DutyDefermentTemplate = "customs_financials_requested_duty_deferment_not_found"
  val C79CertificateTemplate = "customs_financials_requested_c79_certificate_not_found"
  val SecurityTemplate = "customs_financials_requested_postponed_import_vat_statements_not_found"
  val PostponedVATemplate = "customs_financials_requested_notification_adjustment_statements_not_found"
}

object SecureMessageResponse
