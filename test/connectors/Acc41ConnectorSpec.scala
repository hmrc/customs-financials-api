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

import config.MetaConfig.Platform.{MDTP, REGIME_CDS}
import domain.acc41.*
import domain.{Acc41ErrorResponse, Acc41Response, AuthoritiesCsvGenerationResponse}
import models.EORI
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import play.api.{Application, Configuration}
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.Helpers.*
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.http.client.{HttpClientV2, RequestBuilder}
import utils.{SpecBase, WireMockSupportProvider}
import com.typesafe.config.ConfigFactory
import play.api.libs.json.Json
import com.github.tomakehurst.wiremock.client.WireMock.{equalTo, matchingJsonPath, ok, post, urlPathMatching}
import com.github.tomakehurst.wiremock.http.RequestMethod.POST
import config.MetaConfig.Platform.MDTP
import utils.TestData.ERROR_MSG
import utils.Utils.emptyString
import utils.TestData.EORI_VALUE_1

import scala.concurrent.Future

class Acc41ConnectorSpec extends SpecBase with WireMockSupportProvider {

  "initiateAuthoritiesCSV" should {

    "return Left Acc41ErrorResponse when request returns error message" in new Setup {

      wireMockServer.stubFor(
        post(urlPathMatching(acc41AuthoritiesCsvGenerationEndpointUrl))
          .withHeader(X_FORWARDED_HOST, equalTo(MDTP))
          .withHeader(CONTENT_TYPE, equalTo("application/json"))
          .withHeader(ACCEPT, equalTo("application/json"))
          .withHeader(AUTHORIZATION, equalTo("Bearer test1234567"))
          .withRequestBody(
            matchingJsonPath("$.standingAuthoritiesForEORIRequest[?(@.requestCommon.originatingSystem == 'MDTP')]")
          )
          .withRequestBody(
            matchingJsonPath("$.standingAuthoritiesForEORIRequest[?(@.requestCommon.regime == 'CDS')]")
          )
          .withRequestBody(
            matchingJsonPath("$.standingAuthoritiesForEORIRequest[?(@.requestDetail.requestingEORI == 'someEORI')]")
          )
          .willReturn(
            ok(Json.toJson(StandingAuthoritiesForEORIResponse(response(Some("Request failed"), None))).toString)
          )
      )

      val result: Either[Acc41Response, AuthoritiesCsvGenerationResponse] =
        await(connector.initiateAuthoritiesCSV(EORI(EORI_VALUE_1), Some(EORI(EORI_VALUE_1))))

      result mustBe Left(Acc41ErrorResponse)

      verifyExactlyOneEndPointUrlHit(acc41AuthoritiesCsvGenerationEndpointUrl, POST)
    }

    "return Left Acc41ErrorResponse when POST api call throws exception" in new Setup {
      val mockHttpClient: HttpClientV2   = mock[HttpClientV2]
      val requestBuilder: RequestBuilder = mock[RequestBuilder]

      val application: Application = GuiceApplicationBuilder()
        .overrides(
          bind[HttpClientV2].toInstance(mockHttpClient),
          bind[RequestBuilder].toInstance(requestBuilder)
        )
        .configure(
          "microservice.metrics.enabled" -> false,
          "metrics.enabled"              -> false,
          "auditing.enabled"             -> false
        )
        .build()

      when(requestBuilder.withBody(any())(any(), any(), any())).thenReturn(requestBuilder)
      when(requestBuilder.setHeader(any[(String, String)]())).thenReturn(requestBuilder)
      when(mockHttpClient.post(any)(any)).thenReturn(requestBuilder)
      when(requestBuilder.execute(any, any)).thenReturn(Future.failed(new RuntimeException(ERROR_MSG)))

      running(application) {
        val result = await(connector.initiateAuthoritiesCSV(EORI("someEori"), Some(EORI("someAltEori"))))
        result mustBe Left(Acc41ErrorResponse)
      }
    }

    "return Right AuthoritiesCsvGeneration when no alternateEORI" in new Setup {

      wireMockServer.stubFor(
        post(urlPathMatching(acc41AuthoritiesCsvGenerationEndpointUrl))
          .withHeader(X_FORWARDED_HOST, equalTo(MDTP))
          .withHeader(CONTENT_TYPE, equalTo("application/json"))
          .withHeader(ACCEPT, equalTo("application/json"))
          .withHeader(AUTHORIZATION, equalTo("Bearer test1234567"))
          .withRequestBody(
            matchingJsonPath("$.standingAuthoritiesForEORIRequest[?(@.requestCommon.originatingSystem == 'MDTP')]")
          )
          .withRequestBody(
            matchingJsonPath("$.standingAuthoritiesForEORIRequest[?(@.requestCommon.regime == 'CDS')]")
          )
          .withRequestBody(
            matchingJsonPath("$.standingAuthoritiesForEORIRequest[?(@.requestDetail.requestingEORI == 'someEORI')]")
          )
          .willReturn(
            ok(Json.toJson(StandingAuthoritiesForEORIResponse(response(None, Some("020-06-09T21:59:56Z")))).toString)
          )
      )

      val result: Either[Acc41Response, AuthoritiesCsvGenerationResponse] =
        await(connector.initiateAuthoritiesCSV(EORI(EORI_VALUE_1), Some(EORI(emptyString))))

      result mustBe Right(AuthoritiesCsvGenerationResponse(Some("020-06-09T21:59:56Z")))

      verifyExactlyOneEndPointUrlHit(acc41AuthoritiesCsvGenerationEndpointUrl, POST)
    }

    "return Right AuthoritiesCsvGeneration when successful response containing a requestAcceptedDate" in new Setup {

      wireMockServer.stubFor(
        post(urlPathMatching(acc41AuthoritiesCsvGenerationEndpointUrl))
          .withHeader(X_FORWARDED_HOST, equalTo(MDTP))
          .withHeader(CONTENT_TYPE, equalTo("application/json"))
          .withHeader(ACCEPT, equalTo("application/json"))
          .withHeader(AUTHORIZATION, equalTo("Bearer test1234567"))
          .withRequestBody(
            matchingJsonPath("$.standingAuthoritiesForEORIRequest[?(@.requestCommon.originatingSystem == 'MDTP')]")
          )
          .withRequestBody(
            matchingJsonPath("$.standingAuthoritiesForEORIRequest[?(@.requestCommon.regime == 'CDS')]")
          )
          .withRequestBody(
            matchingJsonPath("$.standingAuthoritiesForEORIRequest[?(@.requestDetail.requestingEORI == 'someEORI')]")
          )
          .withRequestBody(
            matchingJsonPath("$.standingAuthoritiesForEORIRequest[?(@.requestDetail.alternateEORI == 'someAltEori')]")
          )
          .willReturn(
            ok(Json.toJson(StandingAuthoritiesForEORIResponse(response(None, Some("020-06-09T21:59:56Z")))).toString)
          )
      )

      val result: Either[Acc41Response, AuthoritiesCsvGenerationResponse] =
        await(connector.initiateAuthoritiesCSV(EORI(EORI_VALUE_1), Some(EORI("someAltEori"))))

      result mustBe Right(AuthoritiesCsvGenerationResponse(Some("020-06-09T21:59:56Z")))

      verifyExactlyOneEndPointUrlHit(acc41AuthoritiesCsvGenerationEndpointUrl, POST)
    }
  }

  override def config: Configuration = Configuration(
    ConfigFactory.parseString(
      s"""
         |microservice {
         |  services {
         |  acc41 {
         |            host = $wireMockHost
         |            port = $wireMockPort
         |        }
         |  }
         |}
         |""".stripMargin
    )
  )

  trait Setup {
    implicit val hc: HeaderCarrier               = HeaderCarrier()
    val acc41AuthoritiesCsvGenerationEndpointUrl =
      "/customs-financials-hods-stub/accounts/requeststandingauthorities/v1"

    def response(error: Option[String], requestAcceptedDate: Option[String]): domain.acc41.Response =
      domain.acc41.Response(
        RequestCommon("date", MDTP, "reference", REGIME_CDS),
        RequestDetail(EORI("someEORI"), Some(EORI("someAltEori"))),
        ResponseDetail(
          errorMessage = error,
          requestAcceptedDate = requestAcceptedDate
        )
      )

    val app: Application          = application().configure(config).build()
    val connector: Acc41Connector = app.injector.instanceOf[Acc41Connector]
  }
}
