/*
 * Copyright 2021 HM Revenue & Customs
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

import models.requests.EmailRequest
import models.{EORI, EmailAddress, EmailTemplate, FileRole}
import utils.SpecBase

class EmailRequestSpec extends SpecBase {

  val someFileSize: Long = 1024L
  val someEORI: EORI = EORI("someEORI")
  val someFilename: String = "whatever.csv"

  "EmailTemplate.fromNotification" should {
    "construct an email request" when {

      "given a week 4 duty deferment statement notification" in {
        val dd4Notification = Notification(
          someEORI, FileRole("DutyDefermentStatement"), someFilename, someFileSize, None, Map(
            "PeriodStartYear" -> "2017",
            "PeriodStartMonth" -> "5",
            "PeriodEndYear" -> "2018",
            "PeriodEndMonth" -> "8",
            "PeriodIssueNumber" -> "4",
            "DefermentStatementType" -> "Weekly",
            "Something" -> "Random"
          )
        )

        val expectedParams = Map(
          "DefermentStatementType" -> "weekly",
          "PeriodIssueNumber" -> "4",
          "date" -> "15 Sep 2018",
          "DutyText" -> "The total Duty and VAT owed will be collected by direct debit on or after"
        )
        val expected = Some(EmailRequest(List(EmailAddress("test@test.com")), "customs_financials_new_statement_notification", expectedParams, force = false, Some("someEORI"), None, None))

        val actual = EmailTemplate.fromNotification(EmailAddress("test@test.com"), dd4Notification).map(_.toEmailRequest)

        actual mustBe expected
      }

      "given a supplementary duty deferment statement notification" in {
        val supplementaryDDNotification = Notification(
          someEORI, FileRole("DutyDefermentStatement"), someFilename, someFileSize, None, Map(
            "PeriodStartYear" -> "2017",
            "PeriodStartMonth" -> "5",
            "PeriodEndYear" -> "2018",
            "PeriodEndMonth" -> "8",
            "PeriodIssueNumber" -> "4",
            "DefermentStatementType" -> "Supplementary",
            "Something" -> "Random"
          )
        )

        val expectedParams = Map(
          "DefermentStatementType" -> "supplementary",
          "PeriodIssueNumber" -> "4",
          "date" -> "15 Sep 2018",
          "DutyText" -> "The total Duty and VAT owed will be collected by direct debit on or after"
        )
        val expected = Some(EmailRequest(List(EmailAddress("test@test.com")), "customs_financials_new_statement_notification", expectedParams, force = false, Some("someEORI"), None, None))

        val actual = EmailTemplate.fromNotification(EmailAddress("test@test.com"), supplementaryDDNotification).map(_.toEmailRequest)

        actual mustBe expected
      }

      "given an excise duty deferment statement notification" in {
        val exciseNotification = Notification(
          someEORI, FileRole("DutyDefermentStatement"), someFilename, someFileSize, None, Map(
            "PeriodStartYear" -> "2017",
            "PeriodStartMonth" -> "5",
            "PeriodEndYear" -> "2018",
            "PeriodEndMonth" -> "8",
            "PeriodIssueNumber" -> "4",
            "DefermentStatementType" -> "Excise",
            "Something" -> "Random"
          )
        )

        val expectedParams = Map(
          "DefermentStatementType" -> "excise",
          "PeriodIssueNumber" -> "4",
          "date" -> "29 Aug 2018",
          "DutyText" -> "The total excise owed will be collected by direct debit on or before"
        )
        val expected = Some(EmailRequest(List(EmailAddress("test@test.com")), "customs_financials_new_statement_notification", expectedParams, force = false, Some("someEORI"), None, None))

        val actual = EmailTemplate.fromNotification(EmailAddress("test@test.com"), exciseNotification).map(_.toEmailRequest)

        actual mustBe expected
      }

      "given a requested duty deferment statement notification" in {
        val ddRequestedNotification = Notification(
          someEORI, FileRole("DutyDefermentStatement"), someFilename, someFileSize, None, Map(
            "FileRole" -> "DutyDefermentStatement",
            "statementRequestID" -> "1abcdeff2-a2b1-abcd-abcd-0123456789",
            "Something" -> "Random"
          )
        )

        val expected = Some(EmailRequest(List(EmailAddress("foo@bar.com")), "customs_financials_requested_duty_deferment_statement", Map.empty, force = false, Some("someEORI"), None, None))

        val actual = EmailTemplate.fromNotification(EmailAddress("foo@bar.com"), ddRequestedNotification).map(_.toEmailRequest)

        actual mustBe expected
      }

      "given a new C79 certificate notification" in {
        val c79Notification = Notification(someEORI, FileRole("C79Certificate"), someFilename, someFileSize, None, Map("Something" -> "Random"))

        val expected = Some(EmailRequest(List(EmailAddress("test@test.com")), "customs_financials_new_c79_certificate", Map.empty, force = false, Some("someEORI"), None, None))

        val actual = EmailTemplate.fromNotification(EmailAddress("test@test.com"), c79Notification).map(_.toEmailRequest)

        actual mustBe expected
      }

      "given a requested C79 certificate notification" in {
        val requestedC79Notification = Notification(
          someEORI, FileRole("C79Certificate"), someFilename, someFileSize, None, Map(
            "statementRequestID" -> "someID",
            "Something" -> "Random"
          )
        )

        val expected = Some(EmailRequest(List(EmailAddress("test@test.com")), "customs_financials_historic_c79_certificate", Map.empty, force = false, Some("someEORI"), None, None))

        val actual = EmailTemplate.fromNotification(EmailAddress("test@test.com"), requestedC79Notification).map(_.toEmailRequest)

        actual mustBe expected
      }

      "given a new security statement notification" in {
        val securityStatementNotification = Notification(someEORI, FileRole("SecurityStatement"), someFilename, someFileSize, None, Map("Something" -> "Random"))

        val expected = Some(EmailRequest(List(EmailAddress("test@test.com")), "customs_financials_new_import_adjustment", Map.empty, force = false, Some("someEORI"), None, None))

        val actual = EmailTemplate.fromNotification(EmailAddress("test@test.com"), securityStatementNotification).map(_.toEmailRequest)

        actual mustBe expected
      }

      "given a requested import adjustment notification" in {
        val requestedSecurityStatementNotification = Notification(
          someEORI, FileRole("SecurityStatement"), someFilename, someFileSize, None, Map(
            "statementRequestID" -> "someID",
            "Something" -> "Random"
          )
        )

        val expected = Some(EmailRequest(List(EmailAddress("test@test.com")), "customs_financials_requested_import_adjustment", Map.empty, force = false, Some("someEORI"), None, None))

        val actual = EmailTemplate.fromNotification(EmailAddress("test@test.com"), requestedSecurityStatementNotification).map(_.toEmailRequest)

        actual mustBe expected
      }

      "given a new postponed VAT statement notification" in {
        val pvatStatementNotification = Notification(someEORI, FileRole("PostponedVATStatement"), someFilename, someFileSize, None, Map("Something" -> "Random"))

        val expected = Some(EmailRequest(List(EmailAddress("test@test.com")), "customs_financials_new_postponed_vat_notification", Map.empty, force = false, Some("someEORI"), None, None))

        val actual = EmailTemplate.fromNotification(EmailAddress("test@test.com"), pvatStatementNotification).map(_.toEmailRequest)

        actual mustBe expected
      }

      "given a requested postponed VAT statement notification" in {
        val requestedPVatNotification = Notification(
          someEORI, FileRole("PostponedVATStatement"), someFilename, someFileSize, None, Map(
            "statementRequestID" -> "someID",
            "Something" -> "Random"
          )
        )

        val expected = Some(EmailRequest(List(EmailAddress("test@test.com")), "customs_financials_requested_postponed_vat_notification", Map.empty, force = false, Some("someEORI"), None, None))

        val actual = EmailTemplate.fromNotification(EmailAddress("test@test.com"), requestedPVatNotification).map(_.toEmailRequest)

        actual mustBe expected
      }


    }
  }

  "createDutyDefermentDueDate" should {
    "calculate the correct payment due date" when {
      "statement type is Excise, due date is 29th of the current month" in {
        val periodEndYear = 2020
        val periodEndMonth = 2
        val defermentStatementType = "Excise"

        val expectedDate = "29 Feb 2020"
        val actualDate = EmailTemplate.createDutyDefermentDueDate(defermentStatementType, periodEndMonth, periodEndYear)
        actualDate mustBe expectedDate
      }

      "statement type is Excise, due date is 28th of the current month in February of a non-leap-year" in {
        val periodEndYear = 2018
        val periodEndMonth = 2
        val defermentStatementType = "Excise"

        val expectedDate = "28 Feb 2018"
        val actualDate= EmailTemplate.createDutyDefermentDueDate(defermentStatementType, periodEndMonth, periodEndYear)
        actualDate mustBe expectedDate
      }

      "statement type is not Excise, due date is 15th of the following month" in {
        val periodEndYear = 2018
        val periodEndMonth = 9
        val defermentStatementType = "anything that isn't Excise..."

        val expectedDate = "15 Oct 2018"
        val actualDate = EmailTemplate.createDutyDefermentDueDate(defermentStatementType, periodEndMonth, periodEndYear)
        actualDate mustBe expectedDate
      }

      "statement type is not Excise, due date is 15th of January next year, when current month is December" in {
        val periodEndYear = 2018
        val periodEndMonth = 12
        val defermentStatementType = "anything that isn't Excise..."

        val expectedDate = "15 Jan 2019"
        val actualDate = EmailTemplate.createDutyDefermentDueDate(defermentStatementType, periodEndMonth, periodEndYear)
        actualDate mustBe expectedDate
      }
    }
  }

}
