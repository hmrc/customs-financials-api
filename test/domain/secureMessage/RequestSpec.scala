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

import config.MetaConfig.Platform.SOURCE_MDTP
import domain.secureMessage
import domain.secureMessage.SecureMessage._
import models._
import utils.SpecBase
import utils.Utils.{emptyString, encodeToUTF8Charsets, englishLangKey, welshLangKey}

import java.time.LocalDate
import java.util.UUID

class RequestSpec extends SpecBase {
  "apply" should {
    "create the object correctly" in new Setup {

      val expectedRequest: Request = Request(
        externalRef = ExternalReference(searchID.toString, SOURCE_MDTP),
        recipient = Recipient(
          regime = "cds",
          taxIdentifier = TaxIdentifier("HMRC-CUS-ORG", currentEori),
          name = Name("Company Name"),
          email = "test@test.com"),
        tags = Tags("CDS Financials"),
        content = testContents,
        messageType = "customs_financials_requested_duty_deferment_not_found",
        validFrom = LocalDate.now().toString,
        alertQueue = "DEFAULT")

      val actualRequestOb: Request = Request(histDocRequestSearch,
        EmailAddress("test@test.com"), "Company Name")

      actualRequestOb.recipient mustBe expectedRequest.recipient
      actualRequestOb.tags mustBe expectedRequest.tags
      actualRequestOb.validFrom mustBe expectedRequest.validFrom
      actualRequestOb.alertQueue mustBe expectedRequest.alertQueue
      actualRequestOb.messageType mustBe expectedRequest.messageType
    }
  }

  "getContents" should {
    "return DutyDefermentStatement for English language" in new Setup {
      override val params: Params = Params("02", "2021", "04", "2021", "DutyDefermentStatement", "1234567")
      val modifiedDoc: HistoricDocumentRequestSearch = histDocRequestSearch.copy(params = params)
      val expectedRequest: Request = Request(modifiedDoc, EmailAddress("Email"), "Company Name")

      expectedRequest.content.head.subject mustBe s"$dutyStatement$subjectDate"
      expectedRequest.content.head.body mustBe
        encodeToUTF8Charsets(dutyDefermentBody("Company Name", dateRange))
    }

    "return DutyDefermentStatement for English language with empty company name" in new Setup {
      override val params: Params = Params("02", "2021", "04", "2021", "DutyDefermentStatement", "1234567")
      val modifiedDoc: HistoricDocumentRequestSearch = histDocRequestSearch.copy(params = params)
      val expectedRequest: Request = Request(modifiedDoc, EmailAddress("Email"), emptyString)

      expectedRequest.content.head.subject mustBe s"$dutyStatement$subjectDate"
      expectedRequest.content.head.body mustBe
        encodeToUTF8Charsets(dutyDefermentBody(emptyString, dateRange))
    }

    "return C79Certificate for English language" in new Setup {
      override val params: Params = Params("02", "2021", "04", "2021", "C79Certificate", "1234567")
      val modifiedDoc: HistoricDocumentRequestSearch = histDocRequestSearch.copy(params = params)
      val expectedRequest: Request = Request(modifiedDoc, EmailAddress("Email"), "Company Name")

      expectedRequest.content.head.subject mustBe s"$c79cert$subjectDate"
      expectedRequest.content.head.body mustBe
        encodeToUTF8Charsets(c79CertificateBody("Company Name", dateRange))
    }

    "return C79Certificate for English language with empty company name" in new Setup {
      override val params: Params = Params("02", "2021", "04", "2021", "C79Certificate", "1234567")
      val modifiedDoc: HistoricDocumentRequestSearch = histDocRequestSearch.copy(params = params)
      val expectedRequest: Request = Request(modifiedDoc, EmailAddress("Email"), emptyString)

      expectedRequest.content.head.subject mustBe s"$c79cert$subjectDate"
      expectedRequest.content.head.body mustBe
        encodeToUTF8Charsets(c79CertificateBody(emptyString, dateRange))
    }

    "return SecurityStatement for English language" in new Setup {
      override val params: Params = Params("02", "2021", "04", "2021", "SecurityStatement", "1234567")
      val modifiedDoc: HistoricDocumentRequestSearch = histDocRequestSearch.copy(params = params)
      val expectedRequest: Request = Request(modifiedDoc, EmailAddress("Email"), "Company Name")

      expectedRequest.content.head.subject mustBe s"$securityStatement$subjectDate"
      expectedRequest.content.head.body mustBe
        encodeToUTF8Charsets(securityBody("Company Name", dateRange))
    }

    "return SecurityStatement for English language with empty company name" in new Setup {
      override val params: Params = Params("02", "2021", "04", "2021", "SecurityStatement", "1234567")
      val modifiedDoc: HistoricDocumentRequestSearch = histDocRequestSearch.copy(params = params)
      val expectedRequest: Request = Request(modifiedDoc, EmailAddress("Email"), emptyString)

      expectedRequest.content.head.subject mustBe s"$securityStatement$subjectDate"
      expectedRequest.content.head.body mustBe
        encodeToUTF8Charsets(securityBody(emptyString, dateRange))
    }

    "return PostponedVATStatement for English language" in new Setup {
      override val params: Params = Params("02", "2021", "04", "2021", "PostponedVATStatement", "1234567")
      val modifiedDoc: HistoricDocumentRequestSearch = histDocRequestSearch.copy(params = params)
      val expectedRequest: Request = Request(modifiedDoc, EmailAddress("Email"), "Company Name")

      expectedRequest.content.head.subject mustBe s"$postPonedVATStatement$subjectDate"
      expectedRequest.content.head.body mustBe
        encodeToUTF8Charsets(postponedVATBody("Company Name", dateRange))
    }

    "return PostponedVATStatement for English language with empty company name" in new Setup {
      override val params: Params = Params("02", "2021", "04", "2021", "PostponedVATStatement", "1234567")
      val modifiedDoc: HistoricDocumentRequestSearch = histDocRequestSearch.copy(params = params)
      val expectedRequest: Request = Request(modifiedDoc, EmailAddress("Email"), emptyString)

      expectedRequest.content.head.subject mustBe s"$postPonedVATStatement$subjectDate"
      expectedRequest.content.head.body mustBe
        encodeToUTF8Charsets(postponedVATBody(emptyString, dateRange))
    }

    "return DutyDefermentStatement for Welsh language" in new Setup {
      override val params: Params = Params("02", "2021", "04", "2021", "DutyDefermentStatement", "1234567")
      val modifiedDoc: HistoricDocumentRequestSearch = histDocRequestSearch.copy(params = params)
      val expectedRequest: Request = Request(modifiedDoc, EmailAddress("Email"), "Company Name")

      expectedRequest.content(1).subject mustBe s"$dutyStatementCy$subjectDateCy"
      expectedRequest.content(1).body mustBe encodeToUTF8Charsets(
        dutyDefermentBody("Company Name", dateRangeForWelsh, welshLangKey))
    }

    "return DutyDefermentStatement for Welsh language with empty company name" in new Setup {
      override val params: Params = Params("02", "2021", "04", "2021", "DutyDefermentStatement", "1234567")
      val modifiedDoc: HistoricDocumentRequestSearch = histDocRequestSearch.copy(params = params)
      val expectedRequest: Request = Request(modifiedDoc, EmailAddress("Email"), emptyString)

      expectedRequest.content(1).subject mustBe s"$dutyStatementCy$subjectDateCy"
      expectedRequest.content(1).body mustBe encodeToUTF8Charsets(
        dutyDefermentBody(emptyString, dateRangeForWelsh, welshLangKey))
    }

    "return C79Certificate for Welsh language" in new Setup {
      override val params: Params = Params("02", "2021", "04", "2021", "C79Certificate", "1234567")
      val modifiedDoc: HistoricDocumentRequestSearch = histDocRequestSearch.copy(params = params)
      val expectedRequest: Request = Request(modifiedDoc, EmailAddress("Email"), "Company Name")

      expectedRequest.content(1).subject mustBe s"$c79certCy$subjectDateCy"
      expectedRequest.content(1).body mustBe
        encodeToUTF8Charsets(c79CertificateBody(
          "Company Name", dateRangeForWelsh, welshLangKey))
    }

    "return C79Certificate for Welsh language with empty company name" in new Setup {
      override val params: Params = Params("02", "2021", "04", "2021", "C79Certificate", "1234567")
      val modifiedDoc: HistoricDocumentRequestSearch = histDocRequestSearch.copy(params = params)
      val expectedRequest: Request = Request(modifiedDoc, EmailAddress("Email"), emptyString)

      expectedRequest.content(1).subject mustBe s"$c79certCy$subjectDateCy"
      expectedRequest.content(1).body mustBe
        encodeToUTF8Charsets(c79CertificateBody(emptyString, dateRangeForWelsh, welshLangKey))
    }

    "return SecurityStatement for Welsh language" in new Setup {
      override val params: Params = Params("02", "2021", "04", "2021", "SecurityStatement", "1234567")
      val modifiedDoc: HistoricDocumentRequestSearch = histDocRequestSearch.copy(params = params)
      val expectedRequest: Request = Request(modifiedDoc, EmailAddress("Email"), "Company Name")

      expectedRequest.content(1).subject mustBe s"$securityStatementCy$subjectDateCy"
      expectedRequest.content(1).body mustBe
        encodeToUTF8Charsets(securityBody("Company Name", dateRangeForWelsh, welshLangKey))
    }

    "return SecurityStatement for Welsh language with empty company name" in new Setup {
      override val params: Params = Params("02", "2021", "04", "2021", "SecurityStatement", "1234567")
      val modifiedDoc: HistoricDocumentRequestSearch = histDocRequestSearch.copy(params = params)
      val expectedRequest: Request = Request(modifiedDoc, EmailAddress("Email"), emptyString)

      expectedRequest.content(1).subject mustBe s"$securityStatementCy$subjectDateCy"
      expectedRequest.content(1).body mustBe
        encodeToUTF8Charsets(securityBody(emptyString, dateRangeForWelsh, welshLangKey))
    }

    "return PostponedVATStatement for Welsh language" in new Setup {
      override val params: Params = Params("02", "2021", "04", "2021", "PostponedVATStatement", "1234567")
      val modifiedDoc: HistoricDocumentRequestSearch = histDocRequestSearch.copy(params = params)
      val expectedRequest: Request = Request(modifiedDoc, EmailAddress("Email"), "Company Name")

      expectedRequest.content(1).subject mustBe s"$postPonedVATStatementCy$subjectDateCy"
      expectedRequest.content(1).body mustBe
        encodeToUTF8Charsets(postponedVATBody(
          "Company Name", dateRangeForWelsh, welshLangKey))
    }

    "return PostponedVATStatement for Welsh language with empty company name" in new Setup {
      override val params: Params = Params("02", "2021", "04", "2021", "PostponedVATStatement", "1234567")
      val modifiedDoc: HistoricDocumentRequestSearch = histDocRequestSearch.copy(params = params)
      val expectedRequest: Request = Request(modifiedDoc, EmailAddress("Email"), emptyString)

      expectedRequest.content(1).subject mustBe s"$postPonedVATStatementCy$subjectDateCy"
      expectedRequest.content(1).body mustBe
        encodeToUTF8Charsets(postponedVATBody(emptyString, dateRangeForWelsh, welshLangKey))
    }

    "return eng and cy in list" in new Setup {

      val contents: List[Content] = List(
        Content(englishLangKey, "DutyDefermentStatement", dutyDefermentBody("Company Name", dateRange)),
        Content(welshLangKey, "DutyDefermentStatement", dutyDefermentBody("Company Name", dateRange, welshLangKey)))

      val expectedRequest: Request = Request(externalRef = ExternalReference(searchID.toString, SOURCE_MDTP),
        recipient = Recipient(
          regime = "cds",
          taxIdentifier = TaxIdentifier("HMRC-CUS-ORG", currentEori),
          name = Name("Company Name"),
          email = "test@test.com"),
        tags = Tags("CDS Financials"),
        content = contents,
        messageType = "newMessageAlert",
        validFrom = LocalDate.now().toString,
        alertQueue = "DEFAULT")

      expectedRequest.content.length mustBe 2
      expectedRequest.content mustBe testContents
    }
  }

  "MessageType" should {
    "return DutyDefermentTemplate" in new Setup {
      override val params: Params = Params("02", "2021", "04", "2021", "DutyDefermentStatement", "1234567")
      val modifiedDoc: HistoricDocumentRequestSearch = histDocRequestSearch.copy(params = params)
      val expectedRequest: Request = Request.apply(modifiedDoc, EmailAddress("Email"), "Company Name")
      expectedRequest.messageType mustBe DutyDefermentTemplate
    }

    "return C79CertificateTemplate" in new Setup {
      override val params: Params = Params("02", "2021", "04", "2021", "C79Certificate", "1234567")
      val modifiedDoc: HistoricDocumentRequestSearch = histDocRequestSearch.copy(params = params)
      val expectedRequest: Request = Request.apply(modifiedDoc, EmailAddress("Email"), "Company Name")
      expectedRequest.messageType mustBe C79CertificateTemplate
    }

    "return SecurityTemplate" in new Setup {
      override val params: Params = Params("02", "2021", "04", "2021", "SecurityStatement", "1234567")
      val modifiedDoc: HistoricDocumentRequestSearch = histDocRequestSearch.copy(params = params)
      val expectedRequest: Request = Request.apply(modifiedDoc, EmailAddress("Email"), "Company Name")
      expectedRequest.messageType mustBe SecurityTemplate
    }

    "return PostponedVATTemplate" in new Setup {
      override val params: Params = Params("02", "2021", "04", "2021", "PostponedVATStatement", "1234567")
      val modifiedDoc: HistoricDocumentRequestSearch = histDocRequestSearch.copy(params = params)
      val expectedRequest: Request = Request.apply(modifiedDoc, EmailAddress("Email"), "Company Name")
      expectedRequest.messageType mustBe PostponedVATTemplate
    }
  }
}

trait Setup {
  val searchID: UUID = UUID.randomUUID()
  val userId: String = "test_userId"
  val resultsFound: SearchResultStatus.Value = SearchResultStatus.inProcess
  val searchStatusUpdateDate: String = emptyString
  val currentEori: String = "GB123456789012"
  val params: Params = Params("02", "2021", "04", "2021", "DutyDefermentStatement", "1234567")
  val dataRangeParam: Params = Params("02", "2021", "04", "2021", "DutyDefermentStatement", "1234567")
  val dateRange: DateRange = DateRange(dataRangeParam, englishLangKey)

  val dateRangeForWelsh: DateRange = DateRange(dataRangeParam, welshLangKey)

  val searchRequests: Set[SearchRequest] = Set(
    SearchRequest("GB123456789012", "5b89895-f0da-4472-af5a-d84d340e7mn5",
      SearchResultStatus.inProcess, emptyString, emptyString, 0),
    SearchRequest("GB234567890121", "5c79895-f0da-4472-af5a-d84d340e7mn6",
      SearchResultStatus.inProcess, emptyString, emptyString, 0))

  val DutyDefermentTemplate = "customs_financials_requested_duty_deferment_not_found"
  val C79CertificateTemplate = "customs_financials_requested_c79_certificate_not_found"
  val SecurityTemplate = "customs_financials_requested_notification_adjustment_statements_not_found"
  val PostponedVATTemplate = "customs_financials_requested_postponed_import_vat_statements_not_found"

  val testContents: List[Content] = {
    List(secureMessage.Content(englishLangKey, "DutyDefermentStatement",
      dutyDefermentBody("Company Name", dateRange, englishLangKey)),
      secureMessage.Content(welshLangKey, "DutyDefermentStatement",
        dutyDefermentBody("Company Name", dateRange, welshLangKey)))
  }

  val dutyStatement: String = "Requested duty deferment statements "
  val c79cert: String = "Requested import VAT certificates (C79) "
  val securityStatement: String = "Requested notification of adjustment statements "
  val postPonedVATStatement: String = "Requested postponed import VAT statements "

  val dutyStatementCy = "Datganiadau gohirio tollau a wnaed cais amdanynt "
  val c79certCy = "Tystysgrifau TAW mewnforio (C79) a wnaed cais amdanynt "
  val securityStatementCy = "Hysbysiad o ddatganiadau addasu a wnaed cais amdanynt"
  val postPonedVATStatementCy = "Datganiadau TAW mewnforio ohiriedig a wnaed cais amdanynt "

  val subjectDate = "02 2021 to 04 2021"
  val subjectDateCy = "02 2021 i 04 2021"

  val histDocRequestSearch: HistoricDocumentRequestSearch =
    HistoricDocumentRequestSearch(searchID,
      resultsFound,
      searchStatusUpdateDate,
      currentEori,
      params,
      searchRequests)

  def requestJsValue: String =
    s"""{
       |  "externalRef": {
       |    "id": "abcd12345",
       |    "source": "mdtp"
       |  },
       |  "recipient": {
       |    "regime": "cds",
       |    "taxIdentifier": {
       |      "name": "HMRC-CUS-ORG",
       |      "value": "GB333186811543"
       |    },
       |    "name": {
       |      "line1": "Test",
       |      "line2": "CDS",
       |      "line3": "Financials"
       |    },
       |    "email": "test@test.com"
       |  },
       |  "tags": {
       |    "notificationType": "CDS Direct Debit"
       |  },
       |  "content": [
       |    {
       |      "lang": "en",
       |      "subject": "Test CDS Financials",
       |      "body": "Message content - 4254101384174917141"
       |    },
       |    {
       |      "lang": "cy",
       |      "subject": "Nodyn atgoffa i ffeilio ffurflen Hunanasesiad",
       |      "body": "Cynnwys - 4254101384174917141"
       |    }
       |  ],
       |  "messageType": "newMessageAlert",
       |  "validFrom": ${LocalDate.now.toString}",
       |  "alertQueue": "DEFAULT"
       |}""".stripMargin
}
