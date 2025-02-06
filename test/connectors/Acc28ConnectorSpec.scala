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

import models.requests.{GuaranteeAccountTransactionsRequest, RequestCommon}
import models.requests.*
import models.{AccountNumber, ErrorResponse, ExceededThresholdErrorException, NoAssociatedDataException}
import play.api.{Application, Configuration}
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.Helpers.*
import uk.gov.hmrc.http.HeaderCarrier
import utils.{SpecBase, WireMockSupportProvider}
import com.github.tomakehurst.wiremock.client.WireMock.{equalTo, matchingJsonPath, ok, post, urlPathMatching}
import com.github.tomakehurst.wiremock.http.RequestMethod.POST
import com.typesafe.config.ConfigFactory
import config.AppConfig
import config.MetaConfig.Platform.MDTP
import play.api.libs.json.Json
import models.responses.{
  GetGGATransactionResponse, GuaranteeTransactionDeclaration, GuaranteeTransactionsResponse, ResponseCommon,
  ResponseDetail
}

import java.time.LocalDate
import scala.concurrent.Future

class Acc28ConnectorSpec extends SpecBase with WireMockSupportProvider {

  "retrieveGuaranteeTransactions" should {
    "return a list of declarations on a successful response" in new Setup {

      wireMockServer.stubFor(
        post(urlPathMatching(retrieveGuaranteeTransactionsUrl))
          .withHeader(X_FORWARDED_HOST, equalTo(MDTP))
          .withHeader(CONTENT_TYPE, equalTo("application/json"))
          .withHeader(ACCEPT, equalTo("application/json"))
          .withHeader(AUTHORIZATION, equalTo("Bearer test1234567"))
          .withRequestBody(
            matchingJsonPath("$.getGGATransactionListing[?(@.requestCommon.requestParameters.paramName == 'REGIME')]")
          )
          .withRequestBody(
            matchingJsonPath("$.getGGATransactionListing[?(@.requestCommon.requestParameters.paramValue == 'CDS')]")
          )
          .withRequestBody(matchingJsonPath("$.getGGATransactionListing[?(@.requestDetail.gan == 'GAN')]"))
          .willReturn(ok(Json.toJson(response).toString))
      )

      val result: Either[ErrorResponse, Seq[GuaranteeTransactionDeclaration]] =
        await(connector.retrieveGuaranteeTransactions(request))

      result mustBe Right(List.empty)

      verifyEndPointUrlHit(retrieveGuaranteeTransactionsUrl, POST)
    }

    "return NoAssociatedData error response when responded with no associated data" in new Setup {

      wireMockServer.stubFor(
        post(urlPathMatching(retrieveGuaranteeTransactionsUrl))
          .withHeader(X_FORWARDED_HOST, equalTo(MDTP))
          .withHeader(CONTENT_TYPE, equalTo("application/json"))
          .withHeader(ACCEPT, equalTo("application/json"))
          .withHeader(AUTHORIZATION, equalTo("Bearer test1234567"))
          .withRequestBody(
            matchingJsonPath("$.getGGATransactionListing[?(@.requestCommon.requestParameters.paramName == 'REGIME')]")
          )
          .withRequestBody(
            matchingJsonPath("$.getGGATransactionListing[?(@.requestCommon.requestParameters.paramValue == 'CDS')]")
          )
          .withRequestBody(matchingJsonPath("$.getGGATransactionListing[?(@.requestDetail.gan == 'GAN')]"))
          .willReturn(ok(Json.toJson(noDataResponse).toString))
      )

      val result: Either[ErrorResponse, Seq[GuaranteeTransactionDeclaration]] =
        await(connector.retrieveGuaranteeTransactions(request))

      result mustBe Left(NoAssociatedDataException)

      verifyEndPointUrlHit(retrieveGuaranteeTransactionsUrl, POST)
    }

    "return ExceededThreshold error response when responded with exceeded threshold" in new Setup {

      wireMockServer.stubFor(
        post(urlPathMatching(retrieveGuaranteeTransactionsUrl))
          .withHeader(X_FORWARDED_HOST, equalTo(MDTP))
          .withHeader(CONTENT_TYPE, equalTo("application/json"))
          .withHeader(ACCEPT, equalTo("application/json"))
          .withHeader(AUTHORIZATION, equalTo("Bearer test1234567"))
          .withRequestBody(
            matchingJsonPath("$.getGGATransactionListing[?(@.requestCommon.requestParameters.paramName == 'REGIME')]")
          )
          .withRequestBody(
            matchingJsonPath("$.getGGATransactionListing[?(@.requestCommon.requestParameters.paramValue == 'CDS')]")
          )
          .withRequestBody(matchingJsonPath("$.getGGATransactionListing[?(@.requestDetail.gan == 'GAN')]"))
          .willReturn(ok(Json.toJson(tooMuchDataRequestedResponse).toString))
      )

      val result: Either[ErrorResponse, Seq[GuaranteeTransactionDeclaration]] =
        await(connector.retrieveGuaranteeTransactions(request))

      result mustBe Left(ExceededThresholdErrorException)

      verifyEndPointUrlHit(retrieveGuaranteeTransactionsUrl, POST)
    }
  }

  override def config: Configuration = Configuration(
    ConfigFactory.parseString(
      s"""
         |microservice {
         |  services {
         |  acc28 {
         |            host = $wireMockHost
         |            port = $wireMockPort
         |            context-base = "/customs-financials-hods-stub"
         |            bearer-token = "test1234567"
         |            serviceName="hods-acc28"
         |        }
         |  }
         |}
         |""".stripMargin
    )
  )

  trait Setup {
    implicit val hc: HeaderCarrier = HeaderCarrier()

    val retrieveGuaranteeTransactionsUrl = "/customs-financials-hods-stub/accounts/getggatransactionlisting/v1"

    val request: GuaranteeAccountTransactionsRequest = GuaranteeAccountTransactionsRequest(
      AccountNumber("GAN"),
      openItems = Some(false),
      None
    )

    val response: GuaranteeTransactionsResponse = GuaranteeTransactionsResponse(
      GetGGATransactionResponse(
        ResponseCommon("OK", None, LocalDate.now().toString),
        Some(ResponseDetail(openItems = true, Seq.empty))
      )
    )

    val noDataResponse: GuaranteeTransactionsResponse = GuaranteeTransactionsResponse(
      GetGGATransactionResponse(
        ResponseCommon("OK", Some("025-No associated data found"), LocalDate.now().toString),
        Some(ResponseDetail(openItems = true, Seq.empty))
      )
    )

    val tooMuchDataRequestedResponse: GuaranteeTransactionsResponse = GuaranteeTransactionsResponse(
      GetGGATransactionResponse(
        ResponseCommon(
          "OK",
          Some("091-The query has exceeded the threshold, please refine the search"),
          LocalDate.now().toString
        ),
        Some(ResponseDetail(openItems = true, Seq.empty))
      )
    )

    val app: Application = application().configure(config).build()

    val connector: Acc28Connector = app.injector.instanceOf[Acc28Connector]
  }
}
