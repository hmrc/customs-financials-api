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
import play.api.{Application, Configuration}
import com.typesafe.config.ConfigFactory
import play.api.test.Helpers.*
import uk.gov.hmrc.http.HeaderCarrier
import utils.{SpecBase, WireMockSupportProvider}
import utils.Utils.emptyString
import com.github.tomakehurst.wiremock.client.WireMock.{
  aResponse, equalTo, matchingJsonPath, noContent, ok, post, serverError, urlPathMatching
}
import com.github.tomakehurst.wiremock.http.Fault
import com.github.tomakehurst.wiremock.http.RequestMethod.POST
import utils.TestData.EORI_VALUE

import java.time.LocalDate
import config.MetaConfig.Platform.MDTP

class Acc30ConnectorSpec extends SpecBase with WireMockSupportProvider {

  "grantAccountAuthorities" should {

    "return true when the api responds with 204" in new Setup {

      wireMockServer.stubFor(
        post(urlPathMatching(acc30ManageAccountAuthoritiesEndpointUrl))
          .withHeader(X_FORWARDED_HOST, equalTo(MDTP))
          .withHeader(CONTENT_TYPE, equalTo(CONTENT_TYPE_APPLICATION_JSON))
          .withHeader(ACCEPT, equalTo(CONTENT_TYPE_APPLICATION_JSON))
          .withHeader(AUTHORIZATION, equalTo(AUTH_BEARER_TOKEN_VALUE))
          .withRequestBody(
            matchingJsonPath("$.manageStandingAuthoritiesRequest[?(@.requestCommon.regime == 'CDS')]")
          )
          .withRequestBody(
            matchingJsonPath("$.manageStandingAuthoritiesRequest[?(@.requestDetail.ownerEori == 'testEORI')]")
          )
          .willReturn(noContent)
      )

      val result: Boolean = await(connector.grantAccountAuthorities(grantRequest))
      result mustBe true

      verifyExactlyOneEndPointUrlHit(acc30ManageAccountAuthoritiesEndpointUrl, POST)
    }

    "return false when the api responds with a successful response that isn't 204" in new Setup {

      wireMockServer.stubFor(
        post(urlPathMatching(acc30ManageAccountAuthoritiesEndpointUrl))
          .withHeader(X_FORWARDED_HOST, equalTo(MDTP))
          .withHeader(CONTENT_TYPE, equalTo(CONTENT_TYPE_APPLICATION_JSON))
          .withHeader(ACCEPT, equalTo(CONTENT_TYPE_APPLICATION_JSON))
          .withHeader(AUTHORIZATION, equalTo(AUTH_BEARER_TOKEN_VALUE))
          .withRequestBody(
            matchingJsonPath("$.manageStandingAuthoritiesRequest[?(@.requestCommon.regime == 'CDS')]")
          )
          .withRequestBody(
            matchingJsonPath("$.manageStandingAuthoritiesRequest[?(@.requestDetail.ownerEori == 'testEORI')]")
          )
          .willReturn(ok(emptyString))
      )

      val result: Boolean = await(connector.grantAccountAuthorities(grantRequest))
      result mustBe false

      verifyExactlyOneEndPointUrlHit(acc30ManageAccountAuthoritiesEndpointUrl, POST)
    }

    "return false when the api fails" in new Setup {

      wireMockServer.stubFor(
        post(urlPathMatching(acc30ManageAccountAuthoritiesEndpointUrl))
          .withHeader(X_FORWARDED_HOST, equalTo(MDTP))
          .withHeader(CONTENT_TYPE, equalTo(CONTENT_TYPE_APPLICATION_JSON))
          .withHeader(ACCEPT, equalTo(CONTENT_TYPE_APPLICATION_JSON))
          .withHeader(AUTHORIZATION, equalTo(AUTH_BEARER_TOKEN_VALUE))
          .withRequestBody(
            matchingJsonPath("$.manageStandingAuthoritiesRequest[?(@.requestCommon.regime == 'CDS')]")
          )
          .withRequestBody(
            matchingJsonPath("$.manageStandingAuthoritiesRequest[?(@.requestDetail.ownerEori == 'testEORI')]")
          )
          .willReturn(serverError)
      )

      val result: Boolean = await(connector.grantAccountAuthorities(grantRequest))
      result mustBe false

      verifyExactlyOneEndPointUrlHit(acc30ManageAccountAuthoritiesEndpointUrl, POST)
    }

    "return false when Future is failed with exception" in new Setup {

      wireMockServer.stubFor(
        post(urlPathMatching(acc30ManageAccountAuthoritiesEndpointUrl))
          .withHeader(X_FORWARDED_HOST, equalTo(MDTP))
          .withHeader(CONTENT_TYPE, equalTo(CONTENT_TYPE_APPLICATION_JSON))
          .withHeader(ACCEPT, equalTo(CONTENT_TYPE_APPLICATION_JSON))
          .withHeader(AUTHORIZATION, equalTo(AUTH_BEARER_TOKEN_VALUE))
          .withRequestBody(
            matchingJsonPath("$.manageStandingAuthoritiesRequest[?(@.requestCommon.regime == 'CDS')]")
          )
          .withRequestBody(
            matchingJsonPath("$.manageStandingAuthoritiesRequest[?(@.requestDetail.ownerEori == 'testEORI')]")
          )
          .willReturn(aResponse().withFault(Fault.CONNECTION_RESET_BY_PEER))
      )

      val result: Boolean = await(connector.grantAccountAuthorities(grantRequest))
      result mustBe false

      verifyEndPointUrlHit(acc30ManageAccountAuthoritiesEndpointUrl, POST)
    }
  }

  "revokeAccountAuthorities" should {

    "return true when the api responds with 204" in new Setup {
      wireMockServer.stubFor(
        post(urlPathMatching(acc30ManageAccountAuthoritiesEndpointUrl))
          .withHeader(X_FORWARDED_HOST, equalTo(MDTP))
          .withHeader(CONTENT_TYPE, equalTo(CONTENT_TYPE_APPLICATION_JSON))
          .withHeader(ACCEPT, equalTo(CONTENT_TYPE_APPLICATION_JSON))
          .withHeader(AUTHORIZATION, equalTo(AUTH_BEARER_TOKEN_VALUE))
          .withRequestBody(
            matchingJsonPath("$.manageStandingAuthoritiesRequest[?(@.requestCommon.regime == 'CDS')]")
          )
          .withRequestBody(
            matchingJsonPath("$.manageStandingAuthoritiesRequest[?(@.requestDetail.ownerEori == 'testEORI')]")
          )
          .willReturn(noContent)
      )

      val result: Boolean = await(connector.revokeAccountAuthorities(revokeRequest))
      result mustBe true

      verifyExactlyOneEndPointUrlHit(acc30ManageAccountAuthoritiesEndpointUrl, POST)
    }

    "return false when the api responds with a successful response that isn't 204" in new Setup {
      wireMockServer.stubFor(
        post(urlPathMatching(acc30ManageAccountAuthoritiesEndpointUrl))
          .withHeader(X_FORWARDED_HOST, equalTo(MDTP))
          .withHeader(CONTENT_TYPE, equalTo(CONTENT_TYPE_APPLICATION_JSON))
          .withHeader(ACCEPT, equalTo(CONTENT_TYPE_APPLICATION_JSON))
          .withHeader(AUTHORIZATION, equalTo(AUTH_BEARER_TOKEN_VALUE))
          .withRequestBody(
            matchingJsonPath("$.manageStandingAuthoritiesRequest[?(@.requestCommon.regime == 'CDS')]")
          )
          .withRequestBody(
            matchingJsonPath("$.manageStandingAuthoritiesRequest[?(@.requestDetail.ownerEori == 'testEORI')]")
          )
          .willReturn(ok(emptyString))
      )

      val result: Boolean = await(connector.revokeAccountAuthorities(revokeRequest))
      result mustBe false

      verifyExactlyOneEndPointUrlHit(acc30ManageAccountAuthoritiesEndpointUrl, POST)
    }

    "return false when the api fails" in new Setup {

      wireMockServer.stubFor(
        post(urlPathMatching(acc30ManageAccountAuthoritiesEndpointUrl))
          .withHeader(X_FORWARDED_HOST, equalTo(MDTP))
          .withHeader(CONTENT_TYPE, equalTo(CONTENT_TYPE_APPLICATION_JSON))
          .withHeader(ACCEPT, equalTo(CONTENT_TYPE_APPLICATION_JSON))
          .withHeader(AUTHORIZATION, equalTo(AUTH_BEARER_TOKEN_VALUE))
          .withRequestBody(
            matchingJsonPath("$.manageStandingAuthoritiesRequest[?(@.requestCommon.regime == 'CDS')]")
          )
          .withRequestBody(
            matchingJsonPath("$.manageStandingAuthoritiesRequest[?(@.requestDetail.ownerEori == 'testEORI')]")
          )
          .willReturn(aResponse().withFault(Fault.CONNECTION_RESET_BY_PEER))
      )

      val result: Boolean = await(connector.revokeAccountAuthorities(revokeRequest))
      result mustBe false

      verifyEndPointUrlHit(acc30ManageAccountAuthoritiesEndpointUrl, POST)
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
      editRequest = true,
      ownerEori = EORI(EORI_VALUE)
    )

    val revokeRequest: RevokeAuthorityRequest = RevokeAuthorityRequest(
      AccountNumber("GAN"),
      CdsCashAccount,
      EORI(EORI_VALUE),
      AuthorisedUser("someUser", "someRole"),
      ownerEori = EORI(EORI_VALUE)
    )

    val app: Application = application().configure(config).build()

    val connector: Acc30Connector = app.injector.instanceOf[Acc30Connector]
  }
}
