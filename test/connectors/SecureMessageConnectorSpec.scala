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

import config.MetaConfig.Platform.{ENROLMENT_KEY, SOURCE_MDTP}
import domain.secureMessage
import domain.secureMessage.*
import models.*
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import play.api.inject.bind
import play.api.test.Helpers.running
import play.api.{Application, Configuration, inject}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.http.client.{HttpClientV2, RequestBuilder}
import utils.{SpecBase, WireMockSupportProvider}
import utils.TestData.{COUNTRY_CODE_GB, ERROR_MSG, REGIME, TEST_EMAIL}
import utils.Utils.emptyString
import com.typesafe.config.ConfigFactory
import play.api.libs.json.Json
import com.github.tomakehurst.wiremock.client.WireMock.{equalTo, matchingJsonPath, ok, post, urlPathMatching}
import com.github.tomakehurst.wiremock.http.RequestMethod.POST
import config.MetaConfig.Platform.MDTP

import java.time.LocalDate
import java.util.UUID
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class SecureMessageConnectorSpec extends SpecBase with WireMockSupportProvider {

  "SecureMessageConnector" should {
    "Populate Request" in new Setup {

      val request: Request = Request(
        externalRef = ExternalReference(searchID.toString, SOURCE_MDTP),
        recipient = Recipient(
          REGIME,
          TaxIdentifier(ENROLMENT_KEY, "GB333186811543"),
          name = Name("Company Name"),
          email = TEST_EMAIL
        ),
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

        when(mockDataStoreService.getCompanyName(any)(any))
          .thenReturn(Future.successful(Option("test")))

        when(mockDataStoreService.getVerifiedEmail(any)(any))
          .thenReturn(Future.successful(Option(EmailAddress("email"))))

        running(app) {
          connector.sendSecureMessage(histDoc = doc).map { result =>
            result mustBe Right(Response(eori.value))
          }
        }
      }

      "successfully post httpclient when getCompanyName call fails and verified email has empty values" in new Setup {

        when(mockDataStoreService.getCompanyName(any)(any))
          .thenReturn(Future.failed(new RuntimeException(ERROR_MSG)))

        when(mockDataStoreService.getVerifiedEmail(any)(any)).thenReturn(Future.successful(None))

        wireMockServer.stubFor(
          post(urlPathMatching(secureMessageEndpointUrl))
            .withHeader(X_FORWARDED_HOST, equalTo(MDTP))
            .withHeader(CONTENT_TYPE, equalTo("application/json"))
            .withHeader(ACCEPT, equalTo("application/json"))
            .withHeader(AUTHORIZATION, equalTo(AUTH_BEARER_TOKEN_VALUE))
            .withRequestBody(
              matchingJsonPath("$.recipient[?(@.regime == 'cds')]")
            )
            .withRequestBody(
              matchingJsonPath("$.recipient[?(@.taxIdentifier.name == 'HMRC-CUS-ORG')]")
            )
            .withRequestBody(
              matchingJsonPath("$.tags[?(@.notificationType == 'CDS Financials')]")
            )
            .willReturn(ok(Json.toJson(response).toString))
        )

        val result: Either[String, Response] = await(connector.sendSecureMessage(histDoc = doc))
        result mustBe Right(Response(eori.value))

        verifyEndPointUrlHit(secureMessageEndpointUrl, POST)
      }

      "return error response when exception occurs while getting VerifiedEmail" in new Setup {

        when(mockDataStoreService.getCompanyName(any)(any)).thenReturn(Future.successful(None))

        when(mockDataStoreService.getVerifiedEmail(any)(any))
          .thenReturn(Future.failed(new RuntimeException(ERROR_MSG)))

        running(app) {
          connector.sendSecureMessage(histDoc = doc).map { result =>
            result mustBe Left(ERROR_MSG)
          }
        }
      }

      "Json Writes result in correct output" in new Setup {
        Json.toJson(compareRequest) mustBe Json.parse(jsValue)
      }
    }
  }

  override def config: Configuration = Configuration(
    ConfigFactory.parseString(
      s"""
         |microservice {
         |  services {
         |  secureMessage {
         |            host = $wireMockHost
         |            port = $wireMockPort
         |        }
         |  }
         |}
         |""".stripMargin
    )
  )

  trait Setup {
    implicit val hc: HeaderCarrier       = HeaderCarrier()
    val secureMessageEndpointUrl: String = "/secure-messaging/v4/message"

    val mockHttpClient: HttpClientV2   = mock[HttpClientV2]
    val requestBuilder: RequestBuilder = mock[RequestBuilder]
    val eori: EORI                     = EORI("GB333186811543")
    val id: String                     = "abcd12345"

    val address: AddressInformation = AddressInformation(
      streetAndNumber = "street&Number",
      city = "london",
      postalCode = Option("Post"),
      countryCode = COUNTRY_CODE_GB
    )

    val corp: CompanyInformation = CompanyInformation(name = "Company Name", consent = "Yes", address = address)

    val searchID: UUID = UUID.randomUUID()
    val params: Params = Params("01", "2022", "01", "2023", "DutyDefermentStatement", "abcd12345")

    val searchRequests: Set[SearchRequest] = Set(
      SearchRequest(
        "GB123456789012",
        "5b89895-f0da-4472-af5a-d84d340e7mn5",
        SearchResultStatus.inProcess,
        emptyString,
        emptyString,
        0
      ),
      SearchRequest(
        "GB234567890121",
        "5c79895-f0da-4472-af5a-d84d340e7mn6",
        SearchResultStatus.inProcess,
        emptyString,
        emptyString,
        0
      )
    )

    val doc: HistoricDocumentRequestSearch =
      HistoricDocumentRequestSearch(searchID, SearchResultStatus.no, emptyString, eori.value, params, searchRequests)

    val TestContents: List[Content] =
      List(
        secureMessage.Content("en", "DutyDefermentStatement", "Message content - 4254101384174917141"),
        secureMessage.Content("cy", "DutyDefermentStatement", "Cynnwys - 4254101384174917141")
      )

    val compareRequest: Request = secureMessage.Request(
      externalRef = secureMessage.ExternalReference(searchID.toString, SOURCE_MDTP),
      recipient = secureMessage.Recipient(
        REGIME,
        secureMessage.TaxIdentifier(ENROLMENT_KEY, eori.value),
        name = Name("Company Name"),
        email = TEST_EMAIL
      ),
      tags = secureMessage.Tags("CDS Financials"),
      content = TestContents,
      messageType = "newMessageAlert",
      validFrom = LocalDate.now().toString,
      alertQueue = "DEFAULT"
    )

    val jsValue: String =
      s"""{"externalRef": {
         |"id": "$searchID",
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

    val response: secureMessage.Response         = secureMessage.Response("GB333186811543")
    val mockDataStoreService: DataStoreConnector = mock[DataStoreConnector]

    val app: Application = application()
      .overrides(inject.bind[DataStoreConnector].toInstance(mockDataStoreService))
      .configure(config)
      .build()

    val connector: SecureMessageConnector = app.injector.instanceOf[SecureMessageConnector]
  }
}
