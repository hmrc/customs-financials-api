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

import config.MetaConfig.Platform.SOURCE_MDTP

import java.time.LocalDate
import java.util.UUID
import domain.secureMessage
import domain.secureMessage._
import models._
import play.api.{Application, inject}
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json
import play.api.test.Helpers.running
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient}
import utils.SpecBase
import utils.Utils.emptyString

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class SecureMessageConnectorSpec extends SpecBase {

  "SecureMessageConnector" should {
    "Populate Request" in new Setup {

      val request = Request(
        externalRef = ExternalReference(searchID.toString, SOURCE_MDTP),
        recipient = Recipient("cds",
          TaxIdentifier("HMRC-CUS-ORG", "GB333186811543"),
          name = Name("Company Name"),
          email = "test@test.com"),
        tags = Tags("CDS Financials"),
        content = TestContents,
        messageType = "newMessageAlert",
        validFrom = LocalDate.now().toString,
        alertQueue = "DEFAULT"
      )

      request mustBe compareRequest
    }

    "sendSecureMessage" should {
      "successfully post httpclient" in new Setup {

      //TODO Update this to work and uncomment
      /*when[Future[domain.secureMessage.Response]](mockHttpClient.POST(any, any, any)(any, any, any, any))
          .thenReturn(Future.successful(response))*/

        when(mockDataStoreService.getCompanyName(any)(any))
          .thenReturn(Future.successful(Option("test")))

        when(mockDataStoreService.getVerifiedEmail(any)(any))
          .thenReturn(Future.successful(Option(EmailAddress("email"))))

        running(app) {
          connector.sendSecureMessage(histDoc = doc).map {
            result => result mustBe Right(Response(eori.value))
          }
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
    val eori: EORI = EORI("GB333186811543")
    val id: String = "abcd12345"

    val address: AddressInformation = AddressInformation(
      streetAndNumber = "street&Number",
      city = "london",
      postalCode = Option("Post"),
      countryCode = "GB")

    val corp: CompanyInformation = CompanyInformation(
      name = "Company Name", consent = "Yes", address = address)

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
      List(secureMessage.Content("en", "DutyDefermentStatement",
        "Message content - 4254101384174917141"), secureMessage.Content(
        "cy", "DutyDefermentStatement", "Cynnwys - 4254101384174917141"))
    }

    val compareRequest = secureMessage.Request(
      externalRef = secureMessage.ExternalReference(searchID.toString, SOURCE_MDTP),
      recipient = secureMessage.Recipient("cds",
        secureMessage.TaxIdentifier("HMRC-CUS-ORG", eori.value),
        name = Name("Company Name"),
        email = "test@test.com"),
      tags = secureMessage.Tags("CDS Financials"),
      content = TestContents,
      messageType = "newMessageAlert",
      validFrom = LocalDate.now().toString,
      alertQueue = "DEFAULT")

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
         |"name": {
         |"line1": "Company Name"
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

    val response: secureMessage.Response = secureMessage.Response("GB333186811543")
    val mockDataStoreService: DataStoreConnector = mock[DataStoreConnector]

    val app: Application = GuiceApplicationBuilder().overrides(
      bind[HttpClient].toInstance(mockHttpClient),
      inject.bind[DataStoreConnector].toInstance(mockDataStoreService)
    ).configure(
      "microservice.metrics.enabled" -> false,
      "metrics.enabled" -> false,
      "auditing.enabled" -> false
    ).build()

    val connector: SecureMessageConnector = app.injector.instanceOf[SecureMessageConnector]
  }
}
