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

package domain

import utils.SpecBase
import domain.SecureMessage._
import models.AccountType

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

    "Test" in new Setup {
      SecureMessage.body mustBe TestText
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

    val TestText = "Dear Apples & Pears Ltd\n\n" +
      "The notification of adjustment statements you requested for March 2021 to May 2021 were not found.\n\n" +
      "There are 2 possible reasons for this:\n\n" +
      "Statements are only created for the periods in which you imported goods. " +
      "Check that you imported goods during the dates you requested.\n" +
      "Notification of adjustment statements for declarations made using " +
      "Customs Handling of Import and Export Freight (CHIEF) cannot be requested " +
      "using the Customs Declaration Service. (Insert guidance on how to get CHIEF NOA statements).\n" +
      "From the Customs Declaration Service"
  }
}

