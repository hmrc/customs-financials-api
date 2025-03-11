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
import domain.acc40.*
import domain.{Acc40Response, AuthoritiesFound, ErrorResponse, NoAuthoritiesFound}
import models.EORI
import play.api.{Application, Configuration}
import play.api.test.Helpers.*
import uk.gov.hmrc.http.HeaderCarrier
import utils.{SpecBase, WireMockSupportProvider}
import utils.TestData.EORI_VALUE_1
import com.typesafe.config.ConfigFactory
import play.api.libs.json.Json
import com.github.tomakehurst.wiremock.client.WireMock.{aResponse, equalTo, matchingJsonPath, ok, post, urlPathMatching}
import com.github.tomakehurst.wiremock.http.Fault
import com.github.tomakehurst.wiremock.http.RequestMethod.POST

class Acc40ConnectorSpec extends SpecBase with WireMockSupportProvider {

  "searchAuthorities" should {

    "return Left no authorities when no authorities returned in the response" in new Setup {
      wireMockServer.stubFor(
        post(urlPathMatching(acc40SearchAuthoritiesEndpointUrl))
          .withHeader(X_FORWARDED_HOST, equalTo(MDTP))
          .withHeader(CONTENT_TYPE, equalTo(CONTENT_TYPE_APPLICATION_JSON))
          .withHeader(ACCEPT, equalTo(CONTENT_TYPE_APPLICATION_JSON))
          .withHeader(AUTHORIZATION, equalTo(AUTH_BEARER_TOKEN_VALUE))
          .withRequestBody(
            matchingJsonPath("$.searchAuthoritiesRequest[?(@.requestCommon.originatingSystem == 'MDTP')]")
          )
          .withRequestBody(
            matchingJsonPath("$.searchAuthoritiesRequest[?(@.requestCommon.regime == 'CDS')]")
          )
          .withRequestBody(
            matchingJsonPath("$.searchAuthoritiesRequest[?(@.requestDetail.requestingEORI == 'someEORI')]")
          )
          .willReturn(ok(Json.toJson(response(None, Some("0"), None, None, None)).toString))
      )

      val result: Either[Acc40Response, AuthoritiesFound] =
        await(connector.searchAuthorities(EORI(EORI_VALUE_1), EORI(EORI_VALUE_1)))

      result mustBe Left(NoAuthoritiesFound)

      verifyExactlyOneEndPointUrlHit(acc40SearchAuthoritiesEndpointUrl, POST)
    }

    "return Left with error response if the error message present in the response" in new Setup {

      wireMockServer.stubFor(
        post(urlPathMatching(acc40SearchAuthoritiesEndpointUrl))
          .withHeader(X_FORWARDED_HOST, equalTo(MDTP))
          .withHeader(CONTENT_TYPE, equalTo(CONTENT_TYPE_APPLICATION_JSON))
          .withHeader(ACCEPT, equalTo(CONTENT_TYPE_APPLICATION_JSON))
          .withHeader(AUTHORIZATION, equalTo(AUTH_BEARER_TOKEN_VALUE))
          .withRequestBody(
            matchingJsonPath("$.searchAuthoritiesRequest[?(@.requestCommon.originatingSystem == 'MDTP')]")
          )
          .withRequestBody(
            matchingJsonPath("$.searchAuthoritiesRequest[?(@.requestCommon.regime == 'CDS')]")
          )
          .withRequestBody(
            matchingJsonPath("$.searchAuthoritiesRequest[?(@.requestDetail.requestingEORI == 'someEORI')]")
          )
          .willReturn(ok(Json.toJson(response(Some("error message"), Some("0"), None, None, None)).toString))
      )

      val result: Either[Acc40Response, AuthoritiesFound] =
        await(connector.searchAuthorities(EORI(EORI_VALUE_1), EORI(EORI_VALUE_1)))

      result mustBe Left(ErrorResponse)

      verifyExactlyOneEndPointUrlHit(acc40SearchAuthoritiesEndpointUrl, POST)
    }

    "return error response when exception occurs while making the POST call" in new Setup {

      wireMockServer.stubFor(
        post(urlPathMatching(acc40SearchAuthoritiesEndpointUrl))
          .withHeader(X_FORWARDED_HOST, equalTo(MDTP))
          .withHeader(CONTENT_TYPE, equalTo(CONTENT_TYPE_APPLICATION_JSON))
          .withHeader(ACCEPT, equalTo(CONTENT_TYPE_APPLICATION_JSON))
          .withHeader(AUTHORIZATION, equalTo(AUTH_BEARER_TOKEN_VALUE))
          .withRequestBody(
            matchingJsonPath("$.searchAuthoritiesRequest[?(@.requestCommon.originatingSystem == 'MDTP')]")
          )
          .withRequestBody(
            matchingJsonPath("$.searchAuthoritiesRequest[?(@.requestCommon.regime == 'CDS')]")
          )
          .withRequestBody(
            matchingJsonPath("$.searchAuthoritiesRequest[?(@.requestDetail.requestingEORI == 'someEORI')]")
          )
          .willReturn(aResponse().withFault(Fault.CONNECTION_RESET_BY_PEER))
      )

      val result: Either[Acc40Response, AuthoritiesFound] =
        await(connector.searchAuthorities(EORI(EORI_VALUE_1), EORI(EORI_VALUE_1)))

      result mustBe Left(ErrorResponse)

      verifyEndPointUrlHit(acc40SearchAuthoritiesEndpointUrl, POST)
    }

    "return Right if a valid response with authorities returned" in new Setup {

      wireMockServer.stubFor(
        post(urlPathMatching(acc40SearchAuthoritiesEndpointUrl))
          .withHeader(X_FORWARDED_HOST, equalTo(MDTP))
          .withHeader(CONTENT_TYPE, equalTo(CONTENT_TYPE_APPLICATION_JSON))
          .withHeader(ACCEPT, equalTo(CONTENT_TYPE_APPLICATION_JSON))
          .withHeader(AUTHORIZATION, equalTo(AUTH_BEARER_TOKEN_VALUE))
          .withRequestBody(
            matchingJsonPath("$.searchAuthoritiesRequest[?(@.requestCommon.originatingSystem == 'MDTP')]")
          )
          .withRequestBody(
            matchingJsonPath("$.searchAuthoritiesRequest[?(@.requestCommon.regime == 'CDS')]")
          )
          .withRequestBody(
            matchingJsonPath("$.searchAuthoritiesRequest[?(@.requestDetail.requestingEORI == 'someEORI')]")
          )
          .willReturn(
            ok(
              Json
                .toJson(
                  response(
                    None,
                    Some("1"),
                    Some(Seq(CashAccount(Account("accountNumber", "accountType", "accountOwner"), Some("10.0")))),
                    None,
                    None
                  )
                )
                .toString
            )
          )
      )

      val result: Either[Acc40Response, AuthoritiesFound] =
        await(connector.searchAuthorities(EORI(EORI_VALUE_1), EORI(EORI_VALUE_1)))

      result mustBe
        Right(
          AuthoritiesFound(
            Some("1"),
            None,
            None,
            Some(Seq(CashAccount(Account("accountNumber", "accountType", "accountOwner"), Some("10.0"))))
          )
        )

      verifyExactlyOneEndPointUrlHit(acc40SearchAuthoritiesEndpointUrl, POST)
    }

    "search type value" should {
      "return 0 for GB EORI searchID" in new Setup {
        val searchTypeValue: String = connector.searchType(EORI("GB123456789012"))
        searchTypeValue mustBe "0"
      }

      "return 0 for XI EORI searchID" in new Setup {
        val searchTypeValue: String = connector.searchType(EORI("XI123456789012"))
        searchTypeValue mustBe "0"
      }

      "return 0 for EU Eori search Id" when {
        "eu-eori-enabled is set to true" in new Setup {
          def config: Configuration = Configuration(
            ConfigFactory.parseString(
              s"""
                 |microservice {
                 |  services {
                 |  acc40 {
                 |            host = $wireMockHost
                 |            port = $wireMockPort
                 |        }
                 |  }
                 |}
                 |features {
                 |eu-eori-enabled = true
                 |}
                 |""".stripMargin
            )
          )

          override val app: Application          = application().configure(config).build()
          override val connector: Acc40Connector = app.injector.instanceOf[Acc40Connector]

          val searchTypeValue: String = connector.searchType(EORI("FR123456789012"))

          searchTypeValue mustBe "0"
        }
      }

      "return 1 for EU Eori search Id" when {
        "eu-eori-enabled is set to false" in new Setup {
          def config: Configuration = Configuration(
            ConfigFactory.parseString(
              s"""
                 |microservice {
                 |  services {
                 |  acc40 {
                 |            host = $wireMockHost
                 |            port = $wireMockPort
                 |        }
                 |  }
                 |}
                 |features {
                 |eu-eori-enabled = false
                 |}
                 |""".stripMargin
            )
          )

          override val app: Application          = application().configure(config).build()
          override val connector: Acc40Connector = app.injector.instanceOf[Acc40Connector]

          val searchTypeValue: String = connector.searchType(EORI("FR123456789012"))

          searchTypeValue mustBe "1"
        }
      }

      "return 1 for account number searchID" in new Setup {
        val searchTypeValue: String = connector.searchType(EORI("1234567"))
        searchTypeValue mustBe "1"
      }
    }
  }

  override def config: Configuration = Configuration(
    ConfigFactory.parseString(
      s"""
         |microservice {
         |  services {
         |  acc40 {
         |            host = $wireMockHost
         |            port = $wireMockPort
         |        }
         |  }
         |}
         |""".stripMargin
    )
  )

  trait Setup {
    implicit val hc: HeaderCarrier        = HeaderCarrier()
    val acc40SearchAuthoritiesEndpointUrl = "/customs-financials-hods-stub/accounts/searchauthorities/v1"

    def response(
      error: Option[String],
      numberOfAuthorities: Option[String],
      cashAccount: Option[Seq[CashAccount]],
      dutyDefermentAccount: Option[Seq[DutyDefermentAccount]],
      generalGuaranteeAccount: Option[Seq[GeneralGuaranteeAccount]]
    ): SearchAuthoritiesResponse =
      SearchAuthoritiesResponse(
        domain.acc40.Response(
          RequestCommon("date", MDTP, "reference", REGIME_CDS),
          RequestDetail(EORI(EORI_VALUE_1), "1", EORI("someOtherEORI")),
          ResponseDetail(
            errorMessage = error,
            numberOfAuthorities = numberOfAuthorities,
            dutyDefermentAccounts = dutyDefermentAccount,
            generalGuaranteeAccounts = generalGuaranteeAccount,
            cdsCashAccounts = cashAccount
          )
        )
      )

    val app: Application          = application().configure(config).build()
    val connector: Acc40Connector = app.injector.instanceOf[Acc40Connector]
  }
}
