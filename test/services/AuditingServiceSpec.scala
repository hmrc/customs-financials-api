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

package services

import domain.{Notification, StandingAuthority, acc41}
import models._
import models.requests.{
  CashAccountPaymentDetails, CashAccountStatementRequestDetail,
  CashAccountTransactionSearchRequestDetails, DeclarationDetails, HistoricDocumentRequest, SearchType
}
import models.requests.ParamName.UCR
import models.requests.manageAuthorities._
import org.mockito.ArgumentCaptor
import org.scalatest.matchers.should.Matchers._
import play.api._
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json
import play.api.test.Helpers.running
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.audit.http.connector._
import uk.gov.hmrc.play.audit.model.ExtendedDataEvent
import utils.SpecBase
import utils.TestData._
import utils.Utils.emptyString

import scala.concurrent._

class AuditingServiceSpec extends SpecBase {

  "AuditingService" should {
    "audit the ACC30 grant authority request data" in new Setup {

      val grantAuthorityRequest: GrantAuthorityRequest = GrantAuthorityRequest(
        Accounts(Some("9876543210"), Seq("12345678"), Some("GAN123456")),
        StandingAuthority(EORI("agentEORI"), "2020-11-01", Some("2020-12-31"), viewBalance = true),
        AuthorisedUser("John Smith", "Managing Director"),
        editRequest = false)

      val auditRequest =
        """{
          "ownerEORI":"someEORI",
          "authorisedEORI":"agentEORI",
          "action":"Grant Authority",
          "accounts" : [
            { "accountType": "CDSCash", "accountNumber" : "9876543210" },
            { "accountType": "DutyDeferment", "accountNumber" : "12345678" },
            { "accountType": "GeneralGuarantee", "accountNumber": "GAN123456" }
          ],
          "startDate": "2020-11-01",
          "endDate": "2020-12-31",
          "seeBalance": true,
          "authoriserName": "John Smith",
          "authoriserJobRole": "Managing Director"
        }"""

      val extendedDataEventCaptor: ArgumentCaptor[ExtendedDataEvent] =
        ArgumentCaptor.forClass(classOf[ExtendedDataEvent])

      running(app) {
        when(mockAuditConnector.sendExtendedEvent(extendedDataEventCaptor.capture())(any, any))
          .thenReturn(Future.successful(AuditResult.Success))

        service.auditGrantAuthority(grantAuthorityRequest, EORI("someEORI"))
        val result = extendedDataEventCaptor.getValue
        result.detail mustBe Json.parse(auditRequest)
        result.auditType mustBe "ManageAuthority"
        result.auditSource mustBe "customs-financials-api"
        result.tags.get("transactionName") mustBe Some("Grant Authority")
      }
    }

    "audit the ACC30 edit authority request data" in new Setup {

      val updateAuthorityRequest: GrantAuthorityRequest = GrantAuthorityRequest(
        Accounts(Some("9876543210"), Seq("12345678"), Some("GAN123456")),
        StandingAuthority(EORI("agentEORI"), "2020-11-01", Some("2020-12-31"), viewBalance = true),
        AuthorisedUser("John Smith", "Managing Director"),
        editRequest = true)

      val auditRequest: String =
        """{
          "ownerEORI":"someEORI",
          "authorisedEORI":"agentEORI",
          "action":"Update Authority",
          "accountType": "CDSCash",
          "accountNumber" : "9876543210",
          "startDate": "2020-11-01",
          "endDate": "2020-12-31",
          "seeBalance": true,
          "authoriserName": "John Smith",
          "authoriserJobRole": "Managing Director"
        }"""

      val extendedDataEventCaptor: ArgumentCaptor[ExtendedDataEvent] =
        ArgumentCaptor.forClass(classOf[ExtendedDataEvent])

      running(app) {
        when(mockAuditConnector.sendExtendedEvent(extendedDataEventCaptor.capture())(any, any))
          .thenReturn(Future.successful(AuditResult.Success))

        service.auditEditAuthority(updateAuthorityRequest, EORI("someEORI"))
        val result = extendedDataEventCaptor.getValue
        result.detail mustBe Json.parse(auditRequest)
        result.auditType mustBe "UpdateAuthority"
        result.auditSource mustBe "customs-financials-api"
        result.tags.get("transactionName") mustBe Some("Update Authority")
      }
    }

    "audit the ACC30 revoke authority request data cash account" in new Setup {
      val auditRequest =
        """{
            "ownerEORI":"someEORI",
            "authorisedEORI":"agentEORI",
            "action":"Revoke Authority",
            "accountType":"CDSCash",
            "accountNumber":"123",
            "authoriserName": "John Smith",
            "authoriserJobRole": "Managing Director"
          }"""

      val revokeAuthorityRequest: RevokeAuthorityRequest = RevokeAuthorityRequest(
        AccountNumber("123"), CdsCashAccount, EORI("agentEORI"), AuthorisedUser("John Smith", "Managing Director")
      )

      val extendedDataEventCaptor: ArgumentCaptor[ExtendedDataEvent] =
        ArgumentCaptor.forClass(classOf[ExtendedDataEvent])

      running(app) {
        when(mockAuditConnector.sendExtendedEvent(extendedDataEventCaptor.capture())(any, any))
          .thenReturn(Future.successful(AuditResult.Success))

        service.auditRevokeAuthority(revokeAuthorityRequest, EORI("someEORI"))
        val result = extendedDataEventCaptor.getValue
        result.detail mustBe Json.parse(auditRequest)
        result.auditType mustBe "ManageAuthority"
        result.auditSource mustBe "customs-financials-api"
        result.tags.get("transactionName") mustBe Some("Revoke Authority")
      }
    }

    "audit the ACC30 revoke authority request data duty deferment account" in new Setup {
      val auditRequest =
        """{
            "ownerEORI":"someEORI",
            "authorisedEORI":"agentEORI",
            "action":"Revoke Authority",
            "accountType":"DutyDeferment",
            "accountNumber":"123",
            "authoriserName": "John Smith",
            "authoriserJobRole": "Managing Director"
          }"""

      val revokeAuthorityRequest: RevokeAuthorityRequest = RevokeAuthorityRequest(
        AccountNumber("123"),
        CdsDutyDefermentAccount,
        EORI("agentEORI"),
        AuthorisedUser("John Smith", "Managing Director"))

      val extendedDataEventCaptor: ArgumentCaptor[ExtendedDataEvent] =
        ArgumentCaptor.forClass(classOf[ExtendedDataEvent])

      running(app) {
        when(mockAuditConnector.sendExtendedEvent(extendedDataEventCaptor.capture())(any, any))
          .thenReturn(Future.successful(AuditResult.Success))

        service.auditRevokeAuthority(revokeAuthorityRequest, EORI("someEORI"))
        val result = extendedDataEventCaptor.getValue
        result.detail mustBe Json.parse(auditRequest)
        result.auditType mustBe "ManageAuthority"
        result.auditSource mustBe "customs-financials-api"
        result.tags.get("transactionName") mustBe Some("Revoke Authority")
      }
    }

    "audit the ACC30 revoke authority request data guarantee account" in new Setup {
      val auditRequest =
        """{
            "ownerEORI":"someEORI",
            "authorisedEORI":"agentEORI",
            "action":"Revoke Authority",
            "accountType":"GeneralGuarantee",
            "accountNumber":"123",
            "authoriserName": "John Smith",
            "authoriserJobRole": "Managing Director"
          }"""

      val revokeAuthorityRequest: RevokeAuthorityRequest = RevokeAuthorityRequest(
        AccountNumber("123"),
        CdsGeneralGuaranteeAccount,
        EORI("agentEORI"),
        AuthorisedUser("John Smith", "Managing Director"))

      val extendedDataEventCaptor: ArgumentCaptor[ExtendedDataEvent] =
        ArgumentCaptor.forClass(classOf[ExtendedDataEvent])

      running(app) {
        when(mockAuditConnector.sendExtendedEvent(extendedDataEventCaptor.capture())(any, any))
          .thenReturn(Future.successful(AuditResult.Success))

        service.auditRevokeAuthority(revokeAuthorityRequest, EORI("someEORI"))
        val result = extendedDataEventCaptor.getValue
        result.detail mustBe Json.parse(auditRequest)
        result.auditType mustBe "ManageAuthority"
        result.auditSource mustBe "customs-financials-api"
        result.tags.get("transactionName") mustBe Some("Revoke Authority")
      }
    }

    "audit requested Duty Deferment statements" in new Setup {
      val auditRequest: String =
        """{
          |   "eori" : "testEORI",
          |   "documentType" : "DutyDefermentStatement",
          |   "accountNumber" : "DAN123",
          |   "periodStartYear" : "2020",
          |   "periodStartMonth" : "04",
          |   "periodEndYear" : "2020",
          |   "periodEndMonth" : "12"
          | }""".stripMargin

      val extendedDataEventCaptor: ArgumentCaptor[ExtendedDataEvent] =
        ArgumentCaptor.forClass(classOf[ExtendedDataEvent])

      val historicDocumentRequest: HistoricDocumentRequest =
        HistoricDocumentRequest(
          EORI("testEORI"),
          FileRole("DutyDefermentStatement"),
          YEAR_2020,
          MONTH_4,
          YEAR_2020,
          MONTH_12,
          Some("DAN123"))

      running(app) {
        when(mockAuditConnector.sendExtendedEvent(extendedDataEventCaptor.capture())(any, any))
          .thenReturn(Future.successful(AuditResult.Success))

        service.auditHistoricStatementRequest(historicDocumentRequest)
        val result = extendedDataEventCaptor.getValue
        result.detail mustBe Json.parse(auditRequest)
        result.auditType mustBe "RequestHistoricStatement"
        result.auditSource mustBe "customs-financials-api"
        result.tags.get("transactionName") mustBe Some("Request historic statements")
      }
    }

    "audit requested Security statements" in new Setup {
      val auditRequest: String =
        """{
          |   "eori" : "testEORI",
          |   "documentType" : "SecuritiesStatement",
          |   "accountNumber" : "",
          |   "periodStartYear" : "2019",
          |   "periodStartMonth" : "01",
          |   "periodEndYear" : "2019",
          |   "periodEndMonth" : "03"
          | }""".stripMargin

      val extendedDataEventCaptor: ArgumentCaptor[ExtendedDataEvent] =
        ArgumentCaptor.forClass(classOf[ExtendedDataEvent])

      val historicDocumentRequest: HistoricDocumentRequest =
        HistoricDocumentRequest(
          EORI("testEORI"), FileRole("SecurityStatement"), YEAR_2019, MONTH_1, YEAR_2019, MONTH_3, None)

      running(app) {
        when(mockAuditConnector.sendExtendedEvent(extendedDataEventCaptor.capture())(any, any))
          .thenReturn(Future.successful(AuditResult.Success))

        service.auditHistoricStatementRequest(historicDocumentRequest)
        val result = extendedDataEventCaptor.getValue

        result.detail mustBe Json.parse(auditRequest)
        result.auditType mustBe "RequestHistoricStatement"
        result.auditSource mustBe "customs-financials-api"
        result.tags.get("transactionName") mustBe Some("Request historic statements")
      }
    }

    "audit requested C79 statements" in new Setup {
      val auditRequest: String =
        """{
          |   "eori" : "testEORI",
          |   "documentType" : "C79Statement",
          |   "accountNumber" : "",
          |   "periodStartYear" : "2019",
          |   "periodStartMonth" : "01",
          |   "periodEndYear" : "2019",
          |   "periodEndMonth" : "03"
          | }""".stripMargin

      val extendedDataEventCaptor: ArgumentCaptor[ExtendedDataEvent] =
        ArgumentCaptor.forClass(classOf[ExtendedDataEvent])

      val historicDocumentRequest: HistoricDocumentRequest =
        HistoricDocumentRequest(
          EORI("testEORI"), FILE_ROLE_C79_CERTIFICATE, YEAR_2019, MONTH_1, YEAR_2019, MONTH_3, None)

      running(app) {
        when(mockAuditConnector.sendExtendedEvent(extendedDataEventCaptor.capture())(any, any))
          .thenReturn(Future.successful(AuditResult.Success))

        service.auditHistoricStatementRequest(historicDocumentRequest)
        val result = extendedDataEventCaptor.getValue

        result.detail mustBe Json.parse(auditRequest)
        result.auditType mustBe "RequestHistoricStatement"
        result.auditSource mustBe "customs-financials-api"
        result.tags.get("transactionName") mustBe Some("Request historic statements")
      }
    }

    "not throw an exception when failing to audit the events" in new Setup {
      val historicDocumentRequest: HistoricDocumentRequest =
        HistoricDocumentRequest(
          EORI("testEORI"), FILE_ROLE_C79_CERTIFICATE, YEAR_2019, MONTH_1, YEAR_2019, MONTH_3, None)

      running(app) {
        val auditResult = AuditResult.Failure("failed to audit", Some(new Exception("error")))
        when(mockAuditConnector.sendExtendedEvent(any)(any, any)).thenReturn(Future.successful(auditResult))
        await(service.auditHistoricStatementRequest(historicDocumentRequest))

        auditResult.msg must be("failed to audit")
      }
    }

    "throw an exception when send fails to connect" in new Setup {
      when(mockAuditConnector.sendExtendedEvent(any)(any, any))
        .thenReturn(Future.failed(new Exception("An audit failure occurred")))

      val historicDocumentRequest: HistoricDocumentRequest =
        HistoricDocumentRequest(
          EORI("testEORI"), FILE_ROLE_C79_CERTIFICATE, YEAR_2019, MONTH_1, YEAR_2019, MONTH_3, None)

      running(app) {
        intercept[Exception] {
          await(service.auditHistoricStatementRequest(historicDocumentRequest))
        }
      }
    }

    "Audit the ACC41 audit Display Auth CSV Statement Request" in new Setup {

      val display: Map[String, String] = Map("Name" -> "DISPLAY_STANDING_AUTHORITIES_NAME",
        "Type" -> "DISPLAY_STANDING_AUTHORITIES_TYPE")

      val notification: Notification = Notification(
        EORI("GB123456789"),
        FileRole("fileRole"),
        "file name",
        FILE_SIZE_1000L,
        Some(CURRENT_LOCAL_DATE),
        display)

      val fileType: FileType = FileType("CSV")

      val auditRequest =
        """{
        "Eori":"EORI(GB123456789)",
        "isHistoric":false,
        "fileName":"file name",
        "fileRole":"FileRole(fileRole)",
        "fileType":"FileType(CSV)"
      }"""

      val extendedDataEventCaptor: ArgumentCaptor[ExtendedDataEvent]
      = ArgumentCaptor.forClass(classOf[ExtendedDataEvent])

      running(app) {
        when(mockAuditConnector.sendExtendedEvent(extendedDataEventCaptor.capture())(any, any))
          .thenReturn(Future.successful(AuditResult.Success))

        service.auditDisplayAuthCSVStatementRequest(notification, fileType)
        val result = extendedDataEventCaptor.getValue
        result.detail mustBe Json.parse(auditRequest)
        result.auditType mustBe "DisplayStandingAuthoritiesCSV"
        result.auditSource mustBe "customs-financials-api"
        result.tags.get("transactionName") mustBe Some("Display Authorities CSV")
      }
    }

    "Audit the ACC41 audit Request Auth CSV Statement Request" in new Setup {
      val response: acc41.ResponseDetail = acc41.ResponseDetail(Some(emptyString), Some(emptyString))
      val request: acc41.RequestDetail = domain.acc41.RequestDetail(EORI("GB123456789"), Some(EORI("someAltEori")))

      val auditRequest =
        """{
          "requestingEori":"GB123456789",
          "requestAcceptedDate":""
        }"""

      val extendedDataEventCaptor: ArgumentCaptor[ExtendedDataEvent] =
        ArgumentCaptor.forClass(classOf[ExtendedDataEvent])

      running(app) {
        when(mockAuditConnector.sendExtendedEvent(extendedDataEventCaptor.capture())(any, any))
          .thenReturn(Future.successful(AuditResult.Success))

        service.auditRequestAuthCSVStatementRequest(response, request)
        val result = extendedDataEventCaptor.getValue

        result.detail mustBe Json.parse(auditRequest)
        result.auditType mustBe "RequestAuthoritiesCSV"
        result.auditSource mustBe "customs-financials-api"
        result.tags.get("transactionName") mustBe Some("Request Authorities CSV")
      }
    }

    "audit the ACC44 cash account transactions search request (eventId EXPA021)" when {

      "request is of searchType P" in new Setup {
        val auditRequest =
          """{
            "can": "12345678909",
            "ownerEORI": "GB123456789",
            "searchType": "P",
            "cashAccountPaymentDetails": {
                "amount": 9999.99,
                "dateFrom": "2024-05-28",
                "dateTo": "2024-05-28"
            }
        }"""

        val extendedDataEventCaptor: ArgumentCaptor[ExtendedDataEvent] =
          ArgumentCaptor.forClass(classOf[ExtendedDataEvent])

        running(app) {
          when(mockAuditConnector.sendExtendedEvent(extendedDataEventCaptor.capture())(any, any))
            .thenReturn(Future.successful(AuditResult.Success))

          service.auditCashAccountTransactionsSearch(cashAccTransactionPaymentSearchRequestDetails)

          val result = extendedDataEventCaptor.getValue

          result.detail mustBe Json.parse(auditRequest)
          result.auditType mustBe "SearchCashAccountTransactions"
          result.auditSource mustBe "customs-financials-api"
          result.tags.get("transactionName") mustBe Some("Search cash account transactions")
        }
      }

      "request is of searchType D" in new Setup {
        val auditRequest =
          """{
            "can": "12345678909",
            "ownerEORI": "GB123456789",
            "searchType": "D",
            "declarationDetails": {
                  "paramName": "UCR",
                  "paramValue": "123456789abcd"
              }
        }"""

        val extendedDataEventCaptor: ArgumentCaptor[ExtendedDataEvent] =
          ArgumentCaptor.forClass(classOf[ExtendedDataEvent])

        running(app) {
          when(mockAuditConnector.sendExtendedEvent(extendedDataEventCaptor.capture())(any, any))
            .thenReturn(Future.successful(AuditResult.Success))

          service.auditCashAccountTransactionsSearch(cashAccTransactionDeclarationSearchRequestDetails)

          val result = extendedDataEventCaptor.getValue

          result.detail mustBe Json.parse(auditRequest)
          result.auditType mustBe "SearchCashAccountTransactions"
          result.auditSource mustBe "customs-financials-api"
          result.tags.get("transactionName") mustBe Some("Search cash account transactions")
        }
      }
    }

    "audit the ACC45 cash account statements request (eventId EXPA022)" in new Setup {
      val auditRequest =
        """{
            "eori": "GB123456789",
            "can": "12345678909",
            "dateFrom": "2024-05-28",
            "dateTo": "2024-05-28"
        }"""

      val extendedDataEventCaptor: ArgumentCaptor[ExtendedDataEvent] =
        ArgumentCaptor.forClass(classOf[ExtendedDataEvent])

      running(app) {
        when(mockAuditConnector.sendExtendedEvent(extendedDataEventCaptor.capture())(any, any))
          .thenReturn(Future.successful(AuditResult.Success))

        service.auditCashAccountStatementsRequest(cashAccStatementReqDetail)

        val result = extendedDataEventCaptor.getValue

        result.detail mustBe Json.parse(auditRequest)
        result.auditType mustBe "SearchCashAccountTransactions"
        result.auditSource mustBe "customs-financials-api"
        result.tags.get("transactionName") mustBe Some("Search cash account transactions")
      }
    }
  }

  trait Setup {
    implicit val hc: HeaderCarrier = HeaderCarrier()
    implicit val executionContext: ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global

    val mockAuditConnector: AuditConnector = mock[AuditConnector]

    val app: Application = GuiceApplicationBuilder().overrides(
      inject.bind[AuditConnector].toInstance(mockAuditConnector)
    ).build()

    val service: AuditingService = app.injector.instanceOf[AuditingService]
    val eoriNumber = "GB123456789"

    val cashAccTransactionPaymentSearchRequestDetails: CashAccountTransactionSearchRequestDetails =
      CashAccountTransactionSearchRequestDetails(
        CAN,
        eoriNumber,
        SearchType.P,
        declarationDetails = None,
        cashAccountPaymentDetails = Some(CashAccountPaymentDetails(AMOUNT, Some(DATE_STRING), Some(DATE_STRING))))

    val cashAccTransactionDeclarationSearchRequestDetails: CashAccountTransactionSearchRequestDetails =
      CashAccountTransactionSearchRequestDetails(
        CAN,
        eoriNumber,
        SearchType.D,
        declarationDetails = Some(DeclarationDetails(UCR, "123456789abcd")),
        cashAccountPaymentDetails = None)

    val cashAccStatementReqDetail: CashAccountStatementRequestDetail = CashAccountStatementRequestDetail(
      eoriNumber, CAN, DATE_STRING, DATE_STRING)
  }
}
