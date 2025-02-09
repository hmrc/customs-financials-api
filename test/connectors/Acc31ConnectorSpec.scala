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

import models.responses.{
  CashTransactionsResponse, CashTransactionsResponseCommon, CashTransactionsResponseDetail,
  GetCashAccountTransactionListingResponse
}
import models.{ErrorResponse, ExceededThresholdErrorException, NoAssociatedDataException}
import play.api.{Application, Configuration}
import play.api.test.Helpers.*
import uk.gov.hmrc.http.HeaderCarrier
import utils.{SpecBase, WireMockSupportProvider}
import play.api.libs.json.Json
import com.github.tomakehurst.wiremock.client.WireMock.{equalTo, matchingJsonPath, ok, post, urlPathMatching}
import com.github.tomakehurst.wiremock.http.RequestMethod.POST
import com.typesafe.config.ConfigFactory
import config.MetaConfig.Platform.MDTP

import java.time.LocalDate

class Acc31ConnectorSpec extends SpecBase with WireMockSupportProvider {

  "retrieveCashTransactions" should {

    "return a list of declarations on a successful response" in new Setup {

      wireMockServer.stubFor(
        post(urlPathMatching(acc31GetCashAccountTransactionListingEndpointUrl))
          .withHeader(X_FORWARDED_HOST, equalTo(MDTP))
          .withHeader(CONTENT_TYPE, equalTo(CONTENT_TYPE_APPLICATION_JSON))
          .withHeader(ACCEPT, equalTo(CONTENT_TYPE_APPLICATION_JSON))
          .withHeader(AUTHORIZATION, equalTo(AUTH_BEARER_TOKEN_VALUE))
          .withRequestBody(
            matchingJsonPath(
              "$.getCashAccountTransactionListingRequest[?(@.requestCommon.originatingSystem == 'MDTP')]"
            )
          )
          .withRequestBody(
            matchingJsonPath("$.getCashAccountTransactionListingRequest[?(@.requestDetail.CAN == 'can')]")
          )
          .willReturn(ok(Json.toJson(response).toString))
      )

      val result: Either[ErrorResponse, Option[CashTransactionsResponseDetail]] =
        await(connector.retrieveCashTransactions("can", LocalDate.now(), LocalDate.now()))

      result mustBe Right(Some(CashTransactionsResponseDetail(None, None, None)))

      verifyExactlyOneEndPointUrlHit(acc31GetCashAccountTransactionListingEndpointUrl, POST)
    }

    "return NoAssociatedData error response when responded with no associated data" in new Setup {

      wireMockServer.stubFor(
        post(urlPathMatching(acc31GetCashAccountTransactionListingEndpointUrl))
          .withHeader(X_FORWARDED_HOST, equalTo(MDTP))
          .withHeader(CONTENT_TYPE, equalTo(CONTENT_TYPE_APPLICATION_JSON))
          .withHeader(ACCEPT, equalTo(CONTENT_TYPE_APPLICATION_JSON))
          .withHeader(AUTHORIZATION, equalTo(AUTH_BEARER_TOKEN_VALUE))
          .withRequestBody(
            matchingJsonPath(
              "$.getCashAccountTransactionListingRequest[?(@.requestCommon.originatingSystem == 'MDTP')]"
            )
          )
          .withRequestBody(
            matchingJsonPath("$.getCashAccountTransactionListingRequest[?(@.requestDetail.CAN == 'can')]")
          )
          .willReturn(ok(Json.toJson(noDataResponse).toString))
      )

      val result: Either[ErrorResponse, Option[CashTransactionsResponseDetail]] =
        await(connector.retrieveCashTransactions("can", LocalDate.now(), LocalDate.now()))

      result mustBe Left(NoAssociatedDataException)

      verifyExactlyOneEndPointUrlHit(acc31GetCashAccountTransactionListingEndpointUrl, POST)
    }

    "return NoAssociatedData error response when responded with no associated data " +
      "and maxTransactionsExceeded field is set to true" in new Setup {

        wireMockServer.stubFor(
          post(urlPathMatching(acc31GetCashAccountTransactionListingEndpointUrl))
            .withHeader(X_FORWARDED_HOST, equalTo(MDTP))
            .withHeader(CONTENT_TYPE, equalTo(CONTENT_TYPE_APPLICATION_JSON))
            .withHeader(ACCEPT, equalTo(CONTENT_TYPE_APPLICATION_JSON))
            .withHeader(AUTHORIZATION, equalTo(AUTH_BEARER_TOKEN_VALUE))
            .withRequestBody(
              matchingJsonPath(
                "$.getCashAccountTransactionListingRequest[?(@.requestCommon.originatingSystem == 'MDTP')]"
              )
            )
            .withRequestBody(
              matchingJsonPath("$.getCashAccountTransactionListingRequest[?(@.requestDetail.CAN == 'can')]")
            )
            .willReturn(ok(Json.toJson(noDataResponse02).toString))
        )

        val result: Either[ErrorResponse, Option[CashTransactionsResponseDetail]] =
          await(connector.retrieveCashTransactions("can", LocalDate.now(), LocalDate.now()))

        result mustBe Left(NoAssociatedDataException)

        verifyExactlyOneEndPointUrlHit(acc31GetCashAccountTransactionListingEndpointUrl, POST)
      }

    "return ExceededThreshold error response when responded with exceeded threshold" in new Setup {

      wireMockServer.stubFor(
        post(urlPathMatching(acc31GetCashAccountTransactionListingEndpointUrl))
          .withHeader(X_FORWARDED_HOST, equalTo(MDTP))
          .withHeader(CONTENT_TYPE, equalTo(CONTENT_TYPE_APPLICATION_JSON))
          .withHeader(ACCEPT, equalTo(CONTENT_TYPE_APPLICATION_JSON))
          .withHeader(AUTHORIZATION, equalTo(AUTH_BEARER_TOKEN_VALUE))
          .withRequestBody(
            matchingJsonPath(
              "$.getCashAccountTransactionListingRequest[?(@.requestCommon.originatingSystem == 'MDTP')]"
            )
          )
          .withRequestBody(
            matchingJsonPath("$.getCashAccountTransactionListingRequest[?(@.requestDetail.CAN == 'can')]")
          )
          .willReturn(ok(Json.toJson(tooMuchDataRequestedResponse).toString))
      )

      val result: Either[ErrorResponse, Option[CashTransactionsResponseDetail]] =
        await(connector.retrieveCashTransactions("can", LocalDate.now(), LocalDate.now()))

      result mustBe Left(ExceededThresholdErrorException)

      verifyExactlyOneEndPointUrlHit(acc31GetCashAccountTransactionListingEndpointUrl, POST)
    }

    "return ExceededThreshold error response when responded with exceeded threshold " +
      "and maxTransactionsExceeded field is set to false" in new Setup {

        wireMockServer.stubFor(
          post(urlPathMatching(acc31GetCashAccountTransactionListingEndpointUrl))
            .withHeader(X_FORWARDED_HOST, equalTo(MDTP))
            .withHeader(CONTENT_TYPE, equalTo(CONTENT_TYPE_APPLICATION_JSON))
            .withHeader(ACCEPT, equalTo(CONTENT_TYPE_APPLICATION_JSON))
            .withHeader(AUTHORIZATION, equalTo(AUTH_BEARER_TOKEN_VALUE))
            .withRequestBody(
              matchingJsonPath(
                "$.getCashAccountTransactionListingRequest[?(@.requestCommon.originatingSystem == 'MDTP')]"
              )
            )
            .withRequestBody(
              matchingJsonPath("$.getCashAccountTransactionListingRequest[?(@.requestDetail.CAN == 'can')]")
            )
            .willReturn(ok(Json.toJson(tooMuchDataRequestedResponse02).toString))
        )

        val result: Either[ErrorResponse, Option[CashTransactionsResponseDetail]] =
          await(connector.retrieveCashTransactions("can", LocalDate.now(), LocalDate.now()))

        result mustBe Left(ExceededThresholdErrorException)

        verifyExactlyOneEndPointUrlHit(acc31GetCashAccountTransactionListingEndpointUrl, POST)
      }
  }

  override def config: Configuration = Configuration(
    ConfigFactory.parseString(
      s"""
         |microservice {
         |  services {
         |  acc31 {
         |            host = $wireMockHost
         |            port = $wireMockPort
         |        }
         |  }
         |}
         |""".stripMargin
    )
  )

  trait Setup {
    implicit val hc: HeaderCarrier = HeaderCarrier()

    val acc31GetCashAccountTransactionListingEndpointUrl =
      "/customs-financials-hods-stub/accounts/getcashaccounttransactionlisting/v1"

    val noAssociatedDataMessage = "025-No associated data found"
    val exceedsThresholdMessage = "091-The query has exceeded the threshold, please refine the search"

    val response: CashTransactionsResponse = CashTransactionsResponse(
      GetCashAccountTransactionListingResponse(
        CashTransactionsResponseCommon("OK", None, LocalDate.now().toString, None),
        Some(CashTransactionsResponseDetail(None, None, None))
      )
    )

    val noDataResponse: CashTransactionsResponse = CashTransactionsResponse(
      GetCashAccountTransactionListingResponse(
        CashTransactionsResponseCommon("OK", Some(noAssociatedDataMessage), LocalDate.now().toString, None),
        Some(CashTransactionsResponseDetail(None, None, None))
      )
    )

    val noDataResponse02: CashTransactionsResponse = CashTransactionsResponse(
      GetCashAccountTransactionListingResponse(
        CashTransactionsResponseCommon("OK", Some(noAssociatedDataMessage), LocalDate.now().toString),
        Some(CashTransactionsResponseDetail(None, None, None))
      )
    )

    val tooMuchDataRequestedResponse: CashTransactionsResponse = CashTransactionsResponse(
      GetCashAccountTransactionListingResponse(
        CashTransactionsResponseCommon("OK", Some(exceedsThresholdMessage), LocalDate.now().toString, None),
        Some(CashTransactionsResponseDetail(None, None, None))
      )
    )

    val tooMuchDataRequestedResponse02: CashTransactionsResponse = CashTransactionsResponse(
      GetCashAccountTransactionListingResponse(
        CashTransactionsResponseCommon("OK", Some(exceedsThresholdMessage), LocalDate.now().toString),
        Some(CashTransactionsResponseDetail(None, None, None))
      )
    )

    val app: Application          = application().configure(config).build()
    val connector: Acc31Connector = app.injector.instanceOf[Acc31Connector]
  }
}
