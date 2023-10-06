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

import models._
import utils.SpecBase
import utils.Utils.{emptyString, encodeToUTF8Charsets}
import java.time.LocalDate
import java.util.UUID

import domain.secureMessage
import domain.secureMessage.SecureMessage._

class RequestSpec extends SpecBase {
  "apply" should {
    "create the object correctly" in new Setup {

      val expectedRequest: Request = Request(
        externalRef = ExternalReference(searchID.toString, "mdtp"),
        recipient = Recipient(
          regime = "cds",
          taxIdentifier = TaxIdentifier("HMRC-CUS-ORG", currentEori),
          fullName = "Company Name",
          email = "test@test.com"),
        tags = Tags("CDS Financials"),
        content = TestContents,
        messageType = "newMessageAlert",
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
    "return DutyDefermentStatement" in new Setup {
      override val params: Params = Params("02", "2021", "04", "2021", "DutyDefermentStatement", "1234567")
      val modifiedDoc = histDocRequestSearch.copy(params = params)
      val expectedRequest: Request = Request.apply(modifiedDoc, EmailAddress("Email"), "Company Name")

      expectedRequest.content.head.subject mustBe dutyStatement
      expectedRequest.content.head.body mustBe encodeToUTF8Charsets(DutyDefermentBody)
    }

    "return C79Certificate" in new Setup {
      override val params: Params = Params("02", "2021", "04", "2021", "C79Certificate", "1234567")
      val modifiedDoc = histDocRequestSearch.copy(params = params)
      val expectedRequest: Request = Request.apply(modifiedDoc, EmailAddress("Email"), "Company Name")

      expectedRequest.content.head.subject mustBe c79cert
      expectedRequest.content.head.body mustBe encodeToUTF8Charsets(C79CertificateBody)
    }

    "return SecurityStatement" in new Setup {
      override val params: Params = Params("02", "2021", "04", "2021", "SecurityStatement", "1234567")
      val modifiedDoc = histDocRequestSearch.copy(params = params)
      val expectedRequest: Request = Request.apply(modifiedDoc, EmailAddress("Email"), "Company Name")

      expectedRequest.content.head.subject mustBe sercStatement
      expectedRequest.content.head.body mustBe encodeToUTF8Charsets(SecurityBody)
    }

    "return PostponedVATStatement" in new Setup {
      override val params: Params = Params("02", "2021", "04", "2021", "PostponedVATStatement", "1234567")
      val modifiedDoc = histDocRequestSearch.copy(params = params)
      val expectedRequest: Request = Request.apply(modifiedDoc, EmailAddress("Email"), "Company Name")

      expectedRequest.content.head.subject mustBe PostPonedVATStatement
      expectedRequest.content.head.body mustBe encodeToUTF8Charsets(PostponedVATBody)
    }

    "return eng and cy in list" in new Setup {

      val contents: List[Content] = List(
        Content("en", AccountType("DutyDefermentStatement"), DutyDefermentBody),
        Content("cy", AccountType("DutyDefermentStatement"), DutyDefermentBody))

      val expectedRequest: Request = Request(externalRef = ExternalReference(searchID.toString, "mdtp"),
        recipient = Recipient(
          regime = "cds",
          taxIdentifier = TaxIdentifier("HMRC-CUS-ORG", currentEori),
          fullName = "Company Name",
          email = "test@test.com"),
        tags = Tags("CDS Financials"),
        content = contents,
        messageType = "newMessageAlert",
        validFrom = LocalDate.now().toString,
        alertQueue = "DEFAULT")

      expectedRequest.content.length mustBe 2
      expectedRequest.content mustBe TestContents
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

  val searchRequests: Set[SearchRequest] = Set(
    SearchRequest("GB123456789012", "5b89895-f0da-4472-af5a-d84d340e7mn5",
      SearchResultStatus.inProcess, emptyString, emptyString, 0),
    SearchRequest("GB234567890121", "5c79895-f0da-4472-af5a-d84d340e7mn6",
      SearchResultStatus.inProcess, emptyString, emptyString, 0))

  val TestContents = {
    List(secureMessage.Content("en", AccountType("DutyDefermentStatement"), DutyDefermentBody),
      secureMessage.Content("cy", AccountType("DutyDefermentStatement"), DutyDefermentBody))}

  val dutyStatement = AccountType("DutyDefermentStatement")
  val c79cert = AccountType("C79Certificate")
  val sercStatement = AccountType("SecurityStatement")
  val PostPonedVATStatement = AccountType("PostponedVATStatement")

  val histDocRequestSearch: HistoricDocumentRequestSearch =
    HistoricDocumentRequestSearch(searchID,
      resultsFound,
      searchStatusUpdateDate,
      currentEori,
      params,
      searchRequests)

  def requestJsValue =
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
