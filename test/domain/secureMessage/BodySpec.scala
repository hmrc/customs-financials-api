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

class BodySpec extends SpecBase {

  "Case Classes should be populated correctly" should {
    "ExternalReference" in new Setup {
      val exRef = ExternalReference("id", "source")
      exRef mustBe TestRef
    }

    "Body" in new Setup {
      val body = Body("eori")
      body mustBe TestBody
    }

    "Tax" in new Setup {
      val tax = TaxIdentifier("name", "value")
      tax mustBe TestTax
    }

    "Receipient" in new Setup {
      val tax = TaxIdentifier("name", "value")
      val recip = Recipient("regime", tax, "Company Name", "test@test.com")
      recip mustBe TestRecip
    }

    "Tags" in new Setup {
      val tags = Tags("NotificationType")
      tags mustBe TestTags
    }

    "Content" in new Setup {
      val content = Content("en", "accountType", "body")
      content mustBe TestContent
    }
  }

  "Body Text" should {
    "display DutyDeferementBody correctly" in new Setup {
      override val dateRange: DateRange = DateRange(dateAsText = "September 2022 to October 2022", dateAsNumber = "")
      DutyDefermentBody("Apples & Pears Ltd", dateRange) mustBe TestDutyDefermentBody
    }

    "display C79CertificateBody correctly" in new Setup {
      override val dateRange: DateRange = DateRange(dateAsText = "January 2022 to April 2022", dateAsNumber = "")
      C79CertificateBody("Apples & Pears Ltd", dateRange) mustBe TestC79CertificateBody
    }

    "display SecurityBody correctly" in new Setup {
      override val dateRange: DateRange = DateRange(dateAsText = "March 2021 to May 2021", dateAsNumber = "")
      SecurityBody("Apples & Pears Ltd", dateRange) mustBe TestSecurityBody
    }

    "display PostponedVATBody correctly" in new Setup {
      override val dateRange: DateRange = DateRange(dateAsText = "February 2022 to March 2022", dateAsNumber = "")
      PostponedVATBody("Apples & Pears Ltd", dateRange) mustBe TestPostponedVATBody
    }

    "should encode correctly" in new Setup {
      val res = Utils.encodeToUTF8Charsets(TestDutyDefermentBody)
      res mustBe encodedDutyDeferementBody
    }

    "display DutyDeferementBody correctly in welsh" in new Setup {
      override val dateRange: DateRange = DateRange(dateAsText = "September 2022 to October 2022", dateAsNumber = "")
      DutyDefermentBody("Apples & Pears Ltd", dateRange, welshLangKey) mustBe TestDutyDefermentBodyCy
    }

    "display C79CertificateBody correctly in welsh" in new Setup {
      override val dateRange: DateRange = DateRange(dateAsText = "January 2022 to April 2022", dateAsNumber = "")
      C79CertificateBody("Apples & Pears Ltd", dateRange, welshLangKey) mustBe TestC79CertificateBodyCy
    }

    "display SecurityBody correctly in welsh" in new Setup {
      override val dateRange: DateRange = DateRange(dateAsText = "March 2021 to May 2021", dateAsNumber = "")
      SecurityBody("Apples & Pears Ltd", dateRange, welshLangKey) mustBe TestSecurityBodyCy
    }

    "display PostponedVATBody correctly in welsh" in new Setup {
      override val dateRange: DateRange = DateRange(dateAsText = "February 2022 to March 2022", dateAsNumber = "")
      PostponedVATBody("Apples & Pears Ltd", dateRange, welshLangKey) mustBe TestPostponedVATBodyCy
    }

    "should encode correctly in welsh" in new Setup {
      val res = Utils.encodeToUTF8Charsets(TestDutyDefermentBodyCy)
      res mustBe encodedDutyDeferementBodyCy
    }

    "short text - from the customs" in new Setup {
      val result = "From the Customs Declaration Service"
      result mustBe SignOff
    }

    "short text - There are 2 possible" in new Setup {
      val result = "There are 2 possible reasons for this:<br/>" +
        "<ol><li>Statements are only created for the periods in which you imported goods." +
        " Check that you imported goods during the dates you requested.</li><br/>"
      result mustBe TwoReasons
    }

    "short text - made using Customs" in new Setup {
      val result = "made using Customs Handling of Import and Export Freight (CHIEF) " +
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
      TestDutyDefermentTemplate mustBe DutyDefermentTemplate
    }

    "match for C79CertificateTemplate" in new Setup {
      TestC79CertificateTemplate mustBe C79CertificateTemplate
    }

    "match for SecurityTemplate" in new Setup {
      TestSecurityTemplate mustBe SecurityTemplate
    }

    "match for TestPostponedVATBody" in new Setup {
      TestPostponedVATTemplate mustBe PostponedVATemplate
    }
  }

  "Subject" should {
    "match for TestSubjectDutyDef" in new Setup {
      TestSubjectDutyDef mustBe SubjectDutyDef
    }

    "match for TestSubjectCert" in new Setup {
      TestSubjectCert mustBe SubjectCert
    }

    "match for TestSubjectSecurity" in new Setup {
      TestSubjectSecurity mustBe SubjectSecurity
    }

    "match for TestSubjectImport" in new Setup {
      TestSubjectImport mustBe SubjectImport
    }
  }

  "DateRange.apply" should {
    "create the DateRange.dateAsText object with correct contents for English" in new Setup {
      DateRange(params, Utils.englishLangKey).dateAsText mustBe "February 2021 to April 2021"
    }

    "create the DateRange.dateAsText object with correct contents for Welsh" in new Setup {
      DateRange(params, Utils.welshLangKey).dateAsText mustBe "Chwefror 2021 i Ebrill 2021"
    }

    "create the DateRange.dateAsNumber object with correct contents for English" in new Setup {
      DateRange(params, Utils.englishLangKey).dateAsNumber mustBe "02 2021 to 04 2021"
    }

    "create the DateRange.dateAsNumber object with correct contents for Welsh" in new Setup {
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

    val TestBody = Body("eori")
    val TestRef = ExternalReference("id", "source")
    val TestTax = TaxIdentifier("name", "value")
    val TestRecip = Recipient("regime", TestTax, "Company Name", "test@test.com")
    val TestTags = Tags("NotificationType")
    val TestContent = Content("en", "accountType", "body")

    val TestDutyDefermentTemplate = "customs_financials_requested_duty_deferment_not_found"
    val TestC79CertificateTemplate = "customs_financials_requested_c79_certificate_not_found"
    val TestSecurityTemplate = "customs_financials_requested_notification_adjustment_statements_not_found"
    val TestPostponedVATTemplate = "customs_financials_requested_postponed_import_vat_statements_not_found"

    val TestSubjectDutyDef = "Requested duty deferment statements "
    val TestSubjectCert = "Requested import VAT certificates (C79) "
    val TestSubjectSecurity: String = "Requested notification of adjustment statements "
    val TestSubjectImport = "Requested postponed import VAT statements "

    val TestDutyDefermentBody: String = "Dear Apples & Pears Ltd<br/><br/>" +
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

    val TestC79CertificateBody: String = "Dear Apples & Pears Ltd<br/><br/>" +
      "The import VAT certificates you requested for January 2022 to April 2022 were not found." +
      "<br/><br/>There are 2 possible reasons for this:<br/><ol><li>Statements are only created " +
      "for the periods in which you imported goods. Check that you imported goods during" +
      " the dates you requested.</li><br/><li>Import VAT certificates for declarations made" +
      " using Customs Handling of Import and Export Freight (CHIEF) cannot be requested using" +
      " the Customs Declaration Service. Check if your declarations were made using CHIEF and" +
      " contact <a class=\"govuk-link\" href=\"mailto:cbc-c79requests@hmrc.gov.uk\">" +
      "cbc-c79requests@hmrc.gov.uk</a> to request CHIEF statements.<br/></li></ol>" +
      "From the Customs Declaration Service"

    val TestSecurityBody: String = "Dear Apples & Pears Ltd<br/><br/>" +
      "The notification of adjustment statements you requested for March 2021 to May 2021 were not found." +
      "<br/><br/>There are 2 possible reasons for this:<br/><ol><li>Statements are only created for the " +
      "periods in which you imported goods. Check that you imported goods during the dates you requested." +
      "</li><br/><li>Notification of adjustment statements for declarations made using Customs Handling " +
      "of Import and Export Freight (CHIEF) cannot be requested using the Customs Declaration Service." +
      "<br/></li></ol>From the Customs Declaration Service"

    val TestPostponedVATBody: String = "Dear Apples & Pears Ltd<br/><br/>" +
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

    val TestDutyDefermentBodyCy: String = "Annwyl Apples & Pears Ltd <br/><br/>Ni chafwyd hyd i’r" +
      " datganiadau gohirio tollau y gwnaethoch gais amdanynt ar gyfer mis September 2022 to October" +
      " 2022.Mae dau reswm posibl am hyn:<br/><ol><li>Dim ond ar gyfer y cyfnodau lle y gwnaethoch" +
      " fewnforio nwyddau y mae datganiadau’n cael eu creu. Gwiriwch eich bod wedi mewnforio nwyddau" +
      " yn ystod y dyddiadau y gwnaethoch gais amdanynt.</li><br/><li>Ni ellir defnyddio’r Gwasanaeth" +
      " Datganiadau Tollau (CDS) i wneud cais am dystysgrifau TAW mewnforio ar gyfer datganiadau a" +
      " wnaed gan ddefnyddio system y Tollau ar gyfer Trin Nwyddau a Gaiff eu Mewnforio a’u" +
      " Hallforio (CHIEF). Gallwch ddefnyddio’r gwasanaeth" +
      " <a class=\"govuk-link\" href=\"https://secure.hmce.gov.uk/ecom/login/index.html\">" +
      " Datganiadau Electronig i Ohirio Tollau (DDES)</a>i gael datganiadau gohirio " +
      "tollau ar gyfer datganiadau a wnaed gan ddefnyddio’r gwasanaeth CHIEF." +
      "</li></ol><br/>Oddi wrth y Gwasanaeth Datganiadau Tollau"

    val TestC79CertificateBodyCy: String = "Annwyl Apples & Pears Ltd<br/><br/>Ni chafwyd hyd i’r" +
      " Tystysgrifau TAW mewnforio y gwnaethoch gais amdanynt ar gyfer mis January 2022 to April" +
      " 2022.Mae dau reswm posibl am hyn:<br/><ol><li>Dim ond ar gyfer y cyfnodau lle y gwnaethoch" +
      " fewnforio nwyddau y mae datganiadau’n cael eu creu. Gwiriwch eich bod wedi mewnforio nwyddau" +
      " yn ystod y dyddiadau y gwnaethoch gais amdanynt.</li><br/><li>Ni ellir defnyddio’r Gwasanaeth" +
      " Datganiadau Tollau (CDS) i wneud cais am dystysgrifau TAW mewnforio ar gyfer datganiadau a" +
      " wnaed gan ddefnyddio system y Tollau ar gyfer Trin Nwyddau a Gaiff eu Mewnforio a’u Hallforio" +
      " (CHIEF).Gwiriwch os cafodd eich datganiadau eu gwneud drwy ddefnyddio CHIEF a chysylltwch â" +
      " <a class=\"govuk-link\" href=\"mailto:cbc-c79requests@hmrc.gov.uk\">cbc-c79requests@hmrc.gov.uk</a>" +
      " i wneud cais am ddatganiadau CHIEF.</li></ol><br/>Oddi wrth y Gwasanaeth Datganiadau Tollau"

    val TestSecurityBodyCy: String = "Annwyl Apples & Pears Ltd<br/><br/>Ni chafwyd hyd i’r" +
      " hysbysiad o ddatganiadau addasu y gwnaethoch gais amdanynt ar gyfer misMarch 2021 to" +
      " May 2021.Mae dau reswm posibl am hyn:<br/><ol><li>Dim ond ar gyfer y cyfnodau lle y" +
      " gwnaethoch fewnforio nwyddau y mae datganiadau’n cael eu creu.Gwiriwch eich bod wedi" +
      " mewnforio nwyddau yn ystod y dyddiadau y gwnaethoch gais amdanynt.</li><br/><li>Ni" +
      " ellir defnyddio’r Gwasanaeth Datganiadau Tollau (CDS) i wneud cais am hysbysiad o" +
      " ddatganiadau addasu ar gyfer datganiadau a wnaed gan ddefnyddio system y Tollau ar" +
      " gyfer Trin Nwyddau a Gaiff eu Mewnforio a’u Hallforio (CHIEF).</li></ol><br/>Oddi" +
      " wrth y Gwasanaeth Datganiadau Tollau"

    val TestPostponedVATBodyCy: String = "Annwyl Apples & Pears Ltd<br/><br/>Ni chafwyd hyd" +
      " i’r datganiadau TAW mewnforio ohiriedig y gwnaethoch gais amdanynt ar gyfer mis" +
      "February 2022 to March 2022Mae dau reswm posibl am hyn:<br/><ol><li>Dim" +
      " ond ar gyfer y cyfnodau lle y gwnaethoch fewnforio nwyddau y mae datganiadau" +
      " ’n cael eu creu. Gwiriwch eich bod wedi mewnforio nwyddau yn ystod y dyddiadau" +
      " y gwnaethoch gais amdanynt.</li><br/>Ni ellir defnyddio’r Gwasanaeth Datganiadau" +
      " Tollau (CDS) i wneud cais am dystysgrifau TAW mewnforio gohiriedig ar gyfer" +
      " datganiadau a wnaed gan ddefnyddio system y Tollau ar gyfer Trin Nwyddau a" +
      " Gaiff eu Mewnforio a’u Hallforio (CHIEF).Gwiriwch os cafodd eich datganiadau" +
      " eu gwneud drwy ddefnyddio CHIEF a chysylltwch â <a class=\"govuk-link\" href=\"mailto:pvaenquiries@hmrc.gov.uk\">" +
      "pvaenquiries@hmrc.gov.uk</a> i wneud cais am ddatganiadau CHIEF</li></ol><br/>Oddi wrth y Gwasanaeth Datganiadau Tollau"

    val encodedDutyDeferementBodyCy: String = "QW5ud3lsIEFwcGxlcyAmIFBlYXJzIEx0ZCA8YnIvPjxici8+TmkgY2hhZnd5ZCBoe" +
      "WQgaeKAmXIgZGF0Z2FuaWFkYXUgZ29oaXJpbyB0b2xsYXUgeSBnd25hZXRob2NoIGdhaXMgYW1kYW55bnQgYXIgZ3lmZXIgbWlzIFNlcH" +
      "RlbWJlciAyMDIyIHRvIE9jdG9iZXIgMjAyMi5NYWUgZGF1IHJlc3dtIHBvc2libCBhbSBoeW46PGJyLz48b2w+PGxpPkRpbSBvbmQgYXI" +
      "gZ3lmZXIgeSBjeWZub2RhdSBsbGUgeSBnd25hZXRob2NoIGZld25mb3JpbyBud3lkZGF1IHkgbWFlIGRhdGdhbmlhZGF14oCZbiBjYWVs" +
      "IGV1IGNyZXUuIEd3aXJpd2NoIGVpY2ggYm9kIHdlZGkgbWV3bmZvcmlvIG53eWRkYXUgeW4geXN0b2QgeSBkeWRkaWFkYXUgeSBnd25hZ" +
      "XRob2NoIGdhaXMgYW1kYW55bnQuPC9saT48YnIvPjxsaT5OaSBlbGxpciBkZWZueWRkaW/igJlyIEd3YXNhbmFldGggRGF0Z2FuaWFkYX" +
      "UgVG9sbGF1IChDRFMpIGkgd25ldWQgY2FpcyBhbSBkeXN0eXNncmlmYXUgVEFXIG1ld25mb3JpbyBhciBneWZlciBkYXRnYW5pYWRhdSB" +
      "hIHduYWVkIGdhbiBkZGVmbnlkZGlvIHN5c3RlbSB5IFRvbGxhdSBhciBneWZlciBUcmluIE53eWRkYXUgYSBHYWlmZiBldSBNZXduZm9y" +
      "aW8gYeKAmXUgSGFsbGZvcmlvIChDSElFRikuIEdhbGx3Y2ggZGRlZm55ZGRpb+KAmXIgZ3dhc2FuYWV0aCA8YSBjbGFzcz0iZ292dWstb" +
      "GluayIgaHJlZj0iaHR0cHM6Ly9zZWN1cmUuaG1jZS5nb3YudWsvZWNvbS9sb2dpbi9pbmRleC5odG1sIj4gRGF0Z2FuaWFkYXUgRWxlY3" +
      "Ryb25pZyBpIE9oaXJpbyBUb2xsYXUgKERERVMpPC9hPmkgZ2FlbCBkYXRnYW5pYWRhdSBnb2hpcmlvIHRvbGxhdSBhciBneWZlciBkYXR" +
      "nYW5pYWRhdSBhIHduYWVkIGdhbiBkZGVmbnlkZGlv4oCZciBnd2FzYW5hZXRoIENISUVGLjwvbGk+PC9vbD48YnIvPk9kZGkgd3J0aCB5" +
      "IEd3YXNhbmFldGggRGF0Z2FuaWFkYXUgVG9sbGF1"

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
