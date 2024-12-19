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

import config.MetaConfig.RETURN_PARAM_POSITION
import domain.acc37.*
import domain.acc38
import domain.acc38.{GetCorrespondenceAddressResponse, ReturnParameter => ACC38ReturnParameter}
import models.requests.{GetContactDetailsRequest, UpdateContactDetailsRequest}
import models.responses.UpdateContactDetailsResponse
import models.{AccountNumber, AccountType, EORI, EmailAddress}
import org.mockito.ArgumentMatchers.{any, eq => eqTo}
import org.mockito.Mockito
import org.mockito.Mockito.{verify, when}
import play.api.http.Status
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json
import play.api.mvc.AnyContentAsJson
import play.api.test.*
import play.api.test.Helpers.*
import play.api.{Application, inject}
import services.AccountContactDetailsService
import uk.gov.hmrc.http.UpstreamErrorResponse
import utils.SpecBase
import utils.TestData.{COUNTRY_CODE_GB, TEST_EMAIL}

import scala.concurrent.Future

class DutyDefermentContactDetailsSpec extends SpecBase {

  "getContactDetails" should {

    "delegate to the service and return contact details with a 200 status code" in new Setup {
      when(mockAccountContactDetailsService.getAccountContactDetails(eqTo(traderDan), eqTo(traderEORI)))
        .thenReturn(Future.successful(acc38Response))

      running(app) {
        val result = route(app, getContactDetailsRequest).value
        status(result) mustBe OK
        contentAsJson(result) mustBe Json.toJson(acc38ContactDetails)
      }
    }

    "return InternalServerError" in new Setup {
      when(mockAccountContactDetailsService.getAccountContactDetails(eqTo(traderDan), eqTo(traderEORI)))
        .thenReturn(Future.successful(acc38ResponseWithMdtpError))

      running(app) {
        val result = route(app, getContactDetailsRequest).value
        status(result) mustBe INTERNAL_SERVER_ERROR
      }
    }

    "return BadRequest" in new Setup {
      val acc38ResponseForBadRequest: acc38.Response =
        domain.acc38.Response(GetCorrespondenceAddressResponse(acc38ResponseCommon, None))

      when(mockAccountContactDetailsService.getAccountContactDetails(eqTo(traderDan), eqTo(traderEORI)))
        .thenReturn(Future.successful(acc38ResponseForBadRequest))

      running(app) {
        val result = route(app, getContactDetailsRequest).value
        status(result) mustBe BAD_REQUEST
      }
    }

    "return ServiceUnavailable" in new Setup {
      when(mockAccountContactDetailsService.getAccountContactDetails(any, any))
        .thenReturn(Future.failed(new RuntimeException("error occurred")))

      running(app) {
        val result = route(app, getContactDetailsRequest).value
        status(result) mustBe SERVICE_UNAVAILABLE
      }
    }
  }

  "updateContactDetails" should {

    "throw an exception when update account contact details fails with a fatal error" in new Setup {
      when(mockAccountContactDetailsService.updateAccountContactDetails(any, any, any))
        .thenThrow(new RuntimeException("Boom1"))

      running(app) {
        intercept[RuntimeException] {
          val result = route(app, updateContactDetailsRequest).value
          status(result) mustBe INTERNAL_SERVER_ERROR
        }.getMessage mustBe "Boom1"
      }
    }

    "return 503" when {
      "update account contact details fails with BadRequestException (4xx)" in new Setup {
        when(mockAccountContactDetailsService.updateAccountContactDetails(any, any, any))
          .thenReturn(Future.failed(UpstreamErrorResponse("4xx", Status.FORBIDDEN, Status.FORBIDDEN)))

        running(app) {
          val result = route(app, updateContactDetailsRequest).value
          status(result) mustBe SERVICE_UNAVAILABLE
        }
      }

      "update account contact details fails with InternalServerException (5xx) " in new Setup {
        when(mockAccountContactDetailsService.updateAccountContactDetails(any, any, any))
          .thenReturn(
            Future.failed(UpstreamErrorResponse("5xx", Status.SERVICE_UNAVAILABLE, Status.SERVICE_UNAVAILABLE))
          )

        running(app) {
          val result = route(app, updateContactDetailsRequest).value
          status(result) mustBe SERVICE_UNAVAILABLE
        }
      }
    }

    "return success when update account contact details with a 200 status code" in new Setup {
      when(mockAccountContactDetailsService.updateAccountContactDetails(any, any, any))
        .thenReturn(Future.successful(acc37SuccessResponse))

      running(app) {
        val result = route(app, updateContactDetailsRequest).value
        status(result) mustBe OK
        contentAsJson(result) mustBe Json.toJson(UpdateContactDetailsResponse(true))

        verify(mockAccountContactDetailsService, Mockito.times(1))
          .updateAccountContactDetails(any, any, any)
      }
    }

    "return InternalServerError when there is mdtp error" in new Setup {
      val acc37ResponseWithMdtpError: Response =
        domain.acc37.Response(AmendCorrespondenceAddressResponse(acc37ResponseCommonMdtpError))

      when(mockAccountContactDetailsService.updateAccountContactDetails(any, any, any))
        .thenReturn(Future.successful(acc37ResponseWithMdtpError))

      running(app) {
        val result = route(app, updateContactDetailsRequest).value
        status(result) mustBe INTERNAL_SERVER_ERROR
      }
    }
  }

  trait Setup extends TestData {

    val getContactDetailsRequest: FakeRequest[AnyContentAsJson] =
      FakeRequest(POST, controllers.routes.DutyDefermentContactDetailsController.getContactDetails().url)
        .withJsonBody(Json.toJson(GetContactDetailsRequest(traderDan, traderEORI)))

    val updateContactDetailsRequest: FakeRequest[AnyContentAsJson] =
      FakeRequest(POST, routes.DutyDefermentContactDetailsController.updateContactDetails().url)
        .withJsonBody(
          Json.toJson(
            UpdateContactDetailsRequest(
              traderDan,
              traderEORI,
              Some("CHANGED MYNAME"),
              "New Road",
              None,
              Some("Edinburgh"),
              None,
              Some("AB12 3CD"),
              Some(COUNTRY_CODE_GB),
              None,
              None,
              Some(EmailAddress("email@email.com"))
            )
          )
        )

    val mockAccountContactDetailsService: AccountContactDetailsService = mock[AccountContactDetailsService]

    val app: Application = GuiceApplicationBuilder()
      .overrides(
        inject.bind[AccountContactDetailsService].toInstance(mockAccountContactDetailsService)
      )
      .configure(
        "microservice.metrics.enabled" -> false,
        "metrics.enabled"              -> false,
        "auditing.enabled"             -> false
      )
      .build()
  }

  trait TestData {
    val traderEORI: EORI         = EORI("testEORI")
    val traderDan: AccountNumber = AccountNumber("1234567")
    val testValue                = "test_value"

    val acc38ResponseCommon: acc38.ResponseCommon          =
      domain.acc38.ResponseCommon("OK", None, "2020-10-05T09:30:47Z", None)
    val acc38ResponseCommonMdtpError: acc38.ResponseCommon =
      domain.acc38.ResponseCommon(
        "OK",
        None,
        "2020-10-05T09:30:47Z",
        Some(List(ACC38ReturnParameter(RETURN_PARAM_POSITION, testValue)))
      )

    val acc38ContactDetails: acc38.ContactDetails = domain.acc38.ContactDetails(
      Some("Bobby Shaftoe"),
      "Boaty Lane",
      Some("Southampton Docks"),
      None,
      Some("Southampton"),
      Some("SO1 1AA"),
      COUNTRY_CODE_GB,
      Some("01234 555555"),
      None,
      Some(EmailAddress(TEST_EMAIL))
    )

    val acc38Response: acc38.Response = domain.acc38.Response(
      GetCorrespondenceAddressResponse(
        acc38ResponseCommon,
        Some(
          domain.acc38.ResponseDetail(
            traderEORI,
            domain.acc38.AccountDetails(AccountType("DutyDeferment"), traderDan),
            acc38ContactDetails
          )
        )
      )
    )

    val acc38ResponseWithMdtpError: acc38.Response = domain.acc38.Response(
      GetCorrespondenceAddressResponse(
        acc38ResponseCommonMdtpError,
        Some(
          domain.acc38.ResponseDetail(
            traderEORI,
            domain.acc38.AccountDetails(AccountType("DutyDeferment"), traderDan),
            acc38ContactDetails
          )
        )
      )
    )

    val acc37ContactInfo: ContactDetails = domain.acc37.ContactDetails(
      Some("Bobby Shaftoe"),
      "Boaty Lane",
      Some("Southampton Docks"),
      None,
      Some("Southampton"),
      Some("SO1 1AA"),
      COUNTRY_CODE_GB,
      Some("01234 555555"),
      None,
      Some(EmailAddress(TEST_EMAIL))
    )

    val acc37ResponseCommon: ResponseCommon          = ResponseCommon("OK", None, "2020-10-05T09:30:47Z", None)
    val acc37ResponseCommonMdtpError: ResponseCommon =
      ResponseCommon(
        "OK",
        None,
        "2020-10-05T09:30:47Z",
        Some(List(ReturnParameter(RETURN_PARAM_POSITION, testValue)).toArray)
      )

    val acc37SuccessResponse: Response = domain.acc37.Response(AmendCorrespondenceAddressResponse(acc37ResponseCommon))

  }
}
