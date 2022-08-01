/*
 * Copyright 2022 HM Revenue & Customs
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

import domain.{StandingAuthority, acc41}
import models._
import models.dec64.{FileUploadDetail, UploadedFile}
import models.requests.HistoricDocumentRequest
import models.requests.manageAuthorities._
import org.mockito.ArgumentCaptor
import org.scalatest.matchers.should.Matchers._
import play.api._
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.{JsValue, Json}
import play.api.test.Helpers.running
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.audit.http.connector._
import uk.gov.hmrc.play.audit.model.ExtendedDataEvent
import utils.SpecBase

import scala.concurrent._

class AuditingServiceSpec extends SpecBase {

  "AuditingService" should {
    "audit the ACC30 grant authority request data" in new Setup {

      val grantAuthorityRequest: GrantAuthorityRequest = GrantAuthorityRequest(
        Accounts(Some("9876543210"), Seq("12345678"), Some("GAN123456")),
        StandingAuthority(EORI("agentEORI"), "2020-11-01", Some("2020-12-31"), viewBalance = true),
        AuthorisedUser("John Smith", "Managing Director"),
        editRequest = false
      )

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

      val extendedDataEventCaptor: ArgumentCaptor[ExtendedDataEvent] = ArgumentCaptor.forClass(classOf[ExtendedDataEvent])

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

    "Audit the ACC41 audit Request Auth CSV Statement Request" in new Setup {

      val response: acc41.ResponseDetail = acc41.ResponseDetail(Some(""), Some(""))
      val request: acc41.RequestDetail = domain.acc41.RequestDetail(EORI("GB123456789"))

      val auditRequest =
        """{
          "requestingEori":"EORI(GB123456789)",
          "requestAcceptedDate":"Some()"
        }"""

      val extendedDataEventCaptor: ArgumentCaptor[ExtendedDataEvent] = ArgumentCaptor.forClass(classOf[ExtendedDataEvent])

      running(app) {
        when(mockAuditConnector.sendExtendedEvent(extendedDataEventCaptor.capture())(any, any))
          .thenReturn(Future.successful(AuditResult.Success))

        service.auditRequestAuthCSVStatementRequest(response, request)
        val result = extendedDataEventCaptor.getValue
        result.detail mustBe Json.parse(auditRequest)
        result.auditType mustBe "RequestAuthoritiesCSV"
        result.auditSource mustBe "customs-financials-api"
        result.tags.get("Request authorities")
      }
    }

    "audit the ACC30 edit authority request data" in new Setup {

      val updateAuthorityRequest: GrantAuthorityRequest = GrantAuthorityRequest(
        Accounts(Some("9876543210"), Seq("12345678"), Some("GAN123456")),
        StandingAuthority(EORI("agentEORI"), "2020-11-01", Some("2020-12-31"), viewBalance = true),
        AuthorisedUser("John Smith", "Managing Director"),
        editRequest = true
      )

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

      val extendedDataEventCaptor: ArgumentCaptor[ExtendedDataEvent] = ArgumentCaptor.forClass(classOf[ExtendedDataEvent])

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

      val extendedDataEventCaptor: ArgumentCaptor[ExtendedDataEvent] = ArgumentCaptor.forClass(classOf[ExtendedDataEvent])

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
        AccountNumber("123"), CdsDutyDefermentAccount, EORI("agentEORI"), AuthorisedUser("John Smith", "Managing Director")
      )

      val extendedDataEventCaptor: ArgumentCaptor[ExtendedDataEvent] = ArgumentCaptor.forClass(classOf[ExtendedDataEvent])

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
        AccountNumber("123"), CdsGeneralGuaranteeAccount, EORI("agentEORI"), AuthorisedUser("John Smith", "Managing Director")
      )

      val extendedDataEventCaptor: ArgumentCaptor[ExtendedDataEvent] = ArgumentCaptor.forClass(classOf[ExtendedDataEvent])

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

      val extendedDataEventCaptor: ArgumentCaptor[ExtendedDataEvent] = ArgumentCaptor.forClass(classOf[ExtendedDataEvent])
      val historicDocumentRequest: HistoricDocumentRequest = HistoricDocumentRequest(EORI("testEORI"), FileRole("DutyDefermentStatement"), 2020, 4, 2020, 12, Some("DAN123"))


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

      val extendedDataEventCaptor: ArgumentCaptor[ExtendedDataEvent] = ArgumentCaptor.forClass(classOf[ExtendedDataEvent])
      val historicDocumentRequest: HistoricDocumentRequest = HistoricDocumentRequest(EORI("testEORI"), FileRole("SecurityStatement"), 2019, 1, 2019, 3, None)

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

      val extendedDataEventCaptor: ArgumentCaptor[ExtendedDataEvent] = ArgumentCaptor.forClass(classOf[ExtendedDataEvent])
      val historicDocumentRequest: HistoricDocumentRequest = HistoricDocumentRequest(EORI("testEORI"), FileRole("C79Certificate"), 2019, 1, 2019, 3, None)

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

    "audit file upload request to Dec64" in new Setup {
      val auditRequest: JsValue = Json.parse(
        """{
          |   "eori":"eori",
          |   "caseNumber":"casenumber",
          |   "applicationName":"appName",
          |   "properties":{
          |      "uploadedFiles":[
          |         {
          |            "upscanReference":"upscanRef",
          |            "downloadUrl":"url",
          |            "uploadTimestamp":"String",
          |            "checksum":"sum",
          |            "fileName":"filename",
          |            "fileMimeType":"mimeType",
          |            "fileSize":12,
          |            "description":"file type"
          |         }
          |      ]
          |   }
          |}""".stripMargin)

      val extendedDataEventCaptor: ArgumentCaptor[ExtendedDataEvent] = ArgumentCaptor.forClass(classOf[ExtendedDataEvent])

      val uploadedFiles: UploadedFile = UploadedFile(upscanReference = "upscanRef", downloadUrl = "url", uploadTimestamp = "String",
        checksum = "sum", fileName = "filename", fileMimeType = "mimeType", fileSize = 12, "file type")

      val fileUploadDetail: FileUploadDetail = FileUploadDetail(id = "id", eori = EORI("eori"), caseNumber = "casenumber", declarationId = "MRN",
        entryNumber = false, applicationName = "appName", declarationType = "MRN", fileCount = 0, file = uploadedFiles, index = 0)

      running(app) {
        when(mockAuditConnector.sendExtendedEvent(extendedDataEventCaptor.capture())(any, any))
          .thenReturn(Future.successful(AuditResult.Success))

        service.auditFileUploadDetail(fileUploadDetail)
        val result = extendedDataEventCaptor.getValue
        result.detail mustBe auditRequest
        result.auditType mustBe "ViewAmendFileUpload"
        result.auditSource mustBe "customs-financials-api"
        result.tags.get("transactionName") mustBe Some("View and amend file upload")
      }
    }

    "not throw an exception when failing to audit the events" in new Setup {
      val historicDocumentRequest: HistoricDocumentRequest = HistoricDocumentRequest(EORI("testEORI"), FileRole("C79Certificate"), 2019, 1, 2019, 3, None)

      running(app) {
        val auditResult = AuditResult.Failure("failed to audit", Some(new Exception("error")))
        when(mockAuditConnector.sendExtendedEvent(any)(any, any)).thenReturn(Future.successful(auditResult))
        await(service.auditHistoricStatementRequest(historicDocumentRequest))
        auditResult.msg must be("failed to audit")
      }
    }

    "throw an exception when send fails to connect" in new Setup {
      when(mockAuditConnector.sendExtendedEvent(any)(any, any)).thenReturn(Future.failed(new Exception("An audit failure occurred")))
      val historicDocumentRequest: HistoricDocumentRequest = HistoricDocumentRequest(EORI("testEORI"), FileRole("C79Certificate"), 2019, 1, 2019, 3, None)

      running(app) {
        intercept[Exception] {
          await(service.auditHistoricStatementRequest(historicDocumentRequest))
        }
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
  }

}
