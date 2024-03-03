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
import models.requests.EmailRequest
import utils.Utils.emptyString

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import scala.util.Try

sealed trait EmailTemplate {
  val templateId: String
  val email: EmailAddress
  val params: Map[String, String] = Map.empty
  val eori: String

  def toEmailRequest: EmailRequest = EmailRequest(List(email), templateId, params, force = false, Some(eori), None, None)
}

object EmailTemplate {
  private val EXCISE_DUTY_DUE_DATE = 29
  private val EXCISE_DUTY_DUE_DATE_FEB = 28
  private val CUSTOMS_DUTY_AND_IMPORT_VAT_DUE_DATE = 15
  private val emailDateFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy")

  def fromNotification(emailAddress: EmailAddress,
                       notification: Notification,
                       companyName: String): Option[EmailTemplate] = {
    val isHistoricStat = notification.metadata.contains("statementRequestID")

    notification.fileRole.value match {
      case "DutyDefermentStatement" =>
        emailTemplateForDutyDefermentStatement(emailAddress, notification, companyName, isHistoricStat)

      case "C79Certificate" =>
        emailTemplateForC79Certificate(emailAddress, notification, companyName, isHistoricStat)

      case "SecurityStatement" =>
        emailTemplateForSecurityStatement(emailAddress, notification, companyName, isHistoricStat)

      case "PostponedVATStatement" =>
        emailTemplateForPVATStatement(emailAddress, notification, companyName, isHistoricStat)

      case "StandingAuthority" => emailTemplateForStandingAuthority(emailAddress, notification, companyName)
      case _ => Some(Unknown(emailAddress, notification.eori.value))
    }
  }

  private def emailTemplateForDutyDefermentStatement(emailAddress: EmailAddress,
                                                     notification: Notification,
                                                     companyName: String,
                                                     isHistoricStat: Boolean): Option[EmailTemplate] = {
    if (isHistoricStat) {
      emailTemplateForHistDDStatement(emailAddress, notification, companyName)
    } else {
      emailTemplateValueForDDStatement(emailAddress, notification, companyName)
    }
  }

  private def emailTemplateForC79Certificate(emailAddress: EmailAddress,
                                             notification: Notification,
                                             companyName: String,
                                             isHistoricStat: Boolean): Option[EmailTemplate] = {
    val params = Map("recipientName_line1" -> companyName)

    if (isHistoricStat) {
      Some(HistoricC79CertificateEmail(emailAddress, notification.eori.value, params))
    } else {
      Some(C79CertificateEmail(emailAddress, notification.eori.value, params))
    }
  }

  private def emailTemplateForSecurityStatement(emailAddress: EmailAddress,
                                                notification: Notification,
                                                companyName: String,
                                                isHistoricStat: Boolean): Option[EmailTemplate] = {
    val params = Map("recipientName_line1" -> companyName)

    if (isHistoricStat) {
      Some(HistoricSecurityStatementEmail(emailAddress, notification.eori.value, params))
    } else {
      Some(SecurityStatementEmail(emailAddress, notification.eori.value, params))
    }
  }

  private def emailTemplateForPVATStatement(emailAddress: EmailAddress,
                                            notification: Notification,
                                            companyName: String,
                                            isHistoricStat: Boolean): Option[EmailTemplate] = {
    val params = Map("recipientName_line1" -> companyName)

    if (isHistoricStat) {
      Some(HistoricPostponedVATStatementEmail(emailAddress, notification.eori.value, params))
    } else {
      Some(PostponedVatEmail(emailAddress, notification.eori.value, params))
    }
  }

  private def emailTemplateForStandingAuthority(emailAddress: EmailAddress,
                                                 notification: Notification,
                                                 companyName: String): Option[AuthoritiesStatementEmail] = {
    Some(AuthoritiesStatementEmail(
      emailAddress,
      notification.eori.value,
      Map("recipientName_line1" -> companyName))
    )
  }

  private def emailTemplateValueForDDStatement(emailAddress: EmailAddress,
                                               notification: Notification,
                                               companyName: String): Option[DutyDefermentStatementEmail] = {
    createDutyDefermentEmailRequestParams(notification.metadata).map {
      params =>
        DutyDefermentStatementEmail(emailAddress, notification.eori.value,
          params ++ Map("recipientName_line1" -> companyName))
    }
  }

  private def emailTemplateForHistDDStatement(emailAddress: EmailAddress,
                                              notification: Notification,
                                              companyName: String): Option[HistoricDutyDefermentStatementEmail] = {
    Some(HistoricDutyDefermentStatementEmail(emailAddress,
      notification.eori.value,
      Map("recipientName_line1" -> companyName))
    )
  }

  private def createDutyDefermentEmailRequestParams(metadata: Map[String, String]): Option[Map[String, String]] = {
    val statementType = metadata.getOrElse("DefermentStatementType", emptyString)

    for {
      periodEndMonth <- metadata.get("PeriodEndMonth")
      periodEndYear <- metadata.get("PeriodEndYear")
      dutyText = statementType match {
        case "Excise" => "The total excise owed will be collected by direct debit on or before"
        case _ => "The total Duty and VAT owed will be collected by direct debit on or after"
      }
    } yield {
      List(
        Some("date" -> createDutyDefermentDueDate(statementType, periodEndMonth.toInt, periodEndYear.toInt)),
        metadata.get("DefermentStatementType").map(
          defermentStatementType => "DefermentStatementType" -> defermentStatementType.toLowerCase
        ),
        metadata.get("PeriodIssueNumber").map(periodIssueNumber => "PeriodIssueNumber" -> periodIssueNumber),
        Some("DutyText" -> dutyText)).flatten.toMap
    }
  }

  def createDutyDefermentDueDate(defermentStatementType: String,
                                 periodEndMonth: Int,
                                 periodEndYear: Int): String = {
    val oneMonth = 1

    val dueDate = defermentStatementType match {
      case "Excise" => Try {
        LocalDate.of(periodEndYear, periodEndMonth, EXCISE_DUTY_DUE_DATE)
      }.getOrElse {
        LocalDate.of(periodEndYear, periodEndMonth, EXCISE_DUTY_DUE_DATE_FEB)
      }

      case _ => LocalDate.of(periodEndYear, periodEndMonth, CUSTOMS_DUTY_AND_IMPORT_VAT_DUE_DATE).plusMonths(oneMonth)
    }

    dueDate.format(emailDateFormatter)
  }
}

case class HistoricDutyDefermentStatementEmail(email: EmailAddress,
                                               eori: String,
                                               override val params: Map[String, String]) extends EmailTemplate {
  override val templateId: String = "customs_financials_requested_duty_deferment_statement"
}

case class HistoricC79CertificateEmail(email: EmailAddress,
                                       eori: String,
                                       override val params: Map[String, String]) extends EmailTemplate {
  override val templateId: String = "customs_financials_historic_c79_certificate"
}

case class HistoricPostponedVATStatementEmail(email: EmailAddress,
                                              eori: String,
                                              override val params: Map[String, String]) extends EmailTemplate {
  override val templateId: String = "customs_financials_requested_postponed_vat_notification"
}

case class HistoricSecurityStatementEmail(email: EmailAddress,
                                          eori: String,
                                          override val params: Map[String, String]) extends EmailTemplate {
  override val templateId: String = "customs_financials_requested_import_adjustment"
}

case class DutyDefermentStatementEmail(email: EmailAddress,
                                       eori: String,
                                       override val params: Map[String, String]) extends EmailTemplate {
  override val templateId: String = "customs_financials_new_statement_notification"
}

case class C79CertificateEmail(email: EmailAddress,
                               eori: String,
                               override val params: Map[String, String]) extends EmailTemplate {
  override val templateId: String = "customs_financials_new_c79_certificate"
}

case class SecurityStatementEmail(email: EmailAddress,
                                  eori: String,
                                  override val params: Map[String, String]) extends EmailTemplate {
  override val templateId: String = "customs_financials_new_import_adjustment"
}

case class PostponedVatEmail(email: EmailAddress,
                             eori: String,
                             override val params: Map[String, String]) extends EmailTemplate {
  override val templateId: String = "customs_financials_new_postponed_vat_notification"
}

case class Unknown(email: EmailAddress, eori: String) extends EmailTemplate {
  override val templateId: String = "customs_financials_new_statement_notification"
  override val params: Map[String, String] = Map("Name" -> "test")
}

case class AuthoritiesStatementEmail(email: EmailAddress,
                                     eori: String,
                                     override val params: Map[String, String]) extends EmailTemplate {
  override val templateId: String = "customs_financials_requested_for_standing_authorities"
}
