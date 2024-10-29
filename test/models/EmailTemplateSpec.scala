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

import java.time.LocalDate
import scala.collection.immutable.{HashMap, Map}

class EmailTemplateSpec extends SpecBase {

  "fromNotification" should {

    "return correct email template" when {

      "file role is DutyDefermentStatement" in new Setup {

        val expectedParams: Map[String, String] = HashMap(
          "recipientName_line1" -> companyName,
          "DefermentStatementType" -> "weekly",
          "PeriodIssueNumber" -> "4",
          "date" -> "16 Sep 2018",
          "DutyText" -> "The total Duty and VAT owed will be collected by direct debit on or after")

        val expectedDDEmailTemplate: Option[DutyDefermentStatementEmail] =
          Some(
            DutyDefermentStatementEmail(emailAddress,
              eoriNumber,
              expectedParams ++ Map("recipientName_line1" -> companyName))
          )

        EmailTemplate.fromNotification(emailAddress, ddNotification, companyName) mustBe
          expectedDDEmailTemplate
      }

      "file role is DutyDefermentStatement and its historic" in new Setup {
        val expectedEmailTemplate: Option[HistoricDutyDefermentStatementEmail] =
          Some(HistoricDutyDefermentStatementEmail(
            emailAddress,
            eoriNumber,
            Map("recipientName_line1" -> companyName))
          )

        EmailTemplate.fromNotification(emailAddress, ddNotificationHistoric, companyName) mustBe expectedEmailTemplate
      }

      "file role is C79Certificate" in new Setup {
        val expectedEmailTemplate: Option[C79CertificateEmail] =
          Some(C79CertificateEmail(emailAddress, eoriNumber, Map("recipientName_line1" -> companyName)))

        EmailTemplate.fromNotification(
          emailAddress,
          c79CertificateNotification,
          companyName) mustBe expectedEmailTemplate
      }

      "file role is C79Certificate and its historic" in new Setup {
        val expectedEmailTemplate: Option[HistoricC79CertificateEmail] =
          Some(
            HistoricC79CertificateEmail(emailAddress, eoriNumber, Map("recipientName_line1" -> companyName))
          )

        EmailTemplate.fromNotification(
          emailAddress,
          c79CertificateHistoricNotification,
          companyName) mustBe expectedEmailTemplate
      }

      "file role is SecurityStatement" in new Setup {
        val expectedEmailTemplate: Option[SecurityStatementEmail] =
          Some(
            SecurityStatementEmail(emailAddress, eoriNumber, Map("recipientName_line1" -> companyName)))

        EmailTemplate.fromNotification(
          emailAddress,
          securityStatementNotification,
          companyName
        ) mustBe expectedEmailTemplate

      }

      "file role is SecurityStatement and its historic" in new Setup {
        val expectedEmailTemplate: Option[HistoricSecurityStatementEmail] =
          Some(
            HistoricSecurityStatementEmail(
              emailAddress, eoriNumber, Map("recipientName_line1" -> companyName))
          )

        EmailTemplate.fromNotification(
          emailAddress,
          securityStatementHistoricNotification,
          companyName
        ) mustBe expectedEmailTemplate

      }

      "file role is PostponedVATStatement" in new Setup {
        val expectedEmailTemplate: Option[PostponedVatEmail] =
          Some(
            PostponedVatEmail(emailAddress, eoriNumber, Map("recipientName_line1" -> companyName)))

        EmailTemplate.fromNotification(
          emailAddress,
          pVATStatementNotification,
          companyName
        ) mustBe expectedEmailTemplate
      }

      "file role is PostponedVATStatement and its historic" in new Setup {
        val expectedEmailTemplate: Option[HistoricPostponedVATStatementEmail] =
          Some(
            HistoricPostponedVATStatementEmail(emailAddress, eoriNumber, Map("recipientName_line1" -> companyName)))

        EmailTemplate.fromNotification(
          emailAddress,
          pVATStatementHistoricNotification,
          companyName
        ) mustBe expectedEmailTemplate
      }

      "file role is StandingAuthority" in new Setup {
        val expectedEmailTemplate: Option[AuthoritiesStatementEmail] =
          Some(
            AuthoritiesStatementEmail(emailAddress, eoriNumber, Map("recipientName_line1" -> companyName)))

        EmailTemplate.fromNotification(
          emailAddress,
          standingAuthNotification,
          companyName
        ) mustBe expectedEmailTemplate
      }

      "file role is CDSCashAccount" in new Setup {
        val expectedEmailTemplate: Option[CDSCashAccountEmail] =
          Some(
            CDSCashAccountEmail(emailAddress, eoriNumber, Map("recipientName_line1" -> companyName)))

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

  trait Setup {

    val emailAddress: EmailAddress = EmailAddress("test_mail@test.com")
    val eoriNumber = "test_eori"

    val fileRoleDDStatement = "DutyDefermentStatement"
    val fileRoleC79Certificate = "C79Certificate"
    val fileRoleSecurityStatement = "SecurityStatement"
    val fileRolePostponedVATStatement = "PostponedVATStatement"
    val fileRoleStandingAuthority = "StandingAuthority"
    val fileRoleCDSCashAccount = "CDSCashAccount"
    val fileRoleUnknown = "Unknown"

    val fileNameValue = "test_file"
    val fileSizeValue = 999L

    val statementRequestIDKey = "statementRequestID"
    val statementRequestIDValue = "1abcdeff2-a2b1-abcd-abcd-0123456789"

    val companyName: String = "test_company"

    val year = 2024
    val monthOfTheYear = 2
    val dayOfMonth = 26
    val date: LocalDate = LocalDate.of(year, monthOfTheYear, dayOfMonth)

    val params: Map[String, String] = Map("PeriodStartYear" -> "2017",
      "PeriodStartMonth" -> "5",
      "PeriodEndYear" -> "2018",
      "PeriodEndMonth" -> "8",
      "PeriodIssueNumber" -> "4",
      "DefermentStatementType" -> "Weekly",
      "Something" -> "Random")

    val ddNotification: Notification = Notification(
      eori = EORI(eoriNumber),
      fileRole = FileRole(fileRoleDDStatement),
      fileName = fileNameValue,
      fileSize = fileSizeValue,
      created = Some(date),
      metadata = params)

    val ddNotificationHistoric: Notification = Notification(
      eori = EORI(eoriNumber),
      fileRole = FileRole(fileRoleDDStatement),
      fileName = fileNameValue,
      fileSize = fileSizeValue,
      created = Some(date),
      metadata = params ++ Map(statementRequestIDKey -> statementRequestIDValue))

    val ddEmailHistoricTemplate: Option[HistoricDutyDefermentStatementEmail] =
      Some(
        HistoricDutyDefermentStatementEmail(emailAddress,
          eoriNumber,
          Map("recipientName_line1" -> companyName)
        )
      )

    val c79CertificateNotification: Notification = Notification(
      eori = EORI(eoriNumber),
      fileRole = FileRole(fileRoleC79Certificate),
      fileName = fileNameValue,
      fileSize = fileSizeValue,
      created = Some(date),
      metadata = params)

    val c79CertificateHistoricNotification: Notification = Notification(
      eori = EORI(eoriNumber),
      fileRole = FileRole(fileRoleC79Certificate),
      fileName = fileNameValue,
      fileSize = fileSizeValue,
      created = Some(date),
      metadata = params ++ Map(statementRequestIDKey -> statementRequestIDValue))

    val securityStatementNotification: Notification = Notification(
      eori = EORI(eoriNumber),
      fileRole = FileRole(fileRoleSecurityStatement),
      fileName = fileNameValue,
      fileSize = fileSizeValue,
      created = Some(date),
      metadata = params)

    val securityStatementHistoricNotification: Notification = Notification(
      eori = EORI(eoriNumber),
      fileRole = FileRole(fileRoleSecurityStatement),
      fileName = fileNameValue,
      fileSize = fileSizeValue,
      created = Some(date),
      metadata = params ++ Map(statementRequestIDKey -> statementRequestIDValue))

    val pVATStatementNotification: Notification = Notification(
      eori = EORI(eoriNumber),
      fileRole = FileRole(fileRolePostponedVATStatement),
      fileName = fileNameValue,
      fileSize = fileSizeValue,
      created = Some(date),
      metadata = params)

    val pVATStatementHistoricNotification: Notification = Notification(
      eori = EORI(eoriNumber),
      fileRole = FileRole(fileRolePostponedVATStatement),
      fileName = fileNameValue,
      fileSize = fileSizeValue,
      created = Some(date),
      metadata = params ++ Map(statementRequestIDKey -> statementRequestIDValue))

    val standingAuthNotification: Notification = Notification(
      eori = EORI(eoriNumber),
      fileRole = FileRole(fileRoleStandingAuthority),
      fileName = fileNameValue,
      fileSize = fileSizeValue,
      created = Some(date),
      metadata = params)

    val cdsCashAccountNotification: Notification = Notification(
      eori = EORI(eoriNumber),
      fileRole = FileRole(fileRoleCDSCashAccount),
      fileName = fileNameValue,
      fileSize = fileSizeValue,
      created = Some(date),
      metadata = params)

    val unknownNotification: Notification = Notification(
      eori = EORI(eoriNumber),
      fileRole = FileRole(fileRoleUnknown),
      fileName = fileNameValue,
      fileSize = fileSizeValue,
      created = Some(date),
      metadata = params)
  }
}
