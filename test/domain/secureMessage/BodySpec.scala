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
import models.Params
import play.api.libs.json.{JsSuccess, Json}
import utils.{SpecBase, Utils}
import utils.Utils._
import utils.Utils.emptyString

class BodySpec extends SpecBase {

  "Case Classes should be populated correctly" should {
    "ExternalReference" in new Setup {
      val exRef: ExternalReference = ExternalReference("id", "source")
      exRef mustBe testRef
    }

    "Body" in new Setup {
      val body: Body = Body("eori")
      body mustBe testBody
    }

    "Tax" in new Setup {
      val tax: TaxIdentifier = TaxIdentifier("name", "value")
      tax mustBe testTax
    }

    "Receipient" in new Setup {
      val tax: TaxIdentifier = TaxIdentifier("name", "value")
      val recip: Recipient = Recipient("regime", tax, Name("Company Name"), "test@test.com")
      recip mustBe testRecip
    }

    "Tags" in new Setup {
      val tags: Tags = Tags("NotificationType")
      tags mustBe testTags
    }

    "Content" in new Setup {
      val content: Content = Content("en", "accountType", "body")
      content mustBe testContent
    }
  }

  "Body Text" should {
    "display DutyDeferementBody correctly" in new Setup {
      override val dateRange: DateRange =
        DateRange(dateAsText = "September 2022 to October 2022", dateAsNumber = emptyString)

      dutyDefermentBody("Apples & Pears Ltd", dateRange) mustBe testDutyDefermentBody(applesAndPearsLtd)
    }

    "display C79CertificateBody correctly" in new Setup {
      override val dateRange: DateRange = DateRange(dateAsText = "January 2022 to April 2022", dateAsNumber = emptyString)
      c79CertificateBody("Apples & Pears Ltd", dateRange) mustBe testC79CertificateBody(applesAndPearsLtd)
    }

    "display SecurityBody correctly" in new Setup {
      override val dateRange: DateRange = DateRange(dateAsText = "March 2021 to May 2021", dateAsNumber = emptyString)
      securityBody("Apples & Pears Ltd", dateRange) mustBe testSecurityBody(applesAndPearsLtd)
    }

    "display PostponedVATBody correctly" in new Setup {
      override val dateRange: DateRange = DateRange(dateAsText = "February 2022 to March 2022", dateAsNumber = emptyString)
      postponedVATBody("Apples & Pears Ltd", dateRange) mustBe testPostponedVATBody(applesAndPearsLtd)
    }

    "display DutyDefermentBody correctly when company name is empty for English" in new Setup {
      override val dateRange: DateRange =
        DateRange(dateAsText = "September 2022 to October 2022", dateAsNumber = emptyString)

      dutyDefermentBody(emptyString, dateRange) mustBe testDutyDefermentBody()
    }

    "display C79CertificateBody correctly when company name is empty for English" in new Setup {
      override val dateRange: DateRange =
        DateRange(dateAsText = "January 2022 to April 2022", dateAsNumber = emptyString)

      c79CertificateBody(emptyString, dateRange) mustBe testC79CertificateBody()
    }

    "display SecurityBody correctly when company name is empty for English" in new Setup {
      override val dateRange: DateRange = DateRange(dateAsText = "March 2021 to May 2021", dateAsNumber = emptyString)
      securityBody(emptyString, dateRange) mustBe testSecurityBody()
    }

    "display PostponedVATBody correctly when company name is empty for English" in new Setup {
      override val dateRange: DateRange =
        DateRange(dateAsText = "February 2022 to March 2022", dateAsNumber = emptyString)

      postponedVATBody(emptyString, dateRange) mustBe testPostponedVATBody()
    }

    "should encode correctly" in new Setup {
      Utils.encodeToUTF8Charsets(testDutyDefermentBody(applesAndPearsLtd)) mustBe encodedDutyDeferementBody
    }

    "should encode the body with empty company name correctly for English" in new Setup {
      Utils.encodeToUTF8Charsets(testDutyDefermentBody()) mustBe
        encodedDutyDefermentBodyForEmptyCompanyName
    }

    "display DutyDeferementBody correctly in welsh" in new Setup {
      override val dateRange: DateRange =
        DateRange(dateAsText = "September 2022 to October 2022", dateAsNumber = emptyString)

      dutyDefermentBody("Apples & Pears Ltd", dateRange, welshLangKey) mustBe
        testDutyDefermentBodyCy(applesAndPearsLtd)
    }

    "display C79CertificateBody correctly in welsh" in new Setup {
      override val dateRange: DateRange =
        DateRange(dateAsText = "January 2022 to April 2022", dateAsNumber = emptyString)

      c79CertificateBody("Apples & Pears Ltd", dateRange, welshLangKey) mustBe
        testC79CertificateBodyCy(applesAndPearsLtd)
    }

    "display SecurityBody correctly in welsh" in new Setup {
      override val dateRange: DateRange = DateRange(dateAsText = "March 2021 to May 2021", dateAsNumber = emptyString)
      securityBody("Apples & Pears Ltd", dateRange, welshLangKey) mustBe
        testSecurityBodyCy(applesAndPearsLtd)
    }

    "display PostponedVATBody correctly in welsh" in new Setup {
      override val dateRange: DateRange = DateRange(
        dateAsText = "February 2022 to March 2022", dateAsNumber = emptyString)

      postponedVATBody("Apples & Pears Ltd", dateRange, welshLangKey) mustBe
        testPostponedVATBodyCy(applesAndPearsLtd)
    }

    "display DutyDefermentBody correctly when company name is empty for Welsh" in new Setup {
      override val dateRange: DateRange = DateRange(
        dateAsText = "September 2022 to October 2022", dateAsNumber = emptyString)

      dutyDefermentBody(emptyString, dateRange, welshLangKey) mustBe testDutyDefermentBodyCy()
    }

    "display C79CertificateBody correctly when company name is empty for Welsh" in new Setup {
      override val dateRange: DateRange = DateRange(
        dateAsText = "January 2022 to April 2022", dateAsNumber = emptyString)

      c79CertificateBody(emptyString, dateRange, welshLangKey) mustBe testC79CertificateBodyCy()
    }

    "display SecurityBody correctly when company name is empty for Welsh" in new Setup {
      override val dateRange: DateRange = DateRange(dateAsText = "March 2021 to May 2021", dateAsNumber = emptyString)
      securityBody(emptyString, dateRange, welshLangKey) mustBe testSecurityBodyCy()
    }

    "display PostponedVATBody correctly when company name is empty for Welsh" in new Setup {
      override val dateRange: DateRange = DateRange(
        dateAsText = "February 2022 to March 2022", dateAsNumber = emptyString)

      postponedVATBody(emptyString, dateRange, welshLangKey) mustBe testPostponedVATBodyCy()
    }

    "should encode correctly in welsh" in new Setup {
      Utils.encodeToUTF8Charsets(testDutyDefermentBodyCy(applesAndPearsLtd)) mustBe encodedDutyDeferementBodyCy
    }

    "should encode the body with empty company name correctly for Welsh" in new Setup {
      Utils.encodeToUTF8Charsets(testDutyDefermentBodyCy()) mustBe
        encodedDutyDeferementBodyCyForEmptyCompanyName
    }

    "short text - from the customs" in new Setup {
      val result = "From the Customs Declaration Service"
      result mustBe SignOff
    }

    "short text - There are 2 possible" in new Setup {
      val result: String = "There are 2 possible reasons for this:<br/>" +
        "<ol><li>Statements are only created for the periods in which you imported goods." +
        " Check that you imported goods during the dates you requested.</li><br/>"
      result mustBe TwoReasons
    }

    "short text - made using Customs" in new Setup {
      val result: String = "made using Customs Handling of Import and Export Freight (CHIEF) " +
        "cannot be requested using the Customs Declaration Service."
      result mustBe MadeUsingCustoms
    }

    "short text - were not found" in new Setup {
      val result = " were not found.<br/><br/>"
      result mustBe WereNotFound
    }

    "short text - Check if your" in new Setup {
      val result = " Check if your declarations were made using CHIEF and contact"
      result mustBe CheckIfYourDeclarations
    }

    "short text - Import VAT Certs" in new Setup {
      val result = "<li>Import VAT certificates for declarations"
      result mustBe ImportVATCerts
    }

    "short text - Request Chief" in new Setup {
      val result = " request CHIEF statements.<br/>"
      result mustBe RequestChief
    }

    "short text = you requested for" in new Setup {
      val result = "you requested for"
      result mustBe YouRequestedFor
    }
  }

  "messageType" should {
    "match for DutyDefermentTemplate" in new Setup {
      testDutyDefermentTemplate mustBe DutyDefermentTemplate
    }

    "match for C79CertificateTemplate" in new Setup {
      testC79CertificateTemplate mustBe C79CertificateTemplate
    }

    "match for SecurityTemplate" in new Setup {
      testSecurityTemplate mustBe SecurityTemplate
    }

    "match for TestPostponedVATBody" in new Setup {
      testPostponedVATTemplate mustBe PostponedVATemplate
    }
  }

  "Subject" should {
    "match for TestSubjectDutyDef" in new Setup {
      testSubjectDutyDef mustBe SubjectDutyDef
    }

    "match for TestSubjectCert" in new Setup {
      testSubjectCert mustBe SubjectCert
    }

    "match for TestSubjectSecurity" in new Setup {
      testSubjectSecurity mustBe SubjectSecurity
    }

    "match for TestSubjectImport" in new Setup {
      testSubjectImport mustBe SubjectImport
    }
  }

  "DateRange.apply" should {
    "create the DateRange object with correct contents for English as Text" in new Setup {
      DateRange(params, Utils.englishLangKey).dateAsText mustBe "February 2021 to April 2021"
    }

    "create the DateRange object with correct contents for Welsh as Text" in new Setup {
      DateRange(params, Utils.welshLangKey).dateAsText mustBe "Chwefror 2021 i Ebrill 2021"
    }

    "create the DateRange object with correct contents for English as Numbers" in new Setup {
      DateRange(params, Utils.englishLangKey).dateAsNumber mustBe "02 2021 to 04 2021"
    }

    "create the DateRange object with correct contents for Welsh as Numbers" in new Setup {
      DateRange(params, Utils.welshLangKey).dateAsNumber mustBe "02 2021 i 04 2021"
    }
  }

  "DateRange Reads" should {
    "generate the correct output" in new Setup {

      import domain.secureMessage.DateRange.dateRangeFormat

      Json.fromJson(Json.parse(jsValue)) mustBe JsSuccess(dateRange)
    }
  }

  "DateRange Writes" should {
    "generate the correct output" in new Setup {
      Json.toJson(dateRange) mustBe Json.parse(jsValue)
    }
  }

  trait Setup {

    val testBody: Body = Body("eori")
    val testRef: ExternalReference = ExternalReference("id", "source")
    val testTax: TaxIdentifier = TaxIdentifier("name", "value")
    val testRecip: Recipient = Recipient("regime", testTax, Name("Company Name"), "test@test.com")
    val testTags: Tags = Tags("NotificationType")
    val testContent: Content = Content("en", "accountType", "body")

    val testDutyDefermentTemplate = "customs_financials_requested_duty_deferment_not_found"
    val testC79CertificateTemplate = "customs_financials_requested_c79_certificate_not_found"
    val testSecurityTemplate = "customs_financials_requested_notification_adjustment_statements_not_found"
    val testPostponedVATTemplate = "customs_financials_requested_postponed_import_vat_statements_not_found"

    val testSubjectDutyDef = "Requested duty deferment statements "
    val testSubjectCert = "Requested import VAT certificates (C79) "
    val testSubjectSecurity: String = "Requested notification of adjustment statements "
    val testSubjectImport = "Requested postponed import VAT statements "

    val applesAndPearsLtd = "Apples & Pears Ltd"
    val defaultCompanyName = "Customer"
    val defaultCompanyNameCy = "Gwsmer"

    def testDutyDefermentBody(companyName: String = defaultCompanyName): String = s"Dear $companyName<br/><br/>" +
      "The duty deferment statements you requested for September 2022 to October 2022 were not found." +
      "<br/><br/>There are 2 possible reasons for this:<br/><ol><li>" +
      "Statements are only created for the periods in which you imported goods." +
      " Check that you imported goods during the dates you requested.</li><br/>" +
      "<li>Import VAT certificates for declarations made using Customs Handling " +
      "of Import and Export Freight (CHIEF) cannot be requested using the Customs " +
      "Declaration Service. You can get duty deferment statements for declarations" +
      " made using CHIEF from <a class=\"govuk-link\" href=\"https://secure.hmce.gov.uk/ecom/login/index.html\">" +
      "Duty Deferment Electronic Statements (DDES)</a>.<br/>" +
      "</li></ol>From the Customs Declaration Service"

    def testC79CertificateBody(companyName: String = defaultCompanyName): String = s"Dear $companyName<br/><br/>" +
      "The import VAT certificates you requested for January 2022 to April 2022 were not found." +
      "<br/><br/>There are 2 possible reasons for this:<br/><ol><li>Statements are only created " +
      "for the periods in which you imported goods. Check that you imported goods during" +
      " the dates you requested.</li><br/><li>Import VAT certificates for declarations made" +
      " using Customs Handling of Import and Export Freight (CHIEF) cannot be requested using" +
      " the Customs Declaration Service. Check if your declarations were made using CHIEF and" +
      " contact <a class=\"govuk-link\" href=\"mailto:cbc-c79requests@hmrc.gov.uk\">" +
      "cbc-c79requests@hmrc.gov.uk</a> to request CHIEF statements.<br/></li></ol>" +
      "From the Customs Declaration Service"

    def testSecurityBody(companyName: String = defaultCompanyName): String = s"Dear $companyName<br/><br/>" +
      "The notification of adjustment statements you requested for March 2021 to May 2021 were not found." +
      "<br/><br/>There are 2 possible reasons for this:<br/><ol><li>Statements are only created for the " +
      "periods in which you imported goods. Check that you imported goods during the dates you requested." +
      "</li><br/><li>Notification of adjustment statements for declarations made using Customs Handling " +
      "of Import and Export Freight (CHIEF) cannot be requested using the Customs Declaration Service." +
      "<br/></li></ol>From the Customs Declaration Service"

    def testPostponedVATBody(companyName: String = defaultCompanyName): String = s"Dear $companyName<br/><br/>" +
      "The postponed import VAT statements you requested for February 2022 to March 2022 were not found." +
      "<br/><br/>There are 2 possible reasons for this:<br/><ol><li>Statements are only created for the " +
      "periods in which you imported goods. Check that you imported goods during the dates you requested." +
      "</li><br/><li>Postponed import VAT statements for declarations made using Customs Handling of " +
      "Import and Export Freight (CHIEF) cannot be requested using the Customs Declaration Service. " +
      "Check if your declarations were made using CHIEF and contact <a class=\"govuk-link\" href=" +
      "\"mailto:pvaenquiries@hmrc.gov.uk\">pvaenquiries@hmrc.gov.uk</a> to " +
      "request CHIEF statements.<br/></li></ol>From the Customs Declaration Service"

    val encodedDutyDeferementBody: String = "RGVhciBBcHBsZXMgJiBQZWFycyBMdGQ8YnIvPjxici8+VGhlIGR1dHkgZGVmZX" +
      "JtZW50IHN0YXRlbWVudHMgeW91IHJlcXVlc3RlZCBmb3IgU2VwdGVtYmVyIDIwMjIgdG8gT2N0b2JlciAyMDIyIHdlcmUgbm90IGZ" +
      "vdW5kLjxici8+PGJyLz5UaGVyZSBhcmUgMiBwb3NzaWJsZSByZWFzb25zIGZvciB0aGlzOjxici8+PG9sPjxsaT5TdGF0ZW1lbnRz" +
      "IGFyZSBvbmx5IGNyZWF0ZWQgZm9yIHRoZSBwZXJpb2RzIGluIHdoaWNoIHlvdSBpbXBvcnRlZCBnb29kcy4gQ2hlY2sgdGhhdCB5b" +
      "3UgaW1wb3J0ZWQgZ29vZHMgZHVyaW5nIHRoZSBkYXRlcyB5b3UgcmVxdWVzdGVkLjwvbGk+PGJyLz48bGk+SW1wb3J0IFZBVCBjZX" +
      "J0aWZpY2F0ZXMgZm9yIGRlY2xhcmF0aW9ucyBtYWRlIHVzaW5nIEN1c3RvbXMgSGFuZGxpbmcgb2YgSW1wb3J0IGFuZCBFeHBvcnQg" +
      "RnJlaWdodCAoQ0hJRUYpIGNhbm5vdCBiZSByZXF1ZXN0ZWQgdXNpbmcgdGhlIEN1c3RvbXMgRGVjbGFyYXRpb24gU2VydmljZS4gWW" +
      "91IGNhbiBnZXQgZHV0eSBkZWZlcm1lbnQgc3RhdGVtZW50cyBmb3IgZGVjbGFyYXRpb25zIG1hZGUgdXNpbmcgQ0hJRUYgZnJvbSA8" +
      "YSBjbGFzcz0iZ292dWstbGluayIgaHJlZj0iaHR0cHM6Ly9zZWN1cmUuaG1jZS5nb3YudWsvZWNvbS9sb2dpbi9pbmRleC5odG1sIj5" +
      "EdXR5IERlZmVybWVudCBFbGVjdHJvbmljIFN0YXRlbWVudHMgKERERVMpPC9hPi48YnIvPjwvbGk+PC9vbD5Gcm9tIHRoZSBDdXN0b21" +
      "zIERlY2xhcmF0aW9uIFNlcnZpY2U="

    val encodedDutyDefermentBodyForEmptyCompanyName: String = "RGVhciBDdXN0b21lcjxici8+PGJyLz5UaGUgZHV0eSBkZWZlcm1lbnQ" +
      "gc3RhdG" +
      "VtZW50cyB5b3UgcmVxdWVzdGVkIGZvciBTZXB" +
      "0ZW1iZXIgMjAyMiB0byBPY3RvYmVyIDIwMjIgd2VyZSBub3QgZm91bmQuPGJyLz48YnIvPlRoZXJlIGFyZSAyIHBvc3NpYmxlIHJlYXNvbnM" +
      "gZm9yIHRoaXM6PGJyLz48b2w+PGxpPlN0YXRlbWVudHMgYXJlIG9ubHkgY3JlYXRlZCBmb3IgdGhlIHBlcmlvZHMgaW4gd2hpY2ggeW91IGl" +
      "tcG9ydGVkIGdvb2RzLiBDaGVjayB0aGF0IHlvdSBpbXBvcnRlZCBnb29kcyBkdXJpbmcgdGhlIGRhdGVzIHlvdSByZXF1ZXN0ZWQuPC9saT4" +
      "8YnIvPjxsaT5JbXBvcnQgVkFUIGNlcnRpZmljYXRlcyBmb3IgZGVjbGFyYXRpb25zIG1hZGUgdXNpbmcgQ3VzdG9tcyBIYW5kbGluZyBvZiB" +
      "JbXBvcnQgYW5kIEV4cG9ydCBGcmVpZ2h0IChDSElFRikgY2Fubm90IGJlIHJlcXVlc3RlZCB1c2luZyB0aGUgQ3VzdG9tcyBEZWNsYXJhdGl" +
      "vbiBTZXJ2aWNlLiBZb3UgY2FuIGdldCBkdXR5IGRlZmVybWVudCBzdGF0ZW1lbnRzIGZvciBkZWNsYXJhdGlvbnMgbWFkZSB1c2luZyBDSElF" +
      "RiBmcm9tIDxhIGNsYXNzPSJnb3Z1ay1saW5rIiBocmVmPSJodHRwczovL3NlY3VyZS5obWNlLmdvdi51ay9lY29tL2xvZ2luL2luZGV4Lmh0bW" +
      "wiPkR1dHkgRGVmZXJtZW50IEVsZWN0cm9uaWMgU3RhdGVtZW50cyAoRERFUyk8L2E+Ljxici8+PC9saT48L29sPkZyb20gdGhlIEN1c3RvbXMg" +
      "RGVjbGFyYXRpb24gU2VydmljZQ=="

    def testDutyDefermentBodyCy(companyName: String = defaultCompanyNameCy): String =
      s"Annwyl $companyName <br/><br/>Ni chafwyd hyd i’r" +
        " datganiadau gohirio tollau y gwnaethoch gais amdanynt ar gyfer mis September 2022 to October" +
        " 2022.<br/><br/>Mae dau reswm posibl am hyn:<br/><ol><li>Dim ond ar gyfer y cyfnodau lle y gwnaethoch" +
        " fewnforio nwyddau y mae datganiadau’n cael eu creu. Gwiriwch eich bod wedi mewnforio nwyddau" +
        " yn ystod y dyddiadau y gwnaethoch gais amdanynt.</li><br/><li>Ni ellir defnyddio’r Gwasanaeth" +
        " Datganiadau Tollau (CDS) i wneud cais am dystysgrifau TAW mewnforio ar gyfer datganiadau a" +
        " wnaed gan ddefnyddio system y Tollau ar gyfer Trin Nwyddau a Gaiff eu Mewnforio a’u" +
        " Hallforio (CHIEF). Gallwch ddefnyddio’r gwasanaeth" +
        " <a class=\"govuk-link\" href=\"https://secure.hmce.gov.uk/ecom/login/index.html\">" +
        " Datganiadau Electronig i Ohirio Tollau (DDES)</a>i gael datganiadau gohirio " +
        "tollau ar gyfer datganiadau a wnaed gan ddefnyddio’r gwasanaeth CHIEF." +
        "</li></ol><br/>Oddi wrth y Gwasanaeth Datganiadau Tollau"

    def testC79CertificateBodyCy(companyName: String = defaultCompanyNameCy): String =
      s"Annwyl $companyName<br/><br/>Ni chafwyd hyd i’r" +
        " Tystysgrifau TAW mewnforio y gwnaethoch gais amdanynt ar gyfer mis January 2022 to April" +
        " 2022.<br/><br/>Mae dau reswm posibl am hyn:<br/><ol><li>Dim ond ar gyfer y cyfnodau lle y gwnaethoch" +
        " fewnforio nwyddau y mae datganiadau’n cael eu creu. Gwiriwch eich bod wedi mewnforio nwyddau" +
        " yn ystod y dyddiadau y gwnaethoch gais amdanynt.</li><br/><li>Ni ellir defnyddio’r Gwasanaeth" +
        " Datganiadau Tollau (CDS) i wneud cais am dystysgrifau TAW mewnforio ar gyfer datganiadau a" +
        " wnaed gan ddefnyddio system y Tollau ar gyfer Trin Nwyddau a Gaiff eu Mewnforio a’u Hallforio" +
        " (CHIEF).Gwiriwch os cafodd eich datganiadau eu gwneud drwy ddefnyddio CHIEF a chysylltwch â" +
        " <a class=\"govuk-link\" href=\"mailto:cbc-c79requests@hmrc.gov.uk\">cbc-c79requests@hmrc.gov.uk</a>" +
        " i wneud cais am ddatganiadau CHIEF.</li></ol><br/>Oddi wrth y Gwasanaeth Datganiadau Tollau"

    def testSecurityBodyCy(companyName: String = defaultCompanyNameCy): String =
      s"Annwyl $companyName<br/><br/>Ni chafwyd hyd i’r" +
        " hysbysiad o ddatganiadau addasu y gwnaethoch gais amdanynt ar gyfer misMarch 2021 to" +
        " May 2021.<br/><br/>Mae dau reswm posibl am hyn:<br/><ol><li>Dim ond ar gyfer y cyfnodau lle y" +
        " gwnaethoch fewnforio nwyddau y mae datganiadau’n cael eu creu.Gwiriwch eich bod wedi" +
        " mewnforio nwyddau yn ystod y dyddiadau y gwnaethoch gais amdanynt.</li><br/><li>Ni" +
        " ellir defnyddio’r Gwasanaeth Datganiadau Tollau (CDS) i wneud cais am hysbysiad o" +
        " ddatganiadau addasu ar gyfer datganiadau a wnaed gan ddefnyddio system y Tollau ar" +
        " gyfer Trin Nwyddau a Gaiff eu Mewnforio a’u Hallforio (CHIEF).</li></ol><br/>Oddi" +
        " wrth y Gwasanaeth Datganiadau Tollau"

    def testPostponedVATBodyCy(companyName: String = defaultCompanyNameCy): String =
      s"Annwyl $companyName<br/><br/>Ni chafwyd hyd" +
        " i’r datganiadau TAW mewnforio ohiriedig y gwnaethoch gais amdanynt ar gyfer mis" +
        "February 2022 to March 2022.<br/><br/>Mae dau reswm posibl am hyn:<br/><ol><li>Dim" +
        " ond ar gyfer y cyfnodau lle y gwnaethoch fewnforio nwyddau y mae datganiadau" +
        " ’n cael eu creu. Gwiriwch eich bod wedi mewnforio nwyddau yn ystod y dyddiadau" +
        " y gwnaethoch gais amdanynt.</li><br/>Ni ellir defnyddio’r Gwasanaeth Datganiadau" +
        " Tollau (CDS) i wneud cais am dystysgrifau TAW mewnforio gohiriedig ar gyfer" +
        " datganiadau a wnaed gan ddefnyddio system y Tollau ar gyfer Trin Nwyddau a" +
        " Gaiff eu Mewnforio a’u Hallforio (CHIEF).Gwiriwch os cafodd eich datganiadau" +
        " eu gwneud drwy ddefnyddio CHIEF a chysylltwch â <a class=\"govuk-link\" href=\"mailto:pvaenquiries@hmrc.gov.uk\">" +
        "pvaenquiries@hmrc.gov.uk</a> i wneud cais am ddatganiadau CHIEF</li></ol><br/>Oddi wrth y Gwasanaeth Datganiadau Tollau"

    val encodedDutyDeferementBodyCy: String = "QW5ud3lsIEFwcGxlcyAmIFBlYXJzIEx0ZCA8YnIvPjxici8+TmkgY2hhZnd5ZCBoeW" +
      "QgaeKAmXIgZGF0Z2FuaWFkYXUgZ29oaXJpbyB0b2xsYXUgeSBnd25hZXRob2NoIGdhaXMgYW1kYW55bnQgYXIgZ3lmZXIgbWlzIFNlcHRl" +
      "bWJlciAyMDIyIHRvIE9jdG9iZXIgMjAyMi48YnIvPjxici8+TWFlIGRhdSByZXN3bSBwb3NpYmwgYW0gaHluOjxici8+PG9sPjxsaT5EaW" +
      "0gb25kIGFyIGd5ZmVyIHkgY3lmbm9kYXUgbGxlIHkgZ3duYWV0aG9jaCBmZXduZm9yaW8gbnd5ZGRhdSB5IG1hZSBkYXRnYW5pYWRhdeKA" +
      "mW4gY2FlbCBldSBjcmV1LiBHd2lyaXdjaCBlaWNoIGJvZCB3ZWRpIG1ld25mb3JpbyBud3lkZGF1IHluIHlzdG9kIHkgZHlkZGlhZGF1IH" +
      "kgZ3duYWV0aG9jaCBnYWlzIGFtZGFueW50LjwvbGk+PGJyLz48bGk+TmkgZWxsaXIgZGVmbnlkZGlv4oCZciBHd2FzYW5hZXRoIERhdGdh" +
      "bmlhZGF1IFRvbGxhdSAoQ0RTKSBpIHduZXVkIGNhaXMgYW0gZHlzdHlzZ3JpZmF1IFRBVyBtZXduZm9yaW8gYXIgZ3lmZXIgZGF0Z2FuaW" +
      "FkYXUgYSB3bmFlZCBnYW4gZGRlZm55ZGRpbyBzeXN0ZW0geSBUb2xsYXUgYXIgZ3lmZXIgVHJpbiBOd3lkZGF1IGEgR2FpZmYgZXUgTWV3" +
      "bmZvcmlvIGHigJl1IEhhbGxmb3JpbyAoQ0hJRUYpLiBHYWxsd2NoIGRkZWZueWRkaW/igJlyIGd3YXNhbmFldGggPGEgY2xhc3M9Imdvdn" +
      "VrLWxpbmsiIGhyZWY9Imh0dHBzOi8vc2VjdXJlLmhtY2UuZ292LnVrL2Vjb20vbG9naW4vaW5kZXguaHRtbCI+IERhdGdhbmlhZGF1IEVs" +
      "ZWN0cm9uaWcgaSBPaGlyaW8gVG9sbGF1IChEREVTKTwvYT5pIGdhZWwgZGF0Z2FuaWFkYXUgZ29oaXJpbyB0b2xsYXUgYXIgZ3lmZXIgZG" +
      "F0Z2FuaWFkYXUgYSB3bmFlZCBnYW4gZGRlZm55ZGRpb+KAmXIgZ3dhc2FuYWV0aCBDSElFRi48L2xpPjwvb2w+PGJyLz5PZGRpIHdydGgg" +
      "eSBHd2FzYW5hZXRoIERhdGdhbmlhZGF1IFRvbGxhdQ=="

    val encodedDutyDeferementBodyCyForEmptyCompanyName: String = "QW5ud3lsIEd3c21lciA8YnIvPjxici8+TmkgY2hhZnd5ZCBo" +
      "eWQgaeKAmXIgZGF0Z2FuaWFkYXUgZ29oaXJpbyB0b2xsYXUgeSBnd25hZXRob2NoIGdhaXMgYW1kYW55bnQgYXIgZ3lmZXIgbWlzIFNlcHR" +
      "lbWJlciAyMDIyIHRvIE9jdG9iZXIgMjAyMi48YnIvPjxici8+TWFlIGRhdSByZXN3bSBwb3NpYmwgYW0gaHluOjxici8+PG9sPjxsaT5EaW" +
      "0gb25kIGFyIGd5ZmVyIHkgY3lmbm9kYXUgbGxlIHkgZ3duYWV0aG9jaCBmZXduZm9yaW8gbnd5ZGRhdSB5IG1hZSBkYXRnYW5pYWRhdeKAm" +
      "W4gY2FlbCBldSBjcmV1LiBHd2lyaXdjaCBlaWNoIGJvZCB3ZWRpIG1ld25mb3JpbyBud3lkZGF1IHluIHlzdG9kIHkgZHlkZGlhZGF1IHkg" +
      "Z3duYWV0aG9jaCBnYWlzIGFtZGFueW50LjwvbGk+PGJyLz48bGk+TmkgZWxsaXIgZGVmbnlkZGlv4oCZciBHd2FzYW5hZXRoIERhdGdhbml" +
      "hZGF1IFRvbGxhdSAoQ0RTKSBpIHduZXVkIGNhaXMgYW0gZHlzdHlzZ3JpZmF1IFRBVyBtZXduZm9yaW8gYXIgZ3lmZXIgZGF0Z2FuaWFkYX" +
      "UgYSB3bmFlZCBnYW4gZGRlZm55ZGRpbyBzeXN0ZW0geSBUb2xsYXUgYXIgZ3lmZXIgVHJpbiBOd3lkZGF1IGEgR2FpZmYgZXUgTWV3bmZvcm" +
      "lvIGHigJl1IEhhbGxmb3JpbyAoQ0hJRUYpLiBHYWxsd2NoIGRkZWZueWRkaW/igJlyIGd3YXNhbmFldGggPGEgY2xhc3M9ImdvdnVrLWxpbm" +
      "siIGhyZWY9Imh0dHBzOi8vc2VjdXJlLmhtY2UuZ292LnVrL2Vjb20vbG9naW4vaW5kZXguaHRtbCI+IERhdGdhbmlhZGF1IEVsZWN0cm9uaW" +
      "cgaSBPaGlyaW8gVG9sbGF1IChEREVTKTwvYT5pIGdhZWwgZGF0Z2FuaWFkYXUgZ29oaXJpbyB0b2xsYXUgYXIgZ3lmZXIgZGF0Z2FuaWFkYX" +
      "UgYSB3bmFlZCBnYW4gZGRlZm55ZGRpb+KAmXIgZ3dhc2FuYWV0aCBDSElFRi48L2xpPjwvb2w+PGJyLz5PZGRpIHdydGggeSBHd2FzYW5hZX" +
      "RoIERhdGdhbmlhZGF1IFRvbGxhdQ=="

    val params: Params = Params(periodStartMonth = "02",
      periodStartYear = "2021",
      periodEndMonth = "04",
      periodEndYear = "2021",
      accountType = "PostponedVATStatement",
      dan = "1234567")

    val jsValue: String = """{"dateAsText":"test_msg","dateAsNumber":"test_msg"}""".stripMargin
    val dateRange: DateRange = DateRange(dateAsText = "test_msg", dateAsNumber = "test_msg")
  }
}
