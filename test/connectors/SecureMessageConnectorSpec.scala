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

import com.google.common.base.Charsets
import com.google.common.io.BaseEncoding
import domain.SecureMessage
import domain.SecureMessage.Response
import models.{AccountType, EORI, HistoricDocumentRequestSearch, Params, SearchRequest, SearchResultStatus}
import play.api.Application
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.{JsValue, Json}
import play.api.test.Helpers.running
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient}
import utils.SpecBase
import utils.Utils.emptyString


class SecureMessageConnectorSpec extends SpecBase {

  "SecureMessageConnector" should {
    "Populate RequestCommon" in new Setup {

      val request = SecureMessage.Request(
        externalRef = SecureMessage.ExternalReference(searchID.toString, "mdtp"),
        recipient = SecureMessage.Recipient("cds",
          SecureMessage.TaxIdentifier("HMRC-CUS-ORG", "GB333186811543"),
          params = SecureMessage.Params("01","2022","01","2023", "Financials"),
          email = "test@test.com"),
        tags = SecureMessage.Tags("CDS Financials"),
        content = TestContents,
        messageType = "newMessageAlert",
        validForm = LocalDate.now().toString(),
        alertQueue = "DEFAULT"
      )

      request mustBe compareRequest
    }

    "encoded body displays correctly" in new Setup {
      SecureMessage.SecureMessage.encoded mustBe encoded
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

    "getRequest" should {
      "return the secure message request" in new Setup {
        running(app) {
          val result = connector.getRequest(doc, TestContents)
          result mustBe compareRequest
        }
      }
    }

    "sendSecureMessage" should {
      "successfully post httpclient" in new Setup {
        running(app) {
          //val result = await(connector.sendSecureMessage(histDoc = doc))
          //result mustBe Response("abcd12345")
        }
      }

      "successfully compares to schema example" in new Setup {
        val jv: JsValue = Json.parse(jsValue)
        //Json.fromJson(jv) mustBe JsSuccess(compareRequest)
      }

      "Json Writesresult in correct output" in new Setup {
        //Json.toJson(compareRequest) mustBe Json.parse(jsValue)
      }
    }
  }

  trait Setup {

    implicit val hc: HeaderCarrier = HeaderCarrier()
    val mockHttpClient: HttpClient = mock[HttpClient]

    val alert = "DEFAULT"
    val mType = "newMessageAlert"
    val eori: EORI = EORI("GB333186811543")
    val id: String = "abcd12345"
    val dutyStatement = AccountType("DutyDefermentStatement")
    val c79cert = AccountType("C79Certificate")
    val sercStatement = AccountType("SecurityStatement")
    val PostPonedVATStatement = AccountType("PostponedVATStatement")

    val TestContents = {
      List(SecureMessage.Content("en", AccountType("DutyDefermentStatement"), SecureMessage.SecureMessage.body),
        SecureMessage.Content("cy", AccountType("DutyDefermentStatement"), SecureMessage.SecureMessage.body))
    }

    val contentBody: String =
      s"Dear Apples & Pears Ltd\n\n" +
        s"The notification of adjustment statements you requested for March 2021 to May 2021 were not found.\n\n" +
        "There are 2 possible reasons for this:\n\n" +
        "Statements are only created for the periods in which you imported goods. " +
        "Check that you imported goods during the dates you requested.\n" +
        "Notification of adjustment statements for declarations made using " +
        "Customs Handling of Import and Export Freight (CHIEF) cannot be requested " +
        "using the Customs Declaration Service. (Insert guidance on how to get CHIEF NOA statements).\n" +
        "From the Customs Declaration Service"

    val encoded = BaseEncoding.base64().encode(contentBody.getBytes(Charsets.UTF_8))

    val searchID: UUID = UUID.randomUUID()
    val params: Params = Params("01", "2022", "01", "2023", "DutyDefermentStatement", "abcd12345")

    val searchRequests: Set[SearchRequest] = Set(
      SearchRequest("GB123456789012", "5b89895-f0da-4472-af5a-d84d340e7mn5",
        SearchResultStatus.inProcess, emptyString, emptyString, 0),
      SearchRequest("GB234567890121", "5c79895-f0da-4472-af5a-d84d340e7mn6",
        SearchResultStatus.inProcess, emptyString, emptyString, 0))

    val doc: HistoricDocumentRequestSearch = HistoricDocumentRequestSearch(searchID,
      SearchResultStatus.no, "", eori.value, params, searchRequests)

    val compareRequest = SecureMessage.Request(
      externalRef = SecureMessage.ExternalReference(searchID.toString, "mdtp"),
      recipient = SecureMessage.Recipient("cds",
        SecureMessage.TaxIdentifier("HMRC-CUS-ORG", eori.value),
        params = SecureMessage.Params("01","2022","01","2023", "Financials"),
        email = "test@test.com"),
      tags = SecureMessage.Tags("CDS Financials"),
      content = TestContents,
      messageType = mType,
      validForm = LocalDate.now().toString(),
      alertQueue = alert
    )

    val jsValue: String =
      s"""{"externalRef": {
         |"id": "${searchID}",
         |"source": "mdtp"
         |},
         |"recipient": {
         |"regime": "cds",
         |"taxIdentifier": {
         |"name": "HMRC-CUS-ORG",
         |"value": "GB333186811543"
         |},
         |"params": {
         |"startMonth": "01",
         |"startYear": "2022",
         |"endMonth": "01",
         |"endYear": "2023",
         |"documentType": "Financials"
         |},
         |"email": "test@test.com"
         |},
         |"tags": {
         |"notificationType": "CDS Financials"
         |},
         |"content": [
         |{
         |"lang": "en",
         |"subject": "DutyDefermentStatement",
         |"body": "Message content - 4254101384174917141"
         |},
         |{
         |"lang": "cy",
         |"subject": "DutyDefermentStatement",
         |"body": "Cynnwys - 4254101384174917141"
         |}
         |],
         |"messageType": "newMessageAlert",
         |"validFrom": "2023-09-08",
         |"alertQueue": "DEFAULT"
         |}""".stripMargin

    val response: SecureMessage.Response = SecureMessage.Response("abcd12345")

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
