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

package connectors

import java.time.LocalDate
import java.util.UUID

import domain.SecureMessage
import domain.SecureMessage.Response
import models.{AccountType, EORI, HistoricDocumentRequestSearch, Params, SearchRequest, SearchResultStatus}
import play.api.Application
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.Helpers.running
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient}
import utils.SpecBase
import utils.Utils.emptyString


class SecureMessageConnectorSpec extends SpecBase {

  "SecureMessageConnector" should {
    "Populate RequestCommon" in new Setup {

      val request = SecureMessage.Request(
        externalRef = SecureMessage.ExternalReference("123123123", "mdtp"),
        recipient = SecureMessage.Recipient("CDS Financials",
          SecureMessage.TaxIdentifier("HMRC-CUS-ORG", "123123123")),
        params = SecureMessage.Params(LocalDate.now(), LocalDate.now(), "Financials"),
        email = "email@email.com",
        tags = SecureMessage.Tags("CDS Financials"),
        content = TestContents,
        messageType = "newMessageAlert",
        validForm = LocalDate.now().toString(),
        alertQueue = "DEFAULT"
      )

      request mustBe compareRequest
    }

    "getSubjetHeader" should {
      "return DutyDefermentStatement" in new Setup {
        running(app) {
          val result = connector.getSubjectHeader("DutyDefermentStatement")
          result mustBe dutyStatement
        }
      }

      "return C79Certificate" in new Setup {
        running(app) {
          val result = connector.getSubjectHeader("C79Certificate")
          result mustBe c79cert
        }
      }

      "return SecurityStatement" in new Setup {
        running(app) {
          val result = connector.getSubjectHeader("SecurityStatement")
          result mustBe sercStatement
        }
      }

      "return PostponedVATStatement" in new Setup {
        running(app) {
          val result = connector.getSubjectHeader("PostponedVATStatement")
          result mustBe PostPonedVATStatement
        }
      }

      "getContents" should {
        "return eng and cy in list" in new Setup {
          running(app) {
            val result = connector.getContents(dutyStatement)
            result mustBe TestContents
          }
        }
      }
    }

    "getCommonRequest" should {
      "return the secure message common request" in new Setup {
        running(app) {
          val result = connector.getRequest("123123123", TestContents)
          result mustBe compareRequest
        }
      }
    }

    "sendSecureMessage" should {
      "successfully post httpclient" in new Setup {
        running(app) {
         val result = await(connector.sendSecureMessage(histDoc = doc))
         result mustBe Response("123123123")
        }
      }
    }
  }

  trait Setup {

    implicit val hc: HeaderCarrier = HeaderCarrier()
    val mockHttpClient: HttpClient = mock[HttpClient]

    val alert = "DEFAULT"
    val mType = "newMessageAlert"
    val eori: EORI = EORI("123123123")
    val dutyStatement = AccountType("DutyDefermentStatement")
    val c79cert = AccountType("C79Certificate")
    val sercStatement = AccountType("SecurityStatement")
    val PostPonedVATStatement = AccountType("PostponedVATStatement")

    val TestContents = {
      List(SecureMessage.Content("en", AccountType("DutyDefermentStatement"), SecureMessage.SecureMessage.body),
        SecureMessage.Content("cy", AccountType("DutyDefermentStatement"), SecureMessage.SecureMessage.body))}

    val searchID: UUID = UUID.randomUUID()
    val params: Params = Params("02", "2021", "04", "2021", "DutyDefermentStatement", "123123123")

    val searchRequests: Set[SearchRequest] = Set(
      SearchRequest("GB123456789012", "5b89895-f0da-4472-af5a-d84d340e7mn5",
        SearchResultStatus.inProcess, emptyString, emptyString, 0),
      SearchRequest("GB234567890121", "5c79895-f0da-4472-af5a-d84d340e7mn6",
        SearchResultStatus.inProcess, emptyString, emptyString, 0))

    val doc: HistoricDocumentRequestSearch = HistoricDocumentRequestSearch(searchID,
      SearchResultStatus.no,"","123123123", params, searchRequests)

    val compareRequest = SecureMessage.Request(
      externalRef = SecureMessage.ExternalReference("123123123", "mdtp"),
      recipient = SecureMessage.Recipient("CDS Financials",
        SecureMessage.TaxIdentifier("HMRC-CUS-ORG", "123123123")),
      params = SecureMessage.Params(LocalDate.now(), LocalDate.now(), "Financials"),
      email = "email@email.com",
      tags = SecureMessage.Tags("CDS Financials"),
      content = TestContents,
      messageType = mType,
      validForm = LocalDate.now().toString(),
      alertQueue = alert
    )

    val response: SecureMessage.Response = SecureMessage.Response("123123123")

    val app: Application = GuiceApplicationBuilder().overrides(
      bind[HttpClient].toInstance(mockHttpClient)
    ).configure(
      "microservice.metrics.enabled" -> false,
      "metrics.enabled" -> false,
      "auditing.enabled" -> false
    ).build()

    val connector: SecureMessageConnector = app.injector.instanceOf[SecureMessageConnector]
  }
}
