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

import models.Params
import play.api.libs.json.{Json, OFormat}
import utils.Utils.*

case class Body(eori: String)

case class ExternalReference(id: String, source: String)

object ExternalReference {
  implicit val extRefFormat: OFormat[ExternalReference] = Json.format[ExternalReference]
}

case class Recipient(regime: String,
                     taxIdentifier: TaxIdentifier,
                     name: Name,
                     email: String)

case class Name(line1: String)

object Name {
  implicit val nameFormat: OFormat[Name] = Json.format[Name]
}

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

case class Content(lang: String, subject: String, body: String)

object Content {
  implicit val contentFormat: OFormat[Content] = Json.format[Content]
}

case class DateRange(dateAsText: String, dateAsNumber: String)

object DateRange {
  def apply(params: Params, lang: String = englishLangKey): DateRange = {

    val startMonth = params.periodStartMonth
    val endMonth = params.periodEndMonth
    val startYear = params.periodStartYear
    val endYear = params.periodEndYear

    val startMonthFullName = convertMonthValueToFullMonthName(startMonth, lang)
    val endMonthFullName = convertMonthValueToFullMonthName(endMonth, lang)

    val dateRangeMsgText = s"$startMonthFullName$singleSpace$startYear$singleSpace${
      if (lang == welshLangKey) "i" else "to"
    }$singleSpace$endMonthFullName$singleSpace$endYear"

    val dateRangeMsgNum = s"$startMonth$singleSpace$startYear$singleSpace${
      if (lang == welshLangKey) "i" else "to"
    }$singleSpace$endMonth$singleSpace$endYear"

    DateRange(dateRangeMsgText, dateRangeMsgNum)
  }

  implicit val dateRangeFormat: OFormat[DateRange] = Json.format[DateRange]
}

object SecureMessage {

  val SubjectDutyDef = "Requested duty deferment statements "
  val SubjectCert = "Requested import VAT certificates (C79) "
  val SubjectSecurity = "Requested notification of adjustment statements "
  val SubjectImport = "Requested postponed import VAT statements "

  val SubjectDutyDefCy = "Datganiadau gohirio tollau a wnaed cais amdanynt "
  val SubjectCertCy = "Tystysgrifau TAW mewnforio (C79) a wnaed cais amdanynt "
  val SubjectSecurityCy = "Hysbysiad o ddatganiadau addasu a wnaed cais amdanynt"
  val SubjectImportCy = "Datganiadau TAW mewnforio ohiriedig a wnaed cais amdanynt "

  val SignOff = "From the Customs Declaration Service"
  val WereNotFound = " were not found.<br/><br/>"
  val CheckIfYourDeclarations = " Check if your declarations were made using CHIEF and contact"
  val ImportVATCerts = "<li>Import VAT certificates for declarations"
  val RequestChief = " request CHIEF statements.<br/>"

  val TwoReasons: String = "There are 2 possible reasons for this:<br/>" +
    "<ol><li>Statements are only created for the periods in which you imported goods." +
    " Check that you imported goods during the dates you requested.</li><br/>"

  val MadeUsingCustoms: String = "made using Customs Handling of Import and Export Freight (CHIEF) " +
    "cannot be requested using the Customs Declaration Service."

  val YouRequestedFor = "you requested for"

  val DutyDefermentTemplate = "customs_financials_requested_duty_deferment_not_found"
  val C79CertificateTemplate = "customs_financials_requested_c79_certificate_not_found"
  val SecurityTemplate = "customs_financials_requested_notification_adjustment_statements_not_found"
  val PostponedVATemplate = "customs_financials_requested_postponed_import_vat_statements_not_found"

  def dutyDefermentBody(companyName: String,
                        dateRange: DateRange,
                        lang: String = englishLangKey): String = {
    val guidanceLink = "https://secure.hmce.gov.uk/ecom/login/index.html"

    if (lang == englishLangKey) {
      val guidanceLinkText = "Duty Deferment Electronic Statements (DDES)"

      s"Dear ${companyNameForMsg(companyName, lang)}<br/><br/>" +
        s"The duty deferment statements $YouRequestedFor ${dateRange.dateAsText}" +
        s"$WereNotFound$TwoReasons" +
        s"$ImportVATCerts $MadeUsingCustoms" +
        " You can get duty deferment statements for declarations made using CHIEF" +
        s" from ${createHyperLink(guidanceLinkText, guidanceLink)}.<br/></li></ol>$SignOff"
    } else {
      val guidanceLinkText = " Datganiadau Electronig i Ohirio Tollau (DDES)"

      s"Annwyl ${companyNameForMsg(companyName, lang)} <br/><br/>" +
        s"Ni chafwyd hyd i’r datganiadau gohirio tollau y gwnaethoch gais amdanynt ar gyfer mis ${dateRange.dateAsText}." +
        s"<br/><br/>Mae dau reswm posibl am hyn:<br/><ol><li>Dim ond ar gyfer y cyfnodau lle y gwnaethoch fewnforio nwyddau y mae " +
        s"datganiadau’n cael eu creu. Gwiriwch eich bod wedi mewnforio nwyddau yn ystod y dyddiadau y" +
        s" gwnaethoch gais amdanynt.</li><br/>" +
        s"<li>Ni ellir defnyddio’r Gwasanaeth Datganiadau Tollau (CDS) i wneud cais am dystysgrifau" +
        s" TAW mewnforio ar gyfer datganiadau a wnaed gan ddefnyddio system y Tollau ar gyfer Trin" +
        s" Nwyddau a Gaiff eu Mewnforio a’u Hallforio (CHIEF). " +
        s"Gallwch ddefnyddio’r gwasanaeth ${createHyperLink(guidanceLinkText, guidanceLink)}" +
        s"i gael datganiadau gohirio tollau ar gyfer datganiadau a wnaed" +
        s" gan ddefnyddio’r gwasanaeth CHIEF.</li></ol><br/>Oddi wrth y Gwasanaeth Datganiadau Tollau"
    }
  }

  def c79CertificateBody(companyName: String,
                         dateRange: DateRange,
                         lang: String = englishLangKey): String = {
    val guidanceLinkText = "cbc-c79requests@hmrc.gov.uk"
    val guidanceLink = "mailto:cbc-c79requests@hmrc.gov.uk"

    if (lang == englishLangKey) {
      s"Dear ${companyNameForMsg(companyName, lang)}<br/><br/>" +
        s"The import VAT certificates $YouRequestedFor ${dateRange.dateAsText}" +
        s"$WereNotFound$TwoReasons$ImportVATCerts $MadeUsingCustoms" +
        s"$CheckIfYourDeclarations ${createHyperLink(guidanceLinkText, guidanceLink)} to" +
        s"$RequestChief</li></ol>$SignOff"
    } else {

      s"Annwyl ${companyNameForMsg(companyName, lang)}<br/><br/>" +
        s"Ni chafwyd hyd i’r Tystysgrifau TAW mewnforio y gwnaethoch gais amdanynt ar gyfer mis ${dateRange.dateAsText}." +
        s"<br/><br/>Mae dau reswm posibl am hyn:<br/><ol><li>" +
        s"Dim ond ar gyfer y cyfnodau lle y gwnaethoch fewnforio nwyddau y mae datganiadau’n cael eu creu." +
        s" Gwiriwch eich bod wedi mewnforio nwyddau yn ystod y dyddiadau y gwnaethoch gais amdanynt.</li><br/>" +
        s"<li>Ni ellir defnyddio’r Gwasanaeth Datganiadau Tollau (CDS) i wneud cais am dystysgrifau " +
        s"TAW mewnforio ar gyfer datganiadau a wnaed gan ddefnyddio system y Tollau ar gyfer" +
        s" Trin Nwyddau a Gaiff eu Mewnforio a’u Hallforio (CHIEF).Gwiriwch os cafodd eich " +
        s"datganiadau eu gwneud drwy ddefnyddio CHIEF a chysylltwch â" +
        s" ${createHyperLink(guidanceLinkText, guidanceLink)} i wneud cais am ddatganiadau CHIEF." +
        s"</li></ol><br/>Oddi wrth y Gwasanaeth Datganiadau Tollau"

    }
  }

  def securityBody(companyName: String,
                   dateRange: DateRange,
                   lang: String = englishLangKey): String = {
    if (lang == englishLangKey) {
      s"Dear ${companyNameForMsg(companyName, lang)}<br/><br/>" +
        s"The notification of adjustment statements $YouRequestedFor ${dateRange.dateAsText}" +
        s"$WereNotFound$TwoReasons" +
        s"<li>Notification of adjustment statements for declarations $MadeUsingCustoms" +
        s"<br/></li></ol>$SignOff"
    } else {
      s"Annwyl ${companyNameForMsg(companyName, lang)}<br/><br/>" +
        s"Ni chafwyd hyd i’r hysbysiad o ddatganiadau addasu y gwnaethoch gais amdanynt ar gyfer mis" +
        s"${dateRange.dateAsText}." + "<br/><br/>Mae dau reswm posibl am hyn:<br/><ol><li>" +
        "Dim ond ar gyfer y cyfnodau lle y gwnaethoch fewnforio nwyddau y mae datganiadau’n cael eu creu." +
        "Gwiriwch eich bod wedi mewnforio nwyddau yn ystod y dyddiadau y gwnaethoch gais amdanynt.</li><br/>" +
        "<li>Ni ellir defnyddio’r Gwasanaeth Datganiadau Tollau (CDS) i wneud cais am hysbysiad o ddatganiadau " +
        "addasu ar gyfer datganiadau a wnaed gan ddefnyddio system y Tollau ar gyfer Trin Nwyddau a Gaiff " +
        "eu Mewnforio a’u Hallforio (CHIEF)." +
        "</li></ol><br/>Oddi wrth y Gwasanaeth Datganiadau Tollau"
    }
  }

  def postponedVATBody(companyName: String,
                       dateRange: DateRange,
                       lang: String = englishLangKey): String = {
    val guidanceLinkText = "pvaenquiries@hmrc.gov.uk"
    val guidanceLink = "mailto:pvaenquiries@hmrc.gov.uk"

    if (lang == englishLangKey) {
      s"Dear ${companyNameForMsg(companyName, lang)}<br/><br/>" +
        s"The postponed import VAT statements $YouRequestedFor ${dateRange.dateAsText}" +
        s"$WereNotFound$TwoReasons" +
        s"<li>Postponed import VAT statements for declarations $MadeUsingCustoms" +
        s"$CheckIfYourDeclarations ${createHyperLink(guidanceLinkText, guidanceLink)} to" +
        s"$RequestChief</li></ol>$SignOff"
    } else {
      s"Annwyl ${companyNameForMsg(companyName, lang)}<br/><br/>" +
        s"Ni chafwyd hyd i’r datganiadau TAW mewnforio ohiriedig y gwnaethoch gais amdanynt ar gyfer mis" +
        s"${dateRange.dateAsText}." + s"<br/><br/>Mae dau reswm posibl am hyn:<br/><ol><li>" +
        s"Dim ond ar gyfer y cyfnodau lle y gwnaethoch fewnforio nwyddau y mae datganiadau ’n cael eu creu." +
        s" Gwiriwch eich bod wedi mewnforio nwyddau yn ystod y dyddiadau y gwnaethoch gais amdanynt.</li><br/>" +
        s"Ni ellir defnyddio’r Gwasanaeth Datganiadau Tollau (CDS) i wneud cais am dystysgrifau TAW mewnforio" +
        s" gohiriedig ar gyfer datganiadau a wnaed gan ddefnyddio system y Tollau ar gyfer Trin Nwyddau a Gaiff" +
        s" eu Mewnforio a’u Hallforio (CHIEF).Gwiriwch os cafodd eich datganiadau eu gwneud drwy ddefnyddio CHIEF" +
        s" a chysylltwch â ${createHyperLink(guidanceLinkText, guidanceLink)} i wneud cais am ddatganiadau CHIEF" +
        s"</li></ol><br/>Oddi wrth y Gwasanaeth Datganiadau Tollau"
    }
  }

  private def companyNameForMsg(companyName: String,
                                lang: String): String = {
    if (companyName.isEmpty) {
      if (lang == englishLangKey) "Customer" else "Gwsmer"
    } else {
      companyName
    }
  }
}
