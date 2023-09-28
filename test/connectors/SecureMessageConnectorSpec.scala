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
import domain.secureMessage
import domain.secureMessage._
import models.{AccountType, EORI, HistoricDocumentRequestSearch, Params, SearchRequest, SearchResultStatus}
import play.api.Application
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json
import play.api.test.Helpers.running
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient}
import utils.SpecBase
import utils.Utils.emptyString

class SecureMessageConnectorSpec extends SpecBase {

  "SecureMessageConnector" should {
    "Populate RequestCommon" in new Setup {

      val request = Request(
        externalRef = ExternalReference(searchID.toString, "mdtp"),
        recipient = Recipient("cds",
          TaxIdentifier("HMRC-CUS-ORG", "GB333186811543"),
          params = secureMessage.Params("01", "2022", "01", "2023", "Financials"),
          email = "test@test.com"),
        tags = Tags("CDS Financials"),
        content = TestContents,
        messageType = "newMessageAlert",
        validFrom = LocalDate.now().toString,
        alertQueue = "DEFAULT"
      )

      request mustBe compareRequest
    }

    "encoded body displays correctly" in new Setup {
      secureMessage.SecureMessage.encoded mustBe encoded
    }

    "sendSecureMessage" should {
      "successfully post httpclient" in new Setup {
        running(app) {
       //   val result = await(connector.sendSecureMessage(histDoc = doc))
        //  result mustBe Response(eori.value)
        }
      }

      "Json Writesresult in correct output" in new Setup {
        Json.toJson(compareRequest) mustBe Json.parse(jsValue)
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

    val TestContents = {
      List(secureMessage.Content("en", AccountType("DutyDefermentStatement"), "Message content - 4254101384174917141"),
        secureMessage.Content("cy", AccountType("DutyDefermentStatement"), "Cynnwys - 4254101384174917141"))
    }

    val compareRequest = secureMessage.Request(
      externalRef = secureMessage.ExternalReference(searchID.toString, "mdtp"),
      recipient = secureMessage.Recipient("cds",
        secureMessage.TaxIdentifier("HMRC-CUS-ORG", eori.value),
        params = secureMessage.Params("01", "2022", "01", "2023", "Financials"),
        email = "test@test.com"),
      tags = secureMessage.Tags("CDS Financials"),
      content = TestContents,
      messageType = mType,
      validFrom = LocalDate.now().toString,
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
         |"validFrom": "${LocalDate.now().toString}",
         |"alertQueue": "DEFAULT"
         |}""".stripMargin

    val response: secureMessage.Response = secureMessage.Response("abcd12345")

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
