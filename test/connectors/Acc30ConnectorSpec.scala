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

import domain.StandingAuthority
import models.requests.manageAuthorities.*
import models.{AccountNumber, EORI}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import play.api.{Application, Configuration}
import com.typesafe.config.ConfigFactory
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.Helpers.*
import uk.gov.hmrc.http.client.{HttpClientV2, RequestBuilder}
import uk.gov.hmrc.http.{HeaderCarrier, NotFoundException}
import utils.{SpecBase, WireMockSupportProvider}
import utils.Utils.emptyString
import com.github.tomakehurst.wiremock.client.WireMock.{
  equalTo, matchingJsonPath, noContent, ok, post, serverError, urlPathMatching
}
import com.github.tomakehurst.wiremock.http.RequestMethod.POST
import utils.TestData.EORI_VALUE

import java.time.LocalDate
import scala.concurrent.Future
import config.MetaConfig.Platform.MDTP

class Acc30ConnectorSpec extends SpecBase with WireMockSupportProvider {

  "grantAccountAuthorities" should {

    "return true when the api responds with 204" in new Setup {

      wireMockServer.stubFor(
        post(urlPathMatching(acc30ManageAccountAuthoritiesEndpointUrl))
          .withHeader(X_FORWARDED_HOST, equalTo(MDTP))
          .withHeader(CONTENT_TYPE, equalTo("application/json"))
          .withHeader(ACCEPT, equalTo("application/json"))
          .withHeader(AUTHORIZATION, equalTo("Bearer test1234567"))
          .withRequestBody(
            matchingJsonPath("$.manageStandingAuthoritiesRequest[?(@.requestCommon.regime == 'CDS')]")
          )
          .withRequestBody(
            matchingJsonPath("$.manageStandingAuthoritiesRequest[?(@.requestDetail.ownerEori == 'testEORI')]")
          )
          .willReturn(noContent)
      )

      val result: Boolean = await(connector.grantAccountAuthorities(grantRequest, EORI(EORI_VALUE)))
      result mustBe true

      verifyEndPointUrlHit(acc30ManageAccountAuthoritiesEndpointUrl, POST)
    }

    "return false when the api responds with a successful response that isn't 204" in new Setup {

      wireMockServer.stubFor(
        post(urlPathMatching(acc30ManageAccountAuthoritiesEndpointUrl))
          .withHeader(X_FORWARDED_HOST, equalTo(MDTP))
          .withHeader(CONTENT_TYPE, equalTo("application/json"))
          .withHeader(ACCEPT, equalTo("application/json"))
          .withHeader(AUTHORIZATION, equalTo("Bearer test1234567"))
          .withRequestBody(
            matchingJsonPath("$.manageStandingAuthoritiesRequest[?(@.requestCommon.regime == 'CDS')]")
          )
          .withRequestBody(
            matchingJsonPath("$.manageStandingAuthoritiesRequest[?(@.requestDetail.ownerEori == 'testEORI')]")
          )
          .willReturn(ok(emptyString))
      )

      val result: Boolean = await(connector.grantAccountAuthorities(grantRequest, EORI(EORI_VALUE)))
      result mustBe false

      verifyEndPointUrlHit(acc30ManageAccountAuthoritiesEndpointUrl, POST)
    }

    "return false when the api fails" in new Setup {

      wireMockServer.stubFor(
        post(urlPathMatching(acc30ManageAccountAuthoritiesEndpointUrl))
          .withHeader(X_FORWARDED_HOST, equalTo(MDTP))
          .withHeader(CONTENT_TYPE, equalTo("application/json"))
          .withHeader(ACCEPT, equalTo("application/json"))
          .withHeader(AUTHORIZATION, equalTo("Bearer test1234567"))
          .withRequestBody(
            matchingJsonPath("$.manageStandingAuthoritiesRequest[?(@.requestCommon.regime == 'CDS')]")
          )
          .withRequestBody(
            matchingJsonPath("$.manageStandingAuthoritiesRequest[?(@.requestDetail.ownerEori == 'testEORI')]")
          )
          .willReturn(serverError)
      )

      val result: Boolean = await(connector.grantAccountAuthorities(grantRequest, EORI(EORI_VALUE)))
      result mustBe false

      verifyEndPointUrlHit(acc30ManageAccountAuthoritiesEndpointUrl, POST)
    }

    "return false when Future is failed with exception" in new Setup {
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
      when(requestBuilder.execute(any, any)).thenReturn(Future.failed(new NotFoundException("error")))

      running(application) {
        val result: Boolean = await(connector.grantAccountAuthorities(grantRequest, EORI(EORI_VALUE)))
        result mustBe false
      }
    }
  }

  "revokeAccountAuthorities" should {

    "return true when the api responds with 204" in new Setup {
      wireMockServer.stubFor(
        post(urlPathMatching(acc30ManageAccountAuthoritiesEndpointUrl))
          .withHeader(X_FORWARDED_HOST, equalTo(MDTP))
          .withHeader(CONTENT_TYPE, equalTo("application/json"))
          .withHeader(ACCEPT, equalTo("application/json"))
          .withHeader(AUTHORIZATION, equalTo("Bearer test1234567"))
          .withRequestBody(
            matchingJsonPath("$.manageStandingAuthoritiesRequest[?(@.requestCommon.regime == 'CDS')]")
          )
          .withRequestBody(
            matchingJsonPath("$.manageStandingAuthoritiesRequest[?(@.requestDetail.ownerEori == 'testEORI')]")
          )
          .willReturn(noContent)
      )

      val result: Boolean = await(connector.revokeAccountAuthorities(revokeRequest, EORI(EORI_VALUE)))
      result mustBe true

      verifyEndPointUrlHit(acc30ManageAccountAuthoritiesEndpointUrl, POST)
    }

    "return false when the api responds with a successful response that isn't 204" in new Setup {
      wireMockServer.stubFor(
        post(urlPathMatching(acc30ManageAccountAuthoritiesEndpointUrl))
          .withHeader(X_FORWARDED_HOST, equalTo(MDTP))
          .withHeader(CONTENT_TYPE, equalTo("application/json"))
          .withHeader(ACCEPT, equalTo("application/json"))
          .withHeader(AUTHORIZATION, equalTo("Bearer test1234567"))
          .withRequestBody(
            matchingJsonPath("$.manageStandingAuthoritiesRequest[?(@.requestCommon.regime == 'CDS')]")
          )
          .withRequestBody(
            matchingJsonPath("$.manageStandingAuthoritiesRequest[?(@.requestDetail.ownerEori == 'testEORI')]")
          )
          .willReturn(ok(emptyString))
      )

      val result: Boolean = await(connector.revokeAccountAuthorities(revokeRequest, EORI(EORI_VALUE)))
      result mustBe false

      verifyEndPointUrlHit(acc30ManageAccountAuthoritiesEndpointUrl, POST)
    }

    "return false when the api fails" in new Setup {
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
      when(requestBuilder.execute(any, any)).thenReturn(Future.failed(new NotFoundException("error")))

      running(application) {
        val result = await(connector.revokeAccountAuthorities(revokeRequest, EORI(EORI_VALUE)))
        result mustBe false
      }
    }
  }

  override def config: Configuration = Configuration(
    ConfigFactory.parseString(
      s"""
         |microservice {
         |  services {
         |  acc30 {
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

    val acc30ManageAccountAuthoritiesEndpointUrl = "/customs-financials-hods-stub/accounts/managestandingauthorities/v1"

    val grantRequest: GrantAuthorityRequest = GrantAuthorityRequest(
      Accounts(None, Seq.empty, None),
      StandingAuthority(EORI("authorised"), LocalDate.now().toString, None, viewBalance = true),
      AuthorisedUser("someUser", "someRole"),
      editRequest = true
    )

    val revokeRequest: RevokeAuthorityRequest = RevokeAuthorityRequest(
      AccountNumber("GAN"),
      CdsCashAccount,
      EORI(EORI_VALUE),
      AuthorisedUser("someUser", "someRole")
    )

    val app: Application = application().configure(config).build()

    val connector: Acc30Connector = app.injector.instanceOf[Acc30Connector]
  }
}
