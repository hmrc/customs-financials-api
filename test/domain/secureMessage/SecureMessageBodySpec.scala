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
import models.AccountType
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
      val params = Params("01", "2022", "01", "2023", "Financials")
      val recip = Recipient("regime", tax, params, "test@test.com")
      recip mustBe TestRecip
    }

    "Params" in new Setup {
      val params = Params("01", "2022", "01", "2023", "Financials")
      params mustBe TestParams
    }

    "Tags" in new Setup {
      val tags = Tags("NotificationType")
      tags mustBe TestTags
    }

    "Content" in new Setup {
      val content = Content("en", AccountType("accountType"), "body")
      content mustBe TestContent
    }
  }

  "Body Text" should {
    "display DutyDeferementBody correctly" in new Setup {
      DutyDefermentBody mustBe TestDutyDefermentBody
    }

    "display C79CertificateBody correctly" in new Setup {
      C79CertificateBody mustBe TestC79CertificateBody
    }

    "display SecurityBody correctly" in new Setup {
      SecurityBody mustBe TestSecurityBody
    }

    "display PostponedVATBody correctly" in new Setup {
      PostponedVATBody mustBe TestPostponedVATBody
    }

    "should encode correctly" in new Setup {
      val res = Utils.encodeToUTF8Charsets(TestDutyDefermentBody)
      res mustBe encodedDutyDeferementBody
    }
  }

  trait Setup {

    val TestBody = Body("eori")
    val TestRef = ExternalReference("id", "source")
    val TestTax = TaxIdentifier("name", "value")
    val TestParams = Params("01", "2022", "01", "2023", "Financials")
    val TestRecip = Recipient("regime", TestTax, TestParams, "test@test.com")
    val TestTags = Tags("NotificationType")
    val TestContent = Content("en", AccountType("accountType"), "body")

    val TestDutyDefermentBody: String = "Dear Apples & Pears Ltd<br/><br/>" +
      "The duty deferment statements you requested for September 2022 to October 2022 were not found." +
      "<br/><br/>There are 2 possible reasons for this:<br/><br/>Statements are only created for the periods in" +
      " which you imported goods. Check that you imported goods during the dates you requested.<br/><br/>Import VAT " +
      "certificates for declarations made using Customs Handling of Import and Export Freight (CHIEF) cannot be" +
      " requested using the Customs Declaration Service. You can get duty deferment statements for declarations" +
      " made using CHIEF from Duty Deferment Electronic Statements (DDES).<br/><br/>From the Customs Declaration Service"

    val TestC79CertificateBody: String = "Dear Apples & Pears Ltd<br/><br/>" +
      "The import VAT certificates you requested for January 2022 to April 2022 were not found." +
      "<br/><br/>There are 2 possible reasons for this:<br/><br/>Statements are only created for the periods in" +
      " which you imported goods. Check that you imported goods during the dates you requested.<br/><br/>Import VAT " +
      "certificates for declarations made using Customs Handling of Import and Export Freight (CHIEF) cannot be " +
      "requested using the Customs Declaration Service. Check if your declarations were made using CHIEF and " +
      "contact cbc-c79requests@hmrc.gov.uk to request CHIEF statements.<br/><br/>From the Customs Declaration Service"

    val TestSecurityBody: String = "Dear Apples & Pears Ltd<br/><br/>" +
      "The notification of adjustment statements you requested for March 2021 to May 2021 were not found." +
      "<br/><br/>There are 2 possible reasons for this:<br/><br/>Statements are only created for the periods in" +
      " which you imported goods. Check that you imported goods during the dates you requested.<br/><br/>Notification " +
      "of adjustment statements for declarations made using Customs Handling of Import and Export Freight (CHIEF)" +
      " cannot be requested using the Customs Declaration Service. (Insert guidance on how to get CHIEF NOA " +
      "statements).<br/><br/>From the Customs Declaration Service"

    val TestPostponedVATBody: String = "Dear Apples & Pears Ltd<br/><br/>" +
      "The postponed import VAT statements you requested for February 2022 to March 2022 were not found." +
      "<br/><br/>There are 2 possible reasons for this:<br/><br/>Statements are only created for the periods in" +
      " which you imported goods. Check that you imported goods during the dates you requested.<br/><br/>Postponed " +
      "import VAT statements for declarations made using Customs Handling of Import and Export Freight (CHIEF) " +
      "cannot be requested using the Customs Declaration Service. Check if your declarations were made using " +
      "CHIEF and contact pvaenquiries@hmrc.gov.uk to request CHIEF statements.<br/><br/>From the Customs Declaration Service"

    val encodedDutyDeferementBody: String = "RGVhciBBcHBsZXMgJiBQZWFycyBMdGQ8YnIvPjxici8+VGhlIGR1dHkgZGVmZXJtZW50IHN0" +
      "YXRlbWVudHMgeW91IHJlcXVlc3RlZCBmb3IgU2VwdGVtYmVyIDIwMjIgdG8gT2N0b2JlciAyMDIyIHdlcmUgbm90IGZvdW5kLjxici8+PGJyLz" +
      "5UaGVyZSBhcmUgMiBwb3NzaWJsZSByZWFzb25zIGZvciB0aGlzOjxici8+PGJyLz5TdGF0ZW1lbnRzIGFyZSBvbmx5IGNyZWF0ZWQgZm9yIHRo" +
      "ZSBwZXJpb2RzIGluIHdoaWNoIHlvdSBpbXBvcnRlZCBnb29kcy4gQ2hlY2sgdGhhdCB5b3UgaW1wb3J0ZWQgZ29vZHMgZHVyaW5nIHRoZSBkYX" +
      "RlcyB5b3UgcmVxdWVzdGVkLjxici8+PGJyLz5JbXBvcnQgVkFUIGNlcnRpZmljYXRlcyBmb3IgZGVjbGFyYXRpb25zIG1hZGUgdXNpbmcgQ3Vz" +
      "dG9tcyBIYW5kbGluZyBvZiBJbXBvcnQgYW5kIEV4cG9ydCBGcmVpZ2h0IChDSElFRikgY2Fubm90IGJlIHJlcXVlc3RlZCB1c2luZyB0aGUgQ3" +
      "VzdG9tcyBEZWNsYXJhdGlvbiBTZXJ2aWNlLiBZb3UgY2FuIGdldCBkdXR5IGRlZmVybWVudCBzdGF0ZW1lbnRzIGZvciBkZWNsYXJhdGlvbnMg" +
      "bWFkZSB1c2luZyBDSElFRiBmcm9tIER1dHkgRGVmZXJtZW50IEVsZWN0cm9uaWMgU3RhdGVtZW50cyAoRERFUykuPGJyLz48YnIvPkZyb20gdG" +
      "hlIEN1c3RvbXMgRGVjbGFyYXRpb24gU2VydmljZQ=="
  }

}