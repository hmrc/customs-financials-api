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

package models

import domain.Notification
import utils.SpecBase
import utils.TestData.{MONTH_2, MONTH_7, YEAR_2019, YEAR_2020}

import java.time.LocalDate
import scala.collection.immutable.{HashMap, Map}

class EmailTemplateSpec extends SpecBase {

  "fromNotification" should {

    "return correct email template" when {

      "file role is DutyDefermentStatement and statement type is Weekly" in new Setup {

        val expectedParams: Map[String, String] = HashMap(
          "recipientName_line1"    -> companyName,
          "DefermentStatementType" -> "weekly",
          "PeriodIssueNumber"      -> "4",
          "date"                   -> "16 Aug 2018",
          "DutyText"               -> dutyText01
        )

        val expectedDDEmailTemplate: Option[DutyDefermentStatementEmail] =
          Some(
            DutyDefermentStatementEmail(
              emailAddress,
              eoriNumber,
              expectedParams ++ Map("recipientName_line1" -> companyName)
            )
          )

        EmailTemplate.fromNotification(emailAddress, ddNotification(weeklyParams), companyName) mustBe
          expectedDDEmailTemplate
      }

      "file role is DutyDefermentStatement and statement type is DD1920" in new Setup {

        val expectedParams: Map[String, String] = HashMap(
          "recipientName_line1"    -> companyName,
          "DefermentStatementType" -> "dd1920",
          "PeriodIssueNumber"      -> "4",
          "date"                   -> "25 Jul 2018",
          "DutyText"               -> dutyText02
        )

        val expectedDDEmailTemplate: Option[DutyDefermentStatementEmail] =
          Some(
            DutyDefermentStatementEmail(
              emailAddress,
              eoriNumber,
              expectedParams ++ Map("recipientName_line1" -> companyName)
            )
          )

        EmailTemplate.fromNotification(emailAddress, ddNotification(dd1920Params), companyName) mustBe
          expectedDDEmailTemplate
      }

      "file role is DutyDefermentStatement and statement type is DD1720" in new Setup {

        val expectedParams: Map[String, String] = HashMap(
          "recipientName_line1"    -> companyName,
          "DefermentStatementType" -> "dd1720",
          "PeriodIssueNumber"      -> "4",
          "date"                   -> "15 Jul 2018",
          "DutyText"               -> dutyText01
        )

        val expectedDDEmailTemplate: Option[DutyDefermentStatementEmail] =
          Some(
            DutyDefermentStatementEmail(
              emailAddress,
              eoriNumber,
              expectedParams ++ Map("recipientName_line1" -> companyName)
            )
          )

        EmailTemplate.fromNotification(emailAddress, ddNotification(dd1720Params), companyName) mustBe
          expectedDDEmailTemplate
      }

      "file role is DutyDefermentStatement and statement type is Excise" in new Setup {

        val expectedParams: Map[String, String] = HashMap(
          "recipientName_line1"    -> companyName,
          "DefermentStatementType" -> "excise",
          "PeriodIssueNumber"      -> "4",
          "date"                   -> "29 Jul 2018",
          "DutyText"               -> dutyText02
        )

        val expectedDDEmailTemplate: Option[DutyDefermentStatementEmail] =
          Some(
            DutyDefermentStatementEmail(
              emailAddress,
              eoriNumber,
              expectedParams ++ Map("recipientName_line1" -> companyName)
            )
          )

        EmailTemplate.fromNotification(emailAddress, ddNotification(exciseParams), companyName) mustBe
          expectedDDEmailTemplate
      }

      "file role is DutyDefermentStatement and statement type is Supplementary" in new Setup {

        val expectedParams: Map[String, String] = HashMap(
          "recipientName_line1"    -> companyName,
          "DefermentStatementType" -> "supplementary",
          "PeriodIssueNumber"      -> "4",
          "date"                   -> "16 Aug 2018",
          "DutyText"               -> dutyText01
        )

        val expectedDDEmailTemplate: Option[DutyDefermentStatementEmail] =
          Some(
            DutyDefermentStatementEmail(
              emailAddress,
              eoriNumber,
              expectedParams ++ Map("recipientName_line1" -> companyName)
            )
          )

        EmailTemplate.fromNotification(emailAddress, ddNotification(supplementaryParams), companyName) mustBe
          expectedDDEmailTemplate
      }

      "file role is DutyDefermentStatement and its historic" in new Setup {
        val expectedEmailTemplate: Option[HistoricDutyDefermentStatementEmail] =
          Some(HistoricDutyDefermentStatementEmail(emailAddress, eoriNumber, Map("recipientName_line1" -> companyName)))

        EmailTemplate.fromNotification(emailAddress, ddNotificationHistoric, companyName) mustBe expectedEmailTemplate
      }

      "file role is C79Certificate" in new Setup {
        val expectedEmailTemplate: Option[C79CertificateEmail] =
          Some(C79CertificateEmail(emailAddress, eoriNumber, Map("recipientName_line1" -> companyName)))

        EmailTemplate.fromNotification(
          emailAddress,
          c79CertificateNotification,
          companyName
        ) mustBe expectedEmailTemplate
      }

      "file role is C79Certificate and its historic" in new Setup {
        val expectedEmailTemplate: Option[HistoricC79CertificateEmail] =
          Some(
            HistoricC79CertificateEmail(emailAddress, eoriNumber, Map("recipientName_line1" -> companyName))
          )

        EmailTemplate.fromNotification(
          emailAddress,
          c79CertificateHistoricNotification,
          companyName
        ) mustBe expectedEmailTemplate
      }

      "file role is SecurityStatement" in new Setup {
        val expectedEmailTemplate: Option[SecurityStatementEmail] =
          Some(SecurityStatementEmail(emailAddress, eoriNumber, Map("recipientName_line1" -> companyName)))

        EmailTemplate.fromNotification(
          emailAddress,
          securityStatementNotification,
          companyName
        ) mustBe expectedEmailTemplate

      }

      "file role is SecurityStatement and its historic" in new Setup {
        val expectedEmailTemplate: Option[HistoricSecurityStatementEmail] =
          Some(
            HistoricSecurityStatementEmail(emailAddress, eoriNumber, Map("recipientName_line1" -> companyName))
          )

        EmailTemplate.fromNotification(
          emailAddress,
          securityStatementHistoricNotification,
          companyName
        ) mustBe expectedEmailTemplate

      }

      "file role is PostponedVATStatement" in new Setup {
        val expectedEmailTemplate: Option[PostponedVatEmail] =
          Some(PostponedVatEmail(emailAddress, eoriNumber, Map("recipientName_line1" -> companyName)))

        EmailTemplate.fromNotification(
          emailAddress,
          pVATStatementNotification,
          companyName
        ) mustBe expectedEmailTemplate
      }

      "file role is PostponedVATStatement and its historic" in new Setup {
        val expectedEmailTemplate: Option[HistoricPostponedVATStatementEmail] =
          Some(HistoricPostponedVATStatementEmail(emailAddress, eoriNumber, Map("recipientName_line1" -> companyName)))

        EmailTemplate.fromNotification(
          emailAddress,
          pVATStatementHistoricNotification,
          companyName
        ) mustBe expectedEmailTemplate
      }

      "file role is StandingAuthority" in new Setup {
        val expectedEmailTemplate: Option[AuthoritiesStatementEmail] =
          Some(AuthoritiesStatementEmail(emailAddress, eoriNumber, Map("recipientName_line1" -> companyName)))

        EmailTemplate.fromNotification(
          emailAddress,
          standingAuthNotification,
          companyName
        ) mustBe expectedEmailTemplate
      }

      "file role is CDSCashAccount" in new Setup {
        val expectedEmailTemplate: Option[CDSCashAccountEmail] =
          Some(CDSCashAccountEmail(emailAddress, eoriNumber, Map("recipientName_line1" -> companyName)))

        EmailTemplate.fromNotification(
          emailAddress,
          cdsCashAccountNotification,
          companyName
        ) mustBe expectedEmailTemplate
      }

      "file role is Unknown" in new Setup {
        val expectedEmailTemplate: Option[Unknown] = Some(Unknown(emailAddress, eoriNumber))

        EmailTemplate.fromNotification(
          emailAddress,
          unknownNotification,
          companyName
        ) mustBe expectedEmailTemplate
      }
    }
  }

  "createDutyDefermentDueDate" should {

    "return correct Due date" when {

      "defermentStatementType is Weekly" in new Setup {
        EmailTemplate.createDutyDefermentDueDate("Weekly", MONTH_7, YEAR_2019) mustBe "16 Aug 2019"
      }

      "defermentStatementType is Excise" in new Setup {
        EmailTemplate.createDutyDefermentDueDate("Excise", MONTH_2, YEAR_2019) mustBe "28 Feb 2019"
        EmailTemplate.createDutyDefermentDueDate("Excise", MONTH_2, YEAR_2020) mustBe "29 Feb 2020"
      }

      "defermentStatementType is DD1920" in new Setup {
        EmailTemplate.createDutyDefermentDueDate("DD1920", MONTH_7, YEAR_2019) mustBe "25 Jul 2019"
      }

      "defermentStatementType is DD1720" in new Setup {
        EmailTemplate.createDutyDefermentDueDate("DD1720", MONTH_7, YEAR_2019) mustBe "15 Jul 2019"
      }

      "defermentStatementType is Supplementary" in new Setup {
        EmailTemplate.createDutyDefermentDueDate("Supplementary", MONTH_7, YEAR_2019) mustBe "16 Aug 2019"
      }
    }
  }

  trait Setup {

    val emailAddress: EmailAddress = EmailAddress("test_mail@test.com")
    val eoriNumber: String         = "test_eori"

    val fileRoleDDStatement: String           = "DutyDefermentStatement"
    val fileRoleC79Certificate: String        = "C79Certificate"
    val fileRoleSecurityStatement: String     = "SecurityStatement"
    val fileRolePostponedVATStatement: String = "PostponedVATStatement"
    val fileRoleStandingAuthority: String     = "StandingAuthority"
    val fileRoleCDSCashAccount: String        = "CDSCashAccount"
    val fileRoleUnknown: String               = "Unknown"

    val fileNameValue: String = "test_file"
    val fileSizeValue: Long   = 999L

    val statementRequestIDKey: String   = "statementRequestID"
    val statementRequestIDValue: String = "1abcdeff2-a2b1-abcd-abcd-0123456789"

    val companyName: String = "test_company"
    val dutyText01: String  = "The total Duty and VAT owed will be collected by direct debit on or after"
    val dutyText02: String  = "The total excise owed will be collected by direct debit on or before"

    val year: Int           = 2024
    val monthOfTheYear: Int = 2
    val dayOfMonth: Int     = 26
    val date: LocalDate     = LocalDate.of(year, monthOfTheYear, dayOfMonth)

    private def params(sttType: String): Map[String, String] =
      Map(
        "PeriodStartYear"        -> "2017",
        "PeriodStartMonth"       -> "5",
        "PeriodEndYear"          -> "2018",
        "PeriodEndMonth"         -> "7",
        "PeriodIssueNumber"      -> "4",
        "DefermentStatementType" -> sttType,
        "Something"              -> "Random"
      )

    def ddNotification(paramType: Map[String, String]): Notification = Notification(
      eori = EORI(eoriNumber),
      fileRole = FileRole(fileRoleDDStatement),
      fileName = fileNameValue,
      fileSize = fileSizeValue,
      created = Some(date),
      metadata = paramType
    )

    val weeklyParams: Map[String, String]        = params("Weekly")
    val dd1920Params: Map[String, String]        = params("DD1920")
    val dd1720Params: Map[String, String]        = params("DD1720")
    val exciseParams: Map[String, String]        = params("Excise")
    val supplementaryParams: Map[String, String] = params("Supplementary")

    val ddNotificationHistoric: Notification = Notification(
      eori = EORI(eoriNumber),
      fileRole = FileRole(fileRoleDDStatement),
      fileName = fileNameValue,
      fileSize = fileSizeValue,
      created = Some(date),
      metadata = weeklyParams ++ Map(statementRequestIDKey -> statementRequestIDValue)
    )

    val ddEmailHistoricTemplate: Option[HistoricDutyDefermentStatementEmail] =
      Some(
        HistoricDutyDefermentStatementEmail(emailAddress, eoriNumber, Map("recipientName_line1" -> companyName))
      )

    val c79CertificateNotification: Notification = Notification(
      eori = EORI(eoriNumber),
      fileRole = FileRole(fileRoleC79Certificate),
      fileName = fileNameValue,
      fileSize = fileSizeValue,
      created = Some(date),
      metadata = weeklyParams
    )

    val c79CertificateHistoricNotification: Notification = Notification(
      eori = EORI(eoriNumber),
      fileRole = FileRole(fileRoleC79Certificate),
      fileName = fileNameValue,
      fileSize = fileSizeValue,
      created = Some(date),
      metadata = weeklyParams ++ Map(statementRequestIDKey -> statementRequestIDValue)
    )

    val securityStatementNotification: Notification = Notification(
      eori = EORI(eoriNumber),
      fileRole = FileRole(fileRoleSecurityStatement),
      fileName = fileNameValue,
      fileSize = fileSizeValue,
      created = Some(date),
      metadata = weeklyParams
    )

    val securityStatementHistoricNotification: Notification = Notification(
      eori = EORI(eoriNumber),
      fileRole = FileRole(fileRoleSecurityStatement),
      fileName = fileNameValue,
      fileSize = fileSizeValue,
      created = Some(date),
      metadata = weeklyParams ++ Map(statementRequestIDKey -> statementRequestIDValue)
    )

    val pVATStatementNotification: Notification = Notification(
      eori = EORI(eoriNumber),
      fileRole = FileRole(fileRolePostponedVATStatement),
      fileName = fileNameValue,
      fileSize = fileSizeValue,
      created = Some(date),
      metadata = weeklyParams
    )

    val pVATStatementHistoricNotification: Notification = Notification(
      eori = EORI(eoriNumber),
      fileRole = FileRole(fileRolePostponedVATStatement),
      fileName = fileNameValue,
      fileSize = fileSizeValue,
      created = Some(date),
      metadata = weeklyParams ++ Map(statementRequestIDKey -> statementRequestIDValue)
    )

    val standingAuthNotification: Notification = Notification(
      eori = EORI(eoriNumber),
      fileRole = FileRole(fileRoleStandingAuthority),
      fileName = fileNameValue,
      fileSize = fileSizeValue,
      created = Some(date),
      metadata = weeklyParams
    )

    val cdsCashAccountNotification: Notification = Notification(
      eori = EORI(eoriNumber),
      fileRole = FileRole(fileRoleCDSCashAccount),
      fileName = fileNameValue,
      fileSize = fileSizeValue,
      created = Some(date),
      metadata = weeklyParams
    )

    val unknownNotification: Notification = Notification(
      eori = EORI(eoriNumber),
      fileRole = FileRole(fileRoleUnknown),
      fileName = fileNameValue,
      fileSize = fileSizeValue,
      created = Some(date),
      metadata = weeklyParams
    )
  }
}
