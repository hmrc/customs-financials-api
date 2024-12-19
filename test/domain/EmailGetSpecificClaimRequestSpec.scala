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

import models.requests.EmailRequest
import models.{EORI, EmailAddress, EmailTemplate, FileRole}
import utils.SpecBase
import utils.TestData.*

import scala.collection.immutable.HashMap

class EmailGetSpecificClaimRequestSpec extends SpecBase {

  val someEORI: EORI = EORI(EORI_VALUE_1)

  "EmailTemplate.fromNotification" should {
    "construct an email request" when {

      "given a week 4 duty deferment statement notification" in {
        val dd4Notification = Notification(
          someEORI,
          FileRole("DutyDefermentStatement"),
          CSV_FILE_NAME,
          FILE_SIZE_1024L,
          None,
          Map(
            "PeriodStartYear"        -> "2017",
            "PeriodStartMonth"       -> "5",
            "PeriodEndYear"          -> "2018",
            "PeriodEndMonth"         -> "8",
            "PeriodIssueNumber"      -> "4",
            "DefermentStatementType" -> "Weekly",
            "Something"              -> "Random"
          )
        )

        val expectedParams = HashMap(
          "recipientName_line1"    -> TEST_COMPANY,
          "DefermentStatementType" -> "weekly",
          "PeriodIssueNumber"      -> "4",
          "date"                   -> "16 Sep 2018",
          "DutyText"               -> "The total Duty and VAT owed will be collected by direct debit on or after"
        )

        val expected =
          Some(
            EmailRequest(
              List(EmailAddress(TEST_EMAIL)),
              "customs_financials_new_statement_notification",
              expectedParams,
              force = false,
              Some(EORI_VALUE_1),
              None,
              None
            )
          )

        val actual =
          EmailTemplate.fromNotification(EmailAddress(TEST_EMAIL), dd4Notification, TEST_COMPANY).map(_.toEmailRequest)

        actual mustBe expected
      }

      "given a supplementary duty deferment statement notification" in {
        val supplementaryDDNotification = Notification(
          someEORI,
          FileRole("DutyDefermentStatement"),
          CSV_FILE_NAME,
          FILE_SIZE_1024L,
          None,
          Map(
            "PeriodStartYear"        -> "2017",
            "PeriodStartMonth"       -> "5",
            "PeriodEndYear"          -> "2018",
            "PeriodEndMonth"         -> "8",
            "PeriodIssueNumber"      -> "4",
            "DefermentStatementType" -> "Supplementary",
            "Something"              -> "Random"
          )
        )

        val expectedParams = HashMap(
          "recipientName_line1"    -> TEST_COMPANY,
          "DefermentStatementType" -> "supplementary",
          "PeriodIssueNumber"      -> "4",
          "date"                   -> "16 Sep 2018",
          "DutyText"               -> "The total Duty and VAT owed will be collected by direct debit on or after"
        )

        val expected =
          Some(
            EmailRequest(
              List(EmailAddress(TEST_EMAIL)),
              "customs_financials_new_statement_notification",
              expectedParams,
              force = false,
              Some(EORI_VALUE_1),
              None,
              None
            )
          )

        val actual =
          EmailTemplate
            .fromNotification(EmailAddress(TEST_EMAIL), supplementaryDDNotification, TEST_COMPANY)
            .map(_.toEmailRequest)

        actual mustBe expected
      }

      "given an excise duty deferment statement notification" in {
        val exciseNotification = Notification(
          someEORI,
          FileRole("DutyDefermentStatement"),
          CSV_FILE_NAME,
          FILE_SIZE_1024L,
          None,
          Map(
            "PeriodStartYear"        -> "2017",
            "PeriodStartMonth"       -> "5",
            "PeriodEndYear"          -> "2018",
            "PeriodEndMonth"         -> "8",
            "PeriodIssueNumber"      -> "4",
            "DefermentStatementType" -> "Excise",
            "Something"              -> "Random"
          )
        )

        val expectedParams = HashMap(
          "recipientName_line1"    -> TEST_COMPANY,
          "DefermentStatementType" -> "excise",
          "PeriodIssueNumber"      -> "4",
          "date"                   -> "29 Aug 2018",
          "DutyText"               -> "The total excise owed will be collected by direct debit on or before"
        )

        val expected =
          Some(
            EmailRequest(
              List(EmailAddress(TEST_EMAIL)),
              "customs_financials_new_statement_notification",
              expectedParams,
              force = false,
              Some(EORI_VALUE_1),
              None,
              None
            )
          )

        val actual =
          EmailTemplate
            .fromNotification(EmailAddress(TEST_EMAIL), exciseNotification, TEST_COMPANY)
            .map(_.toEmailRequest)

        actual mustBe expected
      }

      "given a requested duty deferment statement notification" in {
        val ddRequestedNotification = Notification(
          someEORI,
          FileRole("DutyDefermentStatement"),
          CSV_FILE_NAME,
          FILE_SIZE_1024L,
          None,
          Map(
            "FileRole"           -> "DutyDefermentStatement",
            "statementRequestID" -> "1abcdeff2-a2b1-abcd-abcd-0123456789",
            "Something"          -> "Random"
          )
        )

        val expected =
          Some(
            EmailRequest(
              List(EmailAddress("foo@bar.com")),
              "customs_financials_requested_duty_deferment_statement",
              Map("recipientName_line1" -> TEST_COMPANY),
              force = false,
              Some(EORI_VALUE_1),
              None,
              None
            )
          )

        val actual =
          EmailTemplate
            .fromNotification(EmailAddress("foo@bar.com"), ddRequestedNotification, TEST_COMPANY)
            .map(_.toEmailRequest)

        actual mustBe expected
      }

      "given a new C79 certificate notification" in {
        val c79Notification =
          Notification(
            someEORI,
            FileRole("C79Certificate"),
            CSV_FILE_NAME,
            FILE_SIZE_1024L,
            None,
            Map("Something" -> "Random")
          )

        val expected =
          Some(
            EmailRequest(
              List(EmailAddress(TEST_EMAIL)),
              "customs_financials_new_c79_certificate",
              Map("recipientName_line1" -> TEST_COMPANY),
              force = false,
              Some(EORI_VALUE_1),
              None,
              None
            )
          )

        val actual =
          EmailTemplate.fromNotification(EmailAddress(TEST_EMAIL), c79Notification, TEST_COMPANY).map(_.toEmailRequest)

        actual mustBe expected
      }

      "given a requested C79 certificate notification" in {
        val requestedC79Notification = Notification(
          someEORI,
          FileRole("C79Certificate"),
          CSV_FILE_NAME,
          FILE_SIZE_1024L,
          None,
          Map(
            "statementRequestID" -> "someID",
            "Something"          -> "Random"
          )
        )

        val expected =
          Some(
            EmailRequest(
              List(EmailAddress(TEST_EMAIL)),
              "customs_financials_historic_c79_certificate",
              Map("recipientName_line1" -> TEST_COMPANY),
              force = false,
              Some(EORI_VALUE_1),
              None,
              None
            )
          )

        val actual =
          EmailTemplate
            .fromNotification(EmailAddress(TEST_EMAIL), requestedC79Notification, TEST_COMPANY)
            .map(_.toEmailRequest)

        actual mustBe expected
      }

      "given a new security statement notification" in {
        val securityStatementNotification =
          Notification(
            someEORI,
            FileRole("SecurityStatement"),
            CSV_FILE_NAME,
            FILE_SIZE_1024L,
            None,
            Map("Something" -> "Random")
          )

        val expected =
          Some(
            EmailRequest(
              List(EmailAddress(TEST_EMAIL)),
              "customs_financials_new_import_adjustment",
              Map("recipientName_line1" -> TEST_COMPANY),
              force = false,
              Some(EORI_VALUE_1),
              None,
              None
            )
          )

        val actual =
          EmailTemplate
            .fromNotification(EmailAddress(TEST_EMAIL), securityStatementNotification, TEST_COMPANY)
            .map(_.toEmailRequest)

        actual mustBe expected
      }

      "given a requested import adjustment notification" in {
        val requestedSecurityStatementNotification = Notification(
          someEORI,
          FileRole("SecurityStatement"),
          CSV_FILE_NAME,
          FILE_SIZE_1024L,
          None,
          Map(
            "statementRequestID" -> "someID",
            "Something"          -> "Random"
          )
        )

        val expected =
          Some(
            EmailRequest(
              List(EmailAddress(TEST_EMAIL)),
              "customs_financials_requested_import_adjustment",
              Map("recipientName_line1" -> TEST_COMPANY),
              force = false,
              Some(EORI_VALUE_1),
              None,
              None
            )
          )

        val actual =
          EmailTemplate
            .fromNotification(EmailAddress(TEST_EMAIL), requestedSecurityStatementNotification, TEST_COMPANY)
            .map(_.toEmailRequest)

        actual mustBe expected
      }

      "given a new postponed VAT statement notification" in {
        val pvatStatementNotification =
          Notification(
            someEORI,
            FileRole("PostponedVATStatement"),
            CSV_FILE_NAME,
            FILE_SIZE_1024L,
            None,
            Map("Something" -> "Random")
          )

        val expected =
          Some(
            EmailRequest(
              List(EmailAddress(TEST_EMAIL)),
              "customs_financials_new_postponed_vat_notification",
              Map("recipientName_line1" -> TEST_COMPANY),
              force = false,
              Some(EORI_VALUE_1),
              None,
              None
            )
          )

        val actual =
          EmailTemplate
            .fromNotification(EmailAddress(TEST_EMAIL), pvatStatementNotification, TEST_COMPANY)
            .map(_.toEmailRequest)

        actual mustBe expected
      }

      "given a requested postponed VAT statement notification" in {
        val requestedPVatNotification = Notification(
          someEORI,
          FileRole("PostponedVATStatement"),
          CSV_FILE_NAME,
          FILE_SIZE_1024L,
          None,
          Map(
            "statementRequestID" -> "someID",
            "Something"          -> "Random"
          )
        )

        val expected =
          Some(
            EmailRequest(
              List(EmailAddress(TEST_EMAIL)),
              "customs_financials_requested_postponed_vat_notification",
              Map("recipientName_line1" -> TEST_COMPANY),
              force = false,
              Some(EORI_VALUE_1),
              None,
              None
            )
          )

        val actual =
          EmailTemplate
            .fromNotification(EmailAddress(TEST_EMAIL), requestedPVatNotification, TEST_COMPANY)
            .map(_.toEmailRequest)

        actual mustBe expected
      }

      "given a unknown notification" in {
        val unknownNotification = Notification(
          someEORI,
          FileRole("Unknown"),
          CSV_FILE_NAME,
          FILE_SIZE_1024L,
          None,
          Map(
            "statementRequestID" -> "someID",
            "Something"          -> "Random"
          )
        )

        val expected = Some(
          EmailRequest(
            List(EmailAddress(TEST_EMAIL)),
            "customs_financials_new_statement_notification",
            Map("Name" -> "test"),
            force = false,
            Some(EORI_VALUE_1),
            None,
            None
          )
        )

        val actual = EmailTemplate
          .fromNotification(EmailAddress(TEST_EMAIL), unknownNotification, TEST_COMPANY)
          .map(_.toEmailRequest)

        actual mustBe expected
      }
    }
  }

  "createDutyDefermentDueDate" should {

    "calculate the correct payment due date" when {
      "statement type is Excise, due date is 29th of the current month" in {
        val periodEndYear          = 2020
        val periodEndMonth         = 2
        val defermentStatementType = "Excise"

        val expectedDate = "29 Feb 2020"
        val actualDate   = EmailTemplate.createDutyDefermentDueDate(defermentStatementType, periodEndMonth, periodEndYear)

        actualDate mustBe expectedDate
      }

      "statement type is Excise, due date is 28th of the current month in February of a non-leap-year" in {
        val periodEndYear          = 2018
        val periodEndMonth         = 2
        val defermentStatementType = "Excise"

        val expectedDate = "28 Feb 2018"
        val actualDate   = EmailTemplate.createDutyDefermentDueDate(defermentStatementType, periodEndMonth, periodEndYear)

        actualDate mustBe expectedDate
      }

      "statement type is not Excise, due date is 16th of the following month" in {
        val periodEndYear          = 2018
        val periodEndMonth         = 9
        val defermentStatementType = "anything that isn't Excise..."

        val expectedDate = "16 Oct 2018"
        val actualDate   = EmailTemplate.createDutyDefermentDueDate(defermentStatementType, periodEndMonth, periodEndYear)

        actualDate mustBe expectedDate
      }

      "statement type is not Excise, due date is 16th of January next year, when current month is December" in {
        val periodEndYear          = 2018
        val periodEndMonth         = 12
        val defermentStatementType = "anything that isn't Excise..."

        val expectedDate = "16 Jan 2019"
        val actualDate   = EmailTemplate.createDutyDefermentDueDate(defermentStatementType, periodEndMonth, periodEndYear)

        actualDate mustBe expectedDate
      }
    }
  }

}
