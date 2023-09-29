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

package controllers

import domain._
import models.requests.manageAuthorities._
import models.{AccountNumber, AccountStatus, AccountType, EORI}
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json._
import play.api.mvc.{AnyContentAsEmpty, AnyContentAsJson}
import play.api.test.Helpers._
import play.api.test._
import play.api.{Application, inject}
import services._
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.http.{HeaderCarrier, UpstreamErrorResponse}
import utils.SpecBase

import scala.concurrent.{ExecutionContext, Future}

class AccountAuthoritiesControllerSpec extends SpecBase {
  "AccountAuthoritiesController.get" should {
    "delegate to the service and return a list of account authorities with a 200 status code" in new Setup {
      val accountWithAuthorities = Seq(AccountWithAuthorities(AccountType("CDSCash"), AccountNumber("12345"), AccountStatus("Open"), Seq.empty))
      when(mockAccountAuthorityService.getAccountAuthorities(eqTo(traderEORI))).thenReturn(Future.successful(accountWithAuthorities))

      running(app) {
        val result = route(app, getRequest).value
        status(result) mustBe OK
        contentAsJson(result) mustBe Json.toJson(accountWithAuthorities)
      }
    }

    "return an error when no EORINumber found in auth record" in new Setup {
      when(mockAuthConnector.authorise[Enrolments](any, any)(any, any))
        .thenReturn(Future.successful(Enrolments(Set())))

      running(app) {
        val result = route(app, getRequest).value
        status(result) mustBe FORBIDDEN
        contentAsString(result) mustBe "Enrolment Identifier EORINumber not found"
      }
    }

    "return 503 (service unavailable)" when {
      "get account authorities call fails with BadRequestException (4xx) " in new Setup {
        when(mockAccountAuthorityService.getAccountAuthorities(any)).thenReturn(Future.failed(UpstreamErrorResponse("4xx", FORBIDDEN, FORBIDDEN)))

        running(app) {
          val result = route(app, getRequest).value
          status(result) mustBe SERVICE_UNAVAILABLE
        }
      }

      "get account authorities call fails with InternalServerException (5xx) " in new Setup {
        when(mockAccountAuthorityService.getAccountAuthorities(any)).thenReturn(Future.failed(UpstreamErrorResponse("5xx", SERVICE_UNAVAILABLE, SERVICE_UNAVAILABLE)))

        running(app) {
          val result = route(app, getRequest).value
          status(result) mustBe SERVICE_UNAVAILABLE
        }
      }
    }

    "return 500 (InternalServerError)" when {
      "get account authorities call fails with InternalServerException (5xx) " in new Setup {
        when(mockAccountAuthorityService.getAccountAuthorities(any)).thenReturn(Future.failed(UpstreamErrorResponse("JSON validation", 500)))

        running(app) {
          val result = route(app, getRequest).value
          status(result) mustBe INTERNAL_SERVER_ERROR
        }
      }
    }
  }

  "AccountAuthoritiesController.grant" when {

    val grantAuthorityRequest = GrantAuthorityRequest(
      Accounts(Some("345"), Seq("123", "754"), Some("54345")),
      StandingAuthority(EORI("authorisedEori"), "2018-11-09", None, viewBalance = true),
      AuthorisedUser("some name", "some role"),
      editRequest = false
    )

    "request is valid and API call is successful" should {
      "delegate to the service and return a 204 status code" in new Setup {
        when(mockAccountAuthorityService.grantAccountAuthorities(eqTo(grantAuthorityRequest), eqTo(traderEORI))(any))
          .thenReturn(Future.successful(true))

        running(app) {
          val result = route(app, grantRequest(grantAuthorityRequest)).value
          status(result) mustBe NO_CONTENT
        }
      }

      "delegate to the service and auditEditAuthority" in new Setup {

        val editAuth = grantAuthorityRequest.copy(editRequest = true)

        when(mockAccountAuthorityService.grantAccountAuthorities(eqTo(editAuth), eqTo(traderEORI))(any))
          .thenReturn(Future.successful(true))

        running(app) {
          val result = route(app, grantRequest(editAuth)).value
          status(result) mustBe NO_CONTENT
        }
      }
    }

    "request is valid but API call fails" should {
      "return 500" in new Setup {
        when(mockAccountAuthorityService.grantAccountAuthorities(eqTo(grantAuthorityRequest), eqTo(traderEORI))(any))
          .thenReturn(Future.successful(false))

        running(app) {
          val result = route(app, grantRequest(grantAuthorityRequest)).value
          status(result) mustBe INTERNAL_SERVER_ERROR
        }
      }
    }

    "request JSON is invalid" should {
      "return 400" in new Setup {
        val invalidRequest: FakeRequest[AnyContentAsJson] = FakeRequest(
          POST, controllers.routes.AccountAuthoritiesController.grant(EORI("testEori")).url)
          .withJsonBody(Json.parse("""{"valid": "nope"}"""))

        running(app) {
          val result = route(app, invalidRequest).value
          status(result) mustBe BAD_REQUEST
        }
      }
    }

    "no EORINumber found in auth record" should {
      "return 403" in new Setup {
        when(mockAuthConnector.authorise[Enrolments](any, any)(any, any))
          .thenReturn(Future.successful(Enrolments(Set())))

        running(app) {
          val result = route(app, grantRequest(grantAuthorityRequest)).value
          status(result) mustBe FORBIDDEN
          contentAsString(result) mustBe "Enrolment Identifier EORINumber not found"
        }
      }
    }
  }

  "AccountAuthoritiesController.revoke" when {

    val revokeAuthorityRequest = RevokeAuthorityRequest(
      AccountNumber("123"), CdsCashAccount, EORI("authorisedEori"), AuthorisedUser("some name", "some role")
    )

    "request is valid and API call is successful" should {
      "delegate to the service and return a 204 status code" when {
        "revoking for a cash account" in new Setup {
          when(mockAccountAuthorityService.revokeAccountAuthorities(eqTo(revokeAuthorityRequest), eqTo(traderEORI))(any))
            .thenReturn(Future.successful(true))

          running(app) {
            val result = route(app, revokeRequest(revokeAuthorityRequest)).value
            status(result) mustBe NO_CONTENT
          }
        }

        "revoking for a guarantee account" in new Setup {
          val revokeGuaranteeRequest: RevokeAuthorityRequest = revokeAuthorityRequest.copy(accountType = CdsGeneralGuaranteeAccount)

          when(mockAccountAuthorityService.revokeAccountAuthorities(eqTo(revokeGuaranteeRequest), eqTo(traderEORI))(any))
            .thenReturn(Future.successful(true))

          running(app) {
            val result = route(app, revokeRequest(revokeGuaranteeRequest)).value
            status(result) mustBe NO_CONTENT
          }
        }

        "revoking for a deferment account" in new Setup {
          val revokeDefermentRequest: RevokeAuthorityRequest = revokeAuthorityRequest.copy(accountType = CdsDutyDefermentAccount)

          when(mockAccountAuthorityService.revokeAccountAuthorities(eqTo(revokeDefermentRequest), eqTo(traderEORI))(any))
            .thenReturn(Future.successful(true))

          running(app) {
            val result = route(app, revokeRequest(revokeDefermentRequest)).value
            status(result) mustBe NO_CONTENT
          }
        }
      }
    }

    "request is valid but API call fails" should {
      "return 500" in new Setup {
        when(mockAccountAuthorityService.revokeAccountAuthorities(eqTo(revokeAuthorityRequest), eqTo(traderEORI))(any))
          .thenReturn(Future.successful(false))

        running(app) {
          val result = route(app, revokeRequest(revokeAuthorityRequest)).value
          status(result) mustBe INTERNAL_SERVER_ERROR
        }
      }
    }

    "request JSON is invalid" should {
      "return 400" in new Setup {

        val invalidRequest: FakeRequest[AnyContentAsJson] = FakeRequest(
          POST, controllers.routes.AccountAuthoritiesController.revoke(EORI("testEori")).url)
          .withJsonBody(Json.parse("""{"valid": "nope"}"""))

        running(app) {
          val result = route(app, invalidRequest).value
          status(result) mustBe BAD_REQUEST
        }
      }
    }

    "no EORINumber found in auth record" should {
      "return 403" in new Setup {
        when(mockAuthConnector.authorise[Enrolments](any, any)(any, any))
          .thenReturn(Future.successful(Enrolments(Set())))

        running(app) {
          val result = route(app, revokeRequest(revokeAuthorityRequest)).value
          status(result) mustBe FORBIDDEN
          contentAsString(result) mustBe "Enrolment Identifier EORINumber not found"
        }
      }
    }
  }

  trait Setup {
    val traderEORI: EORI = EORI("testEORI")
    val enrolments: Enrolments = Enrolments(Set(Enrolment("HMRC-CUS-ORG", Seq(EnrolmentIdentifier("EORINumber", traderEORI.value)), "activated")))
    val getRequest: FakeRequest[AnyContentAsEmpty.type] = FakeRequest(GET, controllers.routes.AccountAuthoritiesController.get(traderEORI).url)

    def grantRequest(request: GrantAuthorityRequest): FakeRequest[AnyContentAsJson] =
      FakeRequest(POST, controllers.routes.AccountAuthoritiesController.grant(traderEORI).url)
        .withJsonBody(Json.toJson(request))

    def revokeRequest(request: RevokeAuthorityRequest): FakeRequest[AnyContentAsJson] =
      FakeRequest(POST, controllers.routes.AccountAuthoritiesController.revoke(traderEORI).url)
        .withJsonBody(Json.toJson(request))

    implicit val hc: HeaderCarrier = HeaderCarrier()
    implicit val ec: ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global

    val mockAuthConnector: CustomAuthConnector = mock[CustomAuthConnector]
    when(mockAuthConnector.authorise[Enrolments](any, any)(any, any)).thenReturn(Future.successful(enrolments))
    val mockAccountAuthorityService: AccountAuthorityService = mock[AccountAuthorityService]

    val app: Application = GuiceApplicationBuilder().overrides(
      inject.bind[CustomAuthConnector].toInstance(mockAuthConnector),
      inject.bind[AccountAuthorityService].toInstance(mockAccountAuthorityService)
    ).configure(
      "microservice.metrics.enabled" -> false,
      "metrics.enabled" -> false,
      "auditing.enabled" -> false
    ).build()
  }
}
