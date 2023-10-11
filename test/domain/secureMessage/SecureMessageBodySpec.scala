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
import utils.{SpecBase, Utils}

class SecureMessageBodySpec extends SpecBase {

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
      DutyDefermentBody("Apples & Pears Ltd") mustBe TestDutyDefermentBody
    }

    "display C79CertificateBody correctly" in new Setup {
      C79CertificateBody("Apples & Pears Ltd") mustBe TestC79CertificateBody
    }

    "display SecurityBody correctly" in new Setup {
      SecurityBody("Apples & Pears Ltd") mustBe TestSecurityBody
    }

    "display PostponedVATBody correctly" in new Setup {
      PostponedVATBody("Apples & Pears Ltd") mustBe TestPostponedVATBody
    }

    "should encode correctly" in new Setup {
      val res = Utils.encodeToUTF8Charsets(TestDutyDefermentBody)
      res mustBe encodedDutyDeferementBody
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

  trait Setup {

    val TestBody = Body("eori")
    val TestRef = ExternalReference("id", "source")
    val TestTax = TaxIdentifier("name", "value")
    val TestRecip = Recipient("regime", TestTax, "Company Name", "test@test.com")
    val TestTags = Tags("NotificationType")
    val TestContent = Content("en", "accountType", "body")

    val TestDutyDefermentTemplate = "customs_financials_requested_duty_deferment_not_found"
    val TestC79CertificateTemplate = "customs_financials_requested_c79_certificate_not_found"
    val TestSecurityTemplate = "customs_financials_requested_postponed_import_vat_statements_not_found"
    val TestPostponedVATTemplate = "customs_financials_requested_notification_adjustment_statements_not_found"

    val TestSubjectDutyDef = "Requested duty deferment statements"
    val TestSubjectCert = "Requested import VAT certificates (C79)"
    val TestSubjectSecurity: String = "Requested notification of adjustment statements"
    val TestSubjectImport = "Requested postponed import VAT statements"

    val TestDutyDefermentBody: String = "Dear Apples & Pears Ltd<br/><br/>" +
      "The duty deferment statements you requested for September 2022 to October 2022 were not found." +
      "<br/><br/>There are 2 possible reasons for this:<br/><ol><li>" +
      "Statements are only created for the periods in which you imported goods." +
      " Check that you imported goods during the dates you requested.</li><br/>" +
      "<li>Import VAT certificates for declarations made using Customs Handling " +
      "of Import and Export Freight (CHIEF) cannot be requested using the Customs " +
      "Declaration Service. You can get duty deferment statements for declarations" +
      " made using CHIEF from Duty Deferment Electronic Statements (DDES).<br/>" +
      "</li></ol>From the Customs Declaration Service"

    val TestC79CertificateBody: String = "Dear Apples & Pears Ltd<br/><br/>" +
      "The import VAT certificates you requested for January 2022 to April 2022 were not found." +
      "<br/><br/>There are 2 possible reasons for this:<br/><ol><li>Statements are only created " +
      "for the periods in which you imported goods. Check that you imported goods during" +
      " the dates you requested.</li><br/><li>Import VAT certificates for declarations made" +
      " using Customs Handling of Import and Export Freight (CHIEF) cannot be requested using" +
      " the Customs Declaration Service. Check if your declarations were made using CHIEF and" +
      " contact cbc-c79requests@hmrc.gov.uk to request CHIEF statements.<br/></li></ol>" +
      "From the Customs Declaration Service"

    val TestSecurityBody: String = "Dear Apples & Pears Ltd<br/><br/>" +
      "The notification of adjustment statements you requested for March 2021 to May 2021 were not found." +
      "<br/><br/>There are 2 possible reasons for this:<br/><ol><li>Statements are only created for the " +
      "periods in which you imported goods. Check that you imported goods during the dates you requested." +
      "</li><br/><li>Notification of adjustment statements for declarations made using Customs Handling " +
      "of Import and Export Freight (CHIEF) cannot be requested using the Customs Declaration Service." +
      " (Insert guidance on how to get CHIEF NOA statements).<br/></li></ol>From the Customs Declaration Service"

    val TestPostponedVATBody: String = "Dear Apples & Pears Ltd<br/><br/>" +
      "The postponed import VAT statements you requested for February 2022 to March 2022 were not found." +
      "<br/><br/>There are 2 possible reasons for this:<br/><ol><li>Statements are only created for the " +
      "periods in which you imported goods. Check that you imported goods during the dates you requested." +
      "</li><br/><li>Postponed import VAT statements for declarations made using Customs Handling of " +
      "Import and Export Freight (CHIEF) cannot be requested using the Customs Declaration Service. " +
      "Check if your declarations were made using CHIEF and contact pvaenquiries@hmrc.gov.uk to " +
      "request CHIEF statements.<br/></li></ol>From the Customs Declaration Service"

    val encodedDutyDeferementBody: String = "RGVhciBBcHBsZXMgJiBQZWFycyBMdGQ8YnIvPjxici8+" +
      "VGhlIGR1dHkgZGVmZXJtZW50IHN0YXRlbWVudHMgeW91IHJlcXVlc3RlZCBmb3IgU2VwdGVtYmVyIDIwMjI" +
      "gdG8gT2N0b2JlciAyMDIyIHdlcmUgbm90IGZvdW5kLjxici8+PGJyLz5UaGVyZSBhcmUgMiBwb3NzaWJsZS" +
      "ByZWFzb25zIGZvciB0aGlzOjxici8+PG9sPjxsaT5TdGF0ZW1lbnRzIGFyZSBvbmx5IGNyZWF0ZWQgZm9yI" +
      "HRoZSBwZXJpb2RzIGluIHdoaWNoIHlvdSBpbXBvcnRlZCBnb29kcy4gQ2hlY2sgdGhhdCB5b3UgaW1wb3J0" +
      "ZWQgZ29vZHMgZHVyaW5nIHRoZSBkYXRlcyB5b3UgcmVxdWVzdGVkLjwvbGk+PGJyLz48bGk+SW1wb3J0IFZ" +
      "BVCBjZXJ0aWZpY2F0ZXMgZm9yIGRlY2xhcmF0aW9ucyBtYWRlIHVzaW5nIEN1c3RvbXMgSGFuZGxpbmcgb2" +
      "YgSW1wb3J0IGFuZCBFeHBvcnQgRnJlaWdodCAoQ0hJRUYpIGNhbm5vdCBiZSByZXF1ZXN0ZWQgdXNpbmcgd" +
      "GhlIEN1c3RvbXMgRGVjbGFyYXRpb24gU2VydmljZS4gWW91IGNhbiBnZXQgZHV0eSBkZWZlcm1lbnQgc3Rh" +
      "dGVtZW50cyBmb3IgZGVjbGFyYXRpb25zIG1hZGUgdXNpbmcgQ0hJRUYgZnJvbSBEdXR5IERlZmVybWVudCB" +
      "FbGVjdHJvbmljIFN0YXRlbWVudHMgKERERVMpLjxici8+PC9saT48L29sPkZyb20gdGhlIEN1c3RvbXMgRG" +
      "VjbGFyYXRpb24gU2VydmljZQ=="
  }
}
