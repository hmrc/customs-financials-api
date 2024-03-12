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

package controllers.metadata

import connectors.{DataStoreConnector, EmailThrottlerConnector}
import controllers.CustomAuthConnector
import models.SearchResultStatus.{inProcess, yes}
import models._
import models.requests.EmailRequest
import org.mockito.ArgumentMatchers.{eq => is}
import org.mockito.{ArgumentMatchers, Mockito}
import play.api.http.Status.BAD_REQUEST
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.{JsArray, JsValue, Json}
import play.api.mvc.AnyContentAsJson
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.api.{Application, inject}
import services.NotificationCache
import services.cache.{HistoricDocumentRequestSearchCache, HistoricDocumentRequestSearchCacheService}
import utils.SpecBase
import utils.TestData.{EORI_VALUE, TEST_COMPANY, TEST_EMAIL}
import utils.Utils.emptyString

import java.util.UUID
import scala.concurrent.Future

class MetadataControllerSpec extends SpecBase {

  "addNotifications" should {

    "return 200" in new Setup {
      when(mockDataStore.getVerifiedEmail(any)(any)).thenReturn(Future.successful(Some(emailAddress)))
      when(mockDataStore.getCompanyName(any)(any)).thenReturn(Future.successful(Some(TEST_COMPANY)))
      when(mockEmailThrottler.sendEmail(any)(any)).thenReturn(Future.successful(true))
      when(mockNotificationCache.putNotifications(any)).thenReturn(Future.successful(()))

      running(app) {
        val result = route(app, addRequest).value

        status(result) mustBe OK
        contentAsJson(result) mustBe Json.obj("Status" -> "Ok")
      }
    }

    "return 200 when companyName retrieval throws exception" in new Setup {
      when(mockDataStore.getVerifiedEmail(any)(any)).thenReturn(Future.successful(Some(emailAddress)))
      when(mockDataStore.getCompanyName(any)(any)).thenReturn(Future.failed(new RuntimeException(errorMsg)))
      when(mockEmailThrottler.sendEmail(any)(any)).thenReturn(Future.successful(true))
      when(mockNotificationCache.putNotifications(any)).thenReturn(Future.successful(()))

      running(app) {
        val result = route(app, addRequest).value

        status(result) mustBe OK
        contentAsJson(result) mustBe Json.obj("Status" -> "Ok")
      }
    }

    "return 200 when VerifiedEmail is returned as None " in new Setup {
      when(mockDataStore.getVerifiedEmail(any)(any)).thenReturn(Future.successful(None))
      when(mockNotificationCache.putNotifications(any)).thenReturn(Future.successful(()))

      running(app) {
        val result = route(app, addRequest).value
        status(result) mustBe OK
        contentAsJson(result) mustBe Json.obj("Status" -> "Ok")
      }
    }

    "return 200 when VerifiedEmail retrieval throws exception" in new Setup {
      when(mockDataStore.getVerifiedEmail(any)(any)).thenReturn(Future.failed(new RuntimeException(errorMsg)))
      when(mockNotificationCache.putNotifications(any)).thenReturn(Future.successful(()))

      running(app) {
        val result = route(app, addRequest).value
        status(result) mustBe OK
        contentAsJson(result) mustBe Json.obj("Status" -> "Ok")
      }
    }

    "send email when 4th week duty deferment statement is available" in new Setup {
      val dd4emailRequest: JsValue = Json.parse(
        """
          |[
          |    {
          |       "eori":"testEORI-12345",
          |       "fileName": "vat-2018-05.pdf",
          |       "fileSize": 75251,
          |       "metadata": [
          |           {"metadata": "PeriodStartYear", "value": "2017"},
          |           {"metadata": "PeriodStartMonth", "value": "5"},
          |           {"metadata": "PeriodStartDay", "value": "5"},
          |           {"metadata": "PeriodEndYear", "value": "2018"},
          |           {"metadata": "PeriodEndMonth", "value": "8"},
          |           {"metadata": "PeriodEndDay", "value": "5"},
          |           {"metadata": "PeriodIssueNumber", "value": "4"},
          |           {"metadata": "FileType", "value": "PDF"},
          |           {"metadata": "FileRole", "value": "DutyDefermentStatement"},
          |           {"metadata": "DefermentStatementType", "value": "Weekly"},
          |           {"metadata": "DutyOverLimit", "value": "Y"},
          |           {"metadata": "DutyPaymentType", "value": "DirectDebit"}
          |       ]
          |    }
          |]
        """.stripMargin)

      when(mockEmailThrottler.sendEmail(any)(any)).thenReturn(Future.successful(true))
      when(mockDataStore.getVerifiedEmail(any)(any)).thenReturn(Future.successful(None))
      when(mockDataStore.getVerifiedEmail(ArgumentMatchers.eq(testEori))(any))
        .thenReturn(Future.successful(Some(emailAddress)))
      when(mockDataStore.getCompanyName(any)(any)).thenReturn(Future.successful(Some(TEST_COMPANY)))
      when(mockNotificationCache.putNotifications(any)).thenReturn(Future.successful(()))

      running(app) {
        val req: FakeRequest[AnyContentAsJson] = FakeRequest(POST, "/metadata").withJsonBody(dd4emailRequest)
        val result = route(app, req).value

        status(result) mustBe OK
        contentAsJson(result) mustBe Json.obj("Status" -> "Ok")

        val params: Map[String, String] = Map("DefermentStatementType" -> "weekly",
            "PeriodIssueNumber" -> "4",
            "date" -> "15 Sep 2018",
            "DutyText" -> "The total Duty and VAT owed will be collected by direct debit on or after",
            "recipientName_line1" -> TEST_COMPANY)

        verify(mockEmailThrottler).sendEmail(is(emailRequest(params = params)))(any)
      }
    }

    "do not send email when companyName is not available " in new Setup {
      val dd4emailRequest: JsValue = Json.parse(
        """
          |[
          |    {
          |       "eori":"testEORI-12345",
          |       "fileName": "vat-2018-05.pdf",
          |       "fileSize": 75251,
          |       "metadata": [
          |            {"metadata": "PeriodStartYear", "value": "2017"},
          |            {"metadata": "PeriodStartMonth", "value": "5"},
          |            {"metadata": "PeriodStartDay", "value": "5"},
          |            {"metadata": "PeriodEndYear", "value": "2018"},
          |            {"metadata": "PeriodEndMonth", "value": "8"},
          |            {"metadata": "PeriodEndDay", "value": "5"},
          |            {"metadata": "PeriodIssueNumber", "value": "4"},
          |            {"metadata": "FileType", "value": "PDF"},
          |            {"metadata": "FileRole", "value": "DutyDefermentStatement"},
          |            {"metadata": "DefermentStatementType", "value": "Weekly"},
          |            {"metadata": "DutyOverLimit", "value": "Y"},
          |            {"metadata": "DutyPaymentType", "value": "DirectDebit"}
          |        ]
          |    }
          |]
        """.stripMargin)

      when(mockDataStore.getVerifiedEmail(any)(any)).thenReturn(Future.successful(None))
      when(mockDataStore.getVerifiedEmail(ArgumentMatchers.eq(testEori))(any))
        .thenReturn(Future.successful(Some(emailAddress)))
      when(mockDataStore.getCompanyName(any)(any)).thenReturn(Future.successful(None))
      when(mockNotificationCache.putNotifications(any)).thenReturn(Future.successful(()))

      running(app) {
        val req: FakeRequest[AnyContentAsJson] = FakeRequest(POST, "/metadata").withJsonBody(dd4emailRequest)
        val result = route(app, req).value

        status(result) mustBe OK
        contentAsJson(result) mustBe Json.obj("Status" -> "Ok")
      }
    }

    "send email when requested duty deferment statement is available" in new Setup {
      val newFileNotificationFromSDES: JsValue = Json.parse(
        """
          |[
          |    {
          |       "eori":"testEORI-12345",
          |       "fileName": "whatever.pdf",
          |       "fileSize": 999,
          |       "metadata": [
          |            {"metadata": "FileRole", "value": "DutyDefermentStatement"},
          |            {"metadata": "Other", "value": "Stuff"},
          |            {"metadata": "statementRequestID", "value": "1abcdeff2-a2b1-abcd-abcd-0123456789"}
          |        ]
          |    }
          |]
        """.stripMargin)

      when(mockEmailThrottler.sendEmail(any)(any)).thenReturn(Future.successful(true))
      when(mockDataStore.getVerifiedEmail(any)(any)).thenReturn(Future.successful(None))
      when(mockDataStore.getVerifiedEmail(is(testEori))(any)).thenReturn(Future.successful(Some(emailAddress)))
      when(mockDataStore.getCompanyName(any)(any)).thenReturn(Future.successful(Some(TEST_COMPANY)))
      when(mockNotificationCache.putNotifications(any)).thenReturn(Future.successful(()))

      when(mockHistDocReqCacheService.retrieveHistDocRequestSearchDocForStatementReqId(any))
        .thenReturn(Future.successful(Option(histDocRequestSearch)))

      when(mockHistDocReqCacheService.processSDESNotificationForStatReqId(any, any))
        .thenReturn(Future.successful(Option(histDocRequestSearch)))

      running(app) {
        val req: FakeRequest[AnyContentAsJson] =
          FakeRequest(POST, controllers.metadata.routes.MetadataController.addNotifications().url)
            .withJsonBody(newFileNotificationFromSDES)

        val result = route(app, req).value
        status(result) mustBe OK

        verify(mockEmailThrottler)
          .sendEmail(is(emailRequest(templateId = "customs_financials_requested_duty_deferment_statement")))(any)
      }
    }

    "send email when requested Standing Authority is available" in new Setup {
      val newFileNotificationFromSDES: JsValue = Json.parse(
        """
          |[
          |    {
          |       "eori":"testEORI-23456",
          |       "fileName": "whatever.pdf",
          |       "fileSize": 999,
          |       "metadata": [
          |            {"metadata": "FileRole", "value": "StandingAuthority"},
          |            {"metadata": "Other", "value": "Stuff"},
          |            {"metadata": "statementRequestID", "value": "1abcdeff2-a2b1-abcd-abcd-0123456789"}
          |        ]
          |    }
          |]
        """.stripMargin)

      when(mockEmailThrottler.sendEmail(any)(any)).thenReturn(Future.successful(true))
      when(mockDataStore.getVerifiedEmail(any)(any)).thenReturn(Future.successful(None))
      when(mockDataStore.getVerifiedEmail(is(testEori))(any)).thenReturn(Future.successful(Some(emailAddress)))
      when(mockDataStore.getCompanyName(any)(any)).thenReturn(Future.successful(Some(TEST_COMPANY)))
      when(mockNotificationCache.putNotifications(any)).thenReturn(Future.successful(()))

      when(mockHistDocReqCacheService.retrieveHistDocRequestSearchDocForStatementReqId(any))
        .thenReturn(Future.successful(Option(histDocRequestSearch)))

      when(mockHistDocReqCacheService.processSDESNotificationForStatReqId(any, any)).
        thenReturn(Future.successful(Option(histDocRequestSearch)))

      running(app) {
        val req: FakeRequest[AnyContentAsJson] =
          FakeRequest(POST, controllers.metadata.routes.MetadataController.addNotifications().url)
            .withJsonBody(newFileNotificationFromSDES)

        val result = route(app, req).value
        status(result) mustBe OK

        verify(mockEmailThrottler)
          .sendEmail(is(emailRequest(templateId = "customs_financials_requested_for_standing_authorities")))(any)
      }
    }

    "send email when supplementary duty deferment statement is available" in new Setup {
      val dd4emailRequest: JsValue = Json.parse(
        """
          |[
          |    {
          |       "eori":"testEORI-12345",
          |       "fileName": "vat-2018-05.pdf",
          |       "fileSize": 75251,
          |       "metadata": [
          |            {"metadata": "PeriodStartYear", "value": "2017"},
          |            {"metadata": "PeriodStartMonth", "value": "5"},
          |            {"metadata": "PeriodStartDay", "value": "5"},
          |            {"metadata": "PeriodEndYear", "value": "2018"},
          |            {"metadata": "PeriodEndMonth", "value": "8"},
          |            {"metadata": "PeriodEndDay", "value": "5"},
          |            {"metadata": "PeriodIssueNumber", "value": "1"},
          |            {"metadata": "FileType", "value": "PDF"},
          |            {"metadata": "FileRole", "value": "DutyDefermentStatement"},
          |            {"metadata": "DefermentStatementType", "value": "Supplementary"},
          |            {"metadata": "DutyOverLimit", "value": "Y"},
          |            {"metadata": "DutyPaymentType", "value": "DirectDebit"}
          |        ]
          |    }
          |]
        """.stripMargin)

      when(mockEmailThrottler.sendEmail(any)(any)).thenReturn(Future.successful(true))
      when(mockDataStore.getVerifiedEmail(any)(any)).thenReturn(Future.successful(None))
      when(mockDataStore.getVerifiedEmail(is(testEori))(any)).thenReturn(Future.successful(Some(emailAddress)))
      when(mockDataStore.getCompanyName(any)(any)).thenReturn(Future.successful(Some(TEST_COMPANY)))
      when(mockNotificationCache.putNotifications(any)).thenReturn(Future.successful(()))

      running(app) {
        val req: FakeRequest[AnyContentAsJson] = FakeRequest(POST, "/metadata").withJsonBody(dd4emailRequest)
        val result = route(app, req).value

        status(result) mustBe OK

        val params: Map[String, String] = Map("DefermentStatementType" -> "supplementary",
            "date" -> "15 Sep 2018",
            "PeriodIssueNumber" -> "1",
            "DutyText" -> "The total Duty and VAT owed will be collected by direct debit on or after",
            "recipientName_line1" -> TEST_COMPANY)

        verify(mockEmailThrottler).sendEmail(is(emailRequest(params = params)))(any)
      }
    }

    "send email when excise duty deferment statement is available" in new Setup {
      val dd4emailRequest: JsValue = Json.parse(
        """
          |[
          |    {
          |       "eori":"testEORI-12345",
          |       "fileName": "vat-2018-05.pdf",
          |       "fileSize": 75251,
          |       "metadata": [
          |            {"metadata": "PeriodStartYear", "value": "2017"},
          |            {"metadata": "PeriodStartMonth", "value": "5"},
          |            {"metadata": "PeriodStartDay", "value": "5"},
          |            {"metadata": "PeriodEndYear", "value": "2018"},
          |            {"metadata": "PeriodEndMonth", "value": "8"},
          |            {"metadata": "PeriodEndDay", "value": "5"},
          |            {"metadata": "PeriodIssueNumber", "value": "1"},
          |            {"metadata": "FileType", "value": "PDF"},
          |            {"metadata": "FileRole", "value": "DutyDefermentStatement"},
          |            {"metadata": "DefermentStatementType", "value": "Excise"},
          |            {"metadata": "DutyOverLimit", "value": "Y"},
          |            {"metadata": "DutyPaymentType", "value": "DirectDebit"}
          |        ]
          |    }
          |]
        """.stripMargin)

      when(mockEmailThrottler.sendEmail(any)(any)).thenReturn(Future.successful(true))
      when(mockDataStore.getVerifiedEmail(any)(any)).thenReturn(Future.successful(None))
      when(mockDataStore.getVerifiedEmail(is(testEori))(any)).thenReturn(Future.successful(Some(emailAddress)))
      when(mockDataStore.getCompanyName(any)(any)).thenReturn(Future.successful(Some(TEST_COMPANY)))
      when(mockNotificationCache.putNotifications(any)).thenReturn(Future.successful(()))

      running(app) {
        val req: FakeRequest[AnyContentAsJson] = FakeRequest(POST, "/metadata").withJsonBody(dd4emailRequest)
        val result = route(app, req).value

        status(result) mustBe OK

        val params: Map[String, String] = Map("DefermentStatementType" -> "excise",
            "date" -> "29 Aug 2018",
            "PeriodIssueNumber" -> "1",
            "DutyText" -> "The total excise owed will be collected by direct debit on or before",
            "recipientName_line1" -> TEST_COMPANY)

        verify(mockEmailThrottler).sendEmail(is(emailRequest(params = params)))(any)
      }
    }

    "send email when C79 Certificate is available" in new Setup {
      val c79emailRequest: JsValue = Json.parse(
        """
          |[
          |    {
          |       "eori":"testEORI",
          |       "fileName": "vat-2018-05.pdf",
          |       "fileSize": 75251,
          |       "metadata": [
          |            {"metadata": "PeriodStartYear", "value": "2017"},
          |            {"metadata": "PeriodStartMonth", "value": "5"},
          |            {"metadata": "PeriodStartDay", "value": "5"},
          |            {"metadata": "PeriodEndYear", "value": "2018"},
          |            {"metadata": "PeriodEndMonth", "value": "8"},
          |            {"metadata": "PeriodEndDay", "value": "5"},
          |            {"metadata": "FileType", "value": "PDF"},
          |            {"metadata": "FileRole", "value": "C79Certificate"}
          |        ]
          |    }
          |]
        """.stripMargin)

      when(mockEmailThrottler.sendEmail(any)(any)).thenReturn(Future.successful(true))
      when(mockDataStore.getVerifiedEmail(any)(any)).thenReturn(Future.successful(Some(emailAddress)))
      when(mockDataStore.getCompanyName(any)(any)).thenReturn(Future.successful(Some(TEST_COMPANY)))
      when(mockNotificationCache.putNotifications(any)).thenReturn(Future.successful(()))

      running(app) {
        val req: FakeRequest[AnyContentAsJson] = FakeRequest(POST, "/metadata").withJsonBody(c79emailRequest)
        val result = route(app, req).value

        status(result) mustBe OK
        verify(mockEmailThrottler)
          .sendEmail(is(emailRequest(templateId = "customs_financials_new_c79_certificate")))(any)
      }
    }

    "send email when historic C79 Certificate is available" in new Setup {
      val historicC79CertificateNotificationRequest: JsValue = Json.parse(
        s"""
           |[
           |    {
           |        "eori":"${eori.value}",
           |        "fileName": "vat-2018-05.pdf",
           |        "fileSize": 75251,
           |        "metadata": [
           |            {"metadata": "PeriodStartYear", "value": "2017"},
           |            {"metadata": "PeriodStartMonth", "value": "5"},
           |            {"metadata": "PeriodStartDay", "value": "5"},
           |            {"metadata": "PeriodEndYear", "value": "2018"},
           |            {"metadata": "PeriodEndMonth", "value": "8"},
           |            {"metadata": "PeriodEndDay", "value": "5"},
           |            {"metadata": "FileType", "value": "PDF"},
           |            {"metadata": "FileRole", "value": "C79Certificate"},
           |            {"metadata": "statementRequestID", "value": "1abcdefg2-a2b1-abcd-abcd-0123456789"}
           |        ]
           |    }
           |]
        """.stripMargin)

      when(mockEmailThrottler.sendEmail(any)(any)).thenReturn(Future.successful(true))
      when(mockDataStore.getVerifiedEmail(is(eori))(any)).thenReturn(Future.successful(Some(emailAddress)))
      when(mockDataStore.getCompanyName(any)(any)).thenReturn(Future.successful(Some(TEST_COMPANY)))
      when(mockNotificationCache.putNotifications(any)).thenReturn(Future.successful(()))

      when(mockHistDocReqCacheService.retrieveHistDocRequestSearchDocForStatementReqId(any))
        .thenReturn(Future.successful(Option(histDocRequestSearch)))

      when(mockHistDocReqCacheService.processSDESNotificationForStatReqId(any, any))
        .thenReturn(Future.successful(Option(histDocRequestSearch)))

      running(app) {
        val req: FakeRequest[AnyContentAsJson] =
          FakeRequest(POST, "/metadata").withJsonBody(historicC79CertificateNotificationRequest)

        val result = route(app, req).value
        status(result) mustBe OK
        verify(mockEmailThrottler)
          .sendEmail(is(emailRequest(templateId = "customs_financials_historic_c79_certificate",
            enrolment = testEnrolment)))(any)
      }
    }

    "send email when Security statement is available" in new Setup {
      val ssemailRequest: JsValue = Json.parse(
        """
          |[
          |    {
          |       "eori":"testEORI",
          |       "fileName": "vat-2018-05.pdf",
          |       "fileSize": 75251,
          |       "metadata": [
          |            {"metadata": "PeriodStartYear", "value": "2017"},
          |            {"metadata": "PeriodStartMonth", "value": "5"},
          |            {"metadata": "PeriodStartDay", "value": "5"},
          |            {"metadata": "PeriodEndYear", "value": "2018"},
          |            {"metadata": "PeriodEndMonth", "value": "8"},
          |            {"metadata": "PeriodEndDay", "value": "5"},
          |            {"metadata": "FileType", "value": "PDF"},
          |            {"metadata": "FileRole", "value": "SecurityStatement"}
          |        ]
          |    }
          |]
        """.stripMargin)

      when(mockEmailThrottler.sendEmail(any)(any)).thenReturn(Future.successful(true))
      when(mockDataStore.getVerifiedEmail(any)(any)).thenReturn(Future.successful(Some(emailAddress)))
      when(mockDataStore.getCompanyName(any)(any)).thenReturn(Future.successful(Some(TEST_COMPANY)))
      when(mockNotificationCache.putNotifications(any)).thenReturn(Future.successful(()))

      running(app) {
        val req: FakeRequest[AnyContentAsJson] = FakeRequest(POST, "/metadata").withJsonBody(ssemailRequest)
        val result = route(app, req).value

        status(result) mustBe OK
        verify(mockEmailThrottler)
          .sendEmail(is(emailRequest(templateId = "customs_financials_new_import_adjustment")))(any)
      }
    }

    "send email when requested Security statement is available" in new Setup {
      val requestedSecurityStatementNotificationRequest: JsValue = Json.parse(
        s"""
           |[
           |    {
           |        "eori":"${eori.value}",
           |        "fileName": "vat-2018-05.pdf",
           |        "fileSize": 75251,
           |        "metadata": [
           |            {"metadata": "PeriodStartYear", "value": "2017"},
           |            {"metadata": "PeriodStartMonth", "value": "5"},
           |            {"metadata": "PeriodStartDay", "value": "5"},
           |            {"metadata": "PeriodEndYear", "value": "2018"},
           |            {"metadata": "PeriodEndMonth", "value": "8"},
           |            {"metadata": "PeriodEndDay", "value": "5"},
           |            {"metadata": "FileType", "value": "PDF"},
           |            {"metadata": "FileRole", "value": "SecurityStatement"},
           |            {"metadata": "statementRequestID", "value": "1abcdefg2-a2b1-abcd-abcd-0123456789"}
           |        ]
           |    }
           |]
        """.stripMargin)

      when(mockEmailThrottler.sendEmail(any)(any)).thenReturn(Future.successful(true))
      when(mockDataStore.getVerifiedEmail(is(eori))(any)).thenReturn(Future.successful(Some(emailAddress)))
      when(mockDataStore.getCompanyName(any)(any)).thenReturn(Future.successful(Some(TEST_COMPANY)))
      when(mockNotificationCache.putNotifications(any)).thenReturn(Future.successful(()))

      when(mockHistDocReqCacheService.retrieveHistDocRequestSearchDocForStatementReqId(any))
        .thenReturn(Future.successful(Option(histDocRequestSearch)))

      when(mockHistDocReqCacheService.processSDESNotificationForStatReqId(any, any))
        .thenReturn(Future.successful(Option(histDocRequestSearch)))

      running(app) {
        val req: FakeRequest[AnyContentAsJson] =
          FakeRequest(POST, "/metadata").withJsonBody(requestedSecurityStatementNotificationRequest)

        val result = route(app, req).value
        status(result) mustBe OK
        verify(mockEmailThrottler)
          .sendEmail(is(emailRequest(templateId = "customs_financials_requested_import_adjustment",
            enrolment = testEnrolment)))(any)
      }
    }

    "send email when PVAT statement is available" in new Setup {
      val pvatStatementRequest: JsValue = Json.parse(
        """
          |[
          |    {
          |       "eori":"testEORI",
          |       "fileName": "vat-2018-05.pdf",
          |       "fileSize": 75251,
          |       "metadata": [
          |            {"metadata": "PeriodStartYear", "value": "2017"},
          |            {"metadata": "PeriodStartMonth", "value": "5"},
          |            {"metadata": "PeriodStartDay", "value": "5"},
          |            {"metadata": "PeriodEndYear", "value": "2018"},
          |            {"metadata": "PeriodEndMonth", "value": "8"},
          |            {"metadata": "PeriodEndDay", "value": "5"},
          |            {"metadata": "FileType", "value": "PDF"},
          |            {"metadata": "FileRole", "value": "PostponedVATStatement"}
          |        ]
          |    }
          |]
        """.stripMargin)

      when(mockEmailThrottler.sendEmail(any)(any)).thenReturn(Future.successful(true))
      when(mockDataStore.getVerifiedEmail(any)(any)).thenReturn(Future.successful(Some(emailAddress)))
      when(mockDataStore.getCompanyName(any)(any)).thenReturn(Future.successful(Some(TEST_COMPANY)))
      when(mockNotificationCache.putNotifications(any)).thenReturn(Future.successful(()))

      running(app) {
        val req: FakeRequest[AnyContentAsJson] = FakeRequest(POST, "/metadata").withJsonBody(pvatStatementRequest)
        val result = route(app, req).value
        status(result) mustBe OK

        verify(mockEmailThrottler)
          .sendEmail(is(emailRequest(templateId = "customs_financials_new_postponed_vat_notification")))(any)
      }
    }

    "send email when a requested PVAT statement is available" in new Setup {
      val requestedPVATStatementNotificationRequest: JsValue = Json.parse(
        s"""
           |[
           |    {
           |        "eori":"testEORI",
           |        "fileName": "vat-2018-05.pdf",
           |        "fileSize": 75251,
           |        "metadata": [
           |            {"metadata": "PeriodStartYear", "value": "2017"},
           |            {"metadata": "PeriodStartMonth", "value": "5"},
           |            {"metadata": "PeriodStartDay", "value": "5"},
           |            {"metadata": "PeriodEndYear", "value": "2018"},
           |            {"metadata": "PeriodEndMonth", "value": "8"},
           |            {"metadata": "PeriodEndDay", "value": "5"},
           |            {"metadata": "FileType", "value": "PDF"},
           |            {"metadata": "FileRole", "value": "PostponedVATStatement"},
           |            {"metadata": "statementRequestID", "value": "1abcdefg2-a2b1-abcd-abcd-0123456789"}
           |        ]
           |    }
           |]
        """.stripMargin)

      when(mockEmailThrottler.sendEmail(any)(any)).thenReturn(Future.successful(true))
      when(mockDataStore.getVerifiedEmail(any)(any)).thenReturn(Future.successful(Some(emailAddress)))
      when(mockDataStore.getCompanyName(any)(any)).thenReturn(Future.successful(Some(TEST_COMPANY)))
      when(mockNotificationCache.putNotifications(any)).thenReturn(Future.successful(()))

      when(mockHistDocReqCacheService.retrieveHistDocRequestSearchDocForStatementReqId(any))
        .thenReturn(Future.successful(Option(histDocRequestSearch)))

      when(mockHistDocReqCacheService.processSDESNotificationForStatReqId(any, any))
        .thenReturn(Future.successful(Option(histDocRequestSearch)))

      running(app) {
        val req: FakeRequest[AnyContentAsJson] =
          FakeRequest(POST, "/metadata").withJsonBody(requestedPVATStatementNotificationRequest)

        val result = route(app, req).value
        status(result) mustBe OK
        verify(mockEmailThrottler)
          .sendEmail(is(emailRequest(templateId = "customs_financials_requested_postponed_vat_notification")))(any)
      }
    }

    "send the email only once in case of multiple notifications for same statementRequestID" in new Setup {
      val requestedPVATStatementNotificationRequest: JsValue = Json.parse(
        s"""
           |[
           |    {
           |        "eori":"testEORI",
           |        "fileName": "vat-2018-05.pdf",
           |        "fileSize": 75251,
           |        "metadata": [
           |            {"metadata": "PeriodStartYear", "value": "2017"},
           |            {"metadata": "PeriodStartMonth", "value": "5"},
           |            {"metadata": "PeriodStartDay", "value": "5"},
           |            {"metadata": "PeriodEndYear", "value": "2018"},
           |            {"metadata": "PeriodEndMonth", "value": "6"},
           |            {"metadata": "PeriodEndDay", "value": "5"},
           |            {"metadata": "FileType", "value": "PDF"},
           |            {"metadata": "FileRole", "value": "PostponedVATStatement"},
           |            {"metadata": "statementRequestID", "value": "1abcdefg2-a2b1-abcd-abcd-0123456789"}
           |        ]
           |    },
           |    {
           |        "eori":"testEORI",
           |        "fileName": "vat-2018-05.pdf",
           |        "fileSize": 75251,
           |        "metadata": [
           |            {"metadata": "PeriodStartYear", "value": "2017"},
           |            {"metadata": "PeriodStartMonth", "value": "7"},
           |            {"metadata": "PeriodStartDay", "value": "7"},
           |            {"metadata": "PeriodEndYear", "value": "2018"},
           |            {"metadata": "PeriodEndMonth", "value": "8"},
           |            {"metadata": "PeriodEndDay", "value": "5"},
           |            {"metadata": "FileType", "value": "PDF"},
           |            {"metadata": "FileRole", "value": "PostponedVATStatement"},
           |            {"metadata": "statementRequestID", "value": "1abcdefg2-a2b1-abcd-abcd-0123456789"}
           |        ]
           |    }
           |]
        """.stripMargin)

      when(mockEmailThrottler.sendEmail(any)(any)).thenReturn(Future.successful(true))
      when(mockDataStore.getVerifiedEmail(any)(any)).thenReturn(Future.successful(Some(emailAddress)))
      when(mockDataStore.getCompanyName(any)(any)).thenReturn(Future.successful(Some(TEST_COMPANY)))
      when(mockNotificationCache.putNotifications(any)).thenReturn(Future.successful(()))

      when(mockHistDocReqCacheService.retrieveHistDocRequestSearchDocForStatementReqId(any)).thenReturn(
        Future.successful(Option(histDocRequestSearch))).andThenAnswer(
        Future.successful(Option(histDocRequestSearch.copy(resultsFound = yes))))

      when(mockHistDocReqCacheService.processSDESNotificationForStatReqId(any, any))
        .thenReturn(Future.successful(Option(histDocRequestSearch)))

      running(app) {
        val req: FakeRequest[AnyContentAsJson] =
          FakeRequest(POST, "/metadata").withJsonBody(requestedPVATStatementNotificationRequest)

        val result = route(app, req).value
        status(result) mustBe OK

        verify(mockEmailThrottler, Mockito.times(1))
          .sendEmail(is(emailRequest(templateId = "customs_financials_requested_postponed_vat_notification")))(any)

        verify(mockDataStore, Mockito.times(1)).getVerifiedEmail(any)(any)
      }
    }

    "return BadRequest when request doesn't contain a seq of notifications json" in new Setup {
      val requestWithBadPayload: FakeRequest[JsValue] = FakeRequest(POST, "/metadata").withBody[JsValue](Json.obj())

      running(app) {
        val result = route(app, requestWithBadPayload).value
        status(result) mustBe BAD_REQUEST
      }
    }

    "return BadRequest when request contains an invalid notification in the json" in new Setup {
      val requestWithInvalidNotification: JsArray = Json.arr(Json.obj("invalidField" -> 1))

      running(app) {
        val req: FakeRequest[AnyContentAsJson] =
          FakeRequest(POST, "/metadata").withJsonBody(requestWithInvalidNotification)

        val result = route(app, req).value
        status(result) mustBe BAD_REQUEST
      }
    }
  }

  trait Setup {
    val eori: EORI = EORI("123456789")
    val mockNotificationCache: NotificationCache = mock[NotificationCache]
    val mockEmailThrottler: EmailThrottlerConnector = mock[EmailThrottlerConnector]
    val mockAuthConnector: CustomAuthConnector = mock[CustomAuthConnector]
    val mockDataStore: DataStoreConnector = mock[DataStoreConnector]
    val mockHistDocReqCacheService: HistoricDocumentRequestSearchCacheService =
      mock[HistoricDocumentRequestSearchCacheService]

    val mockHistDocReqCache: HistoricDocumentRequestSearchCache = mock[HistoricDocumentRequestSearchCache]

    val app: Application = GuiceApplicationBuilder().overrides(
      inject.bind[CustomAuthConnector].toInstance(mockAuthConnector),
      inject.bind[NotificationCache].toInstance(mockNotificationCache),
      inject.bind[EmailThrottlerConnector].toInstance(mockEmailThrottler),
      inject.bind[DataStoreConnector].toInstance(mockDataStore),
      inject.bind[HistoricDocumentRequestSearchCacheService].toInstance(mockHistDocReqCacheService),
      inject.bind[HistoricDocumentRequestSearchCache].toInstance(mockHistDocReqCache)
    ).configure(
      "microservice.metrics.enabled" -> false,
      "metrics.enabled" -> false,
      "auditing.enabled" -> false
    ).build()

    val addRequest: FakeRequest[AnyContentAsJson] = FakeRequest(POST, "/metadata").withJsonBody(Json.parse(
      """
        |[
        |    {
        |       "eori":"testEORI",
        |       "fileName": "vat-2018-05.pdf",
        |       "fileSize": 75251,
        |       "metadata": [
        |            {"metadata": "PeriodStartYear", "value": "2018"},
        |            {"metadata": "PeriodStartMonth", "value": "5"},
        |            {"metadata": "FileType", "value": "PDF"},
        |            {"metadata": "FileRole", "value": "C79Certificate"}
        |        ]
        |    },
        |    {
        |       "eori":"testEORI-12345",
        |       "fileName": "vat-2018-05.csv",
        |       "fileSize": 12345,
        |       "metadata": [
        |            {"metadata": "PeriodStartYear", "value": "2018"},
        |            {"metadata": "PeriodStartMonth", "value": "5"},
        |            {"metadata": "FileType", "value": "CSV"},
        |            {"metadata": "FileRole", "value": "C79Certificate"}
        |        ]
        |    },
        |    {
        |        "eori":"someEORI",
        |        "fileName": "statement-2018-09-19.pdf",
        |        "fileSize": 2417804,
        |        "metadata": [
        |            {"metadata": "checksum", "value": "whatever"},
        |            {"metadata": "eoriNumber", "value": "someEORI"},
        |            {"metadata": "FileRole", "value": "SecurityStatement"},
        |            {"metadata": "fileSize", "value": "9000"},
        |            {"metadata": "FileType", "value": "PDF"},
        |            {"metadata": "issueDate", "value": "19/09/2018"},
        |            {"metadata": "PeriodEndDay", "value": "19"},
        |            {"metadata": "PeriodEndMonth", "value": "9"},
        |            {"metadata": "PeriodEndYear", "value": "2018"},
        |            {"metadata": "PeriodStartDay", "value": "13"},
        |            {"metadata": "PeriodStartMonth", "value": "9"},
        |            {"metadata": "PeriodStartYear", "value": "2018"}
        |        ]
        |    }
        |]
      """.stripMargin))

    val searchID: UUID = UUID.randomUUID()
    val userId: String = "test_userId"
    val resultsFound: SearchResultStatus.Value = inProcess
    val searchStatusUpdateDate: String = emptyString
    val currentEori: String = "GB123456789012"
    val params: Params = Params("2", "2021", "4", "2021", "DutyDefermentStatement", "1234567")
    val emailAddress: EmailAddress = EmailAddress(TEST_EMAIL)
    val testEori: EORI = EORI(EORI_VALUE)
    val testEnrolment: Option[String] = Some("123456789")
    val errorMsg: String = "Error occurred"

    val searchRequests: Set[SearchRequest] = Set(
      SearchRequest("GB123456789012", "1abcdefg2-a2b1-abcd-abcd-0123456789", inProcess, emptyString, emptyString, 0),
      SearchRequest("GB234567890121", "5c79895-f0da-4472-af5a-d84d340e7mn6", inProcess, emptyString, emptyString, 0))

    val histDocRequestSearch: HistoricDocumentRequestSearch =
      HistoricDocumentRequestSearch(searchID, resultsFound, searchStatusUpdateDate, currentEori, params, searchRequests)

    def emailRequest(emailId: EmailAddress = EmailAddress(TEST_EMAIL),
                     templateId: String = "customs_financials_new_statement_notification",
                     params: Map[String, String] = Map("recipientName_line1" -> TEST_COMPANY),
                     force: Boolean = false,
                     enrolment: Option[String] = Some(EORI_VALUE)): EmailRequest = {
      EmailRequest(List(emailId), templateId, params, force, enrolment, None, None)
    }
  }
}
