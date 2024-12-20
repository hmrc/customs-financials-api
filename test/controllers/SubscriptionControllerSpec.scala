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

import config.MetaConfig.Platform.{ENROLMENT_IDENTIFIER, ENROLMENT_KEY}
import domain.sub09.{EmailUnverifiedResponse, EmailVerifiedResponse}
import models.{EORI, EmailAddress}
import org.mockito.ArgumentMatchers.{any, eq => is}
import org.mockito.Mockito.when
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import play.api.{Application, inject}
import services.SubscriptionService
import uk.gov.hmrc.auth.core.{Enrolment, EnrolmentIdentifier, Enrolments}
import uk.gov.hmrc.http.NotFoundException
import utils.SpecBase
import utils.TestData.EORI_VALUE

import scala.concurrent.Future

class SubscriptionControllerSpec extends SpecBase {

  "SubscriptionController.get" should {
    "return 200 status code" in new Setup {
      val subscriptionResponse: EmailVerifiedResponse = EmailVerifiedResponse(None)

      when(mockSubscriptionService.getVerifiedEmail(is(traderEORI)))
        .thenReturn(Future.successful(subscriptionResponse))

      running(app) {
        val result = route(app, request).value
        status(result) mustBe OK
      }
    }

    "return 200 status code for unVerified email" in new Setup {
      val subscriptionResponse: EmailUnverifiedResponse = EmailUnverifiedResponse(None)

      when(mockSubscriptionService.getUnverifiedEmail(is(traderEORI)))
        .thenReturn(Future.successful(subscriptionResponse))

      running(app) {
        val result = route(app, unVerifiedEmailRequest).value
        status(result) mustBe OK
      }
    }

    "return 200 status code for getEmail" in new Setup {
      val subscriptionResponse: EmailVerifiedResponse = EmailVerifiedResponse(Some(EmailAddress("test@email.com")))

      when(mockSubscriptionService.getEmailAddress(is(traderEORI)))
        .thenReturn(Future.successful(subscriptionResponse))

      running(app) {
        val result = route(app, getEmailAddressrequest).value
        status(result) mustBe OK
      }
    }

    "return 503 for any error" in new Setup {
      when(mockSubscriptionService.getVerifiedEmail(is(traderEORI)))
        .thenReturn(Future.failed(new NotFoundException("ShouldNotReturnThis")))

      running(app) {
        val result = route(app, request).value
        status(result) mustBe SERVICE_UNAVAILABLE
      }
    }

    "return 503 for any error in unverified email" in new Setup {
      when(mockSubscriptionService.getUnverifiedEmail(is(traderEORI)))
        .thenReturn(Future.failed(new NotFoundException("ShouldNotReturnThis")))

      running(app) {
        val result = route(app, unVerifiedEmailRequest).value
        status(result) mustBe SERVICE_UNAVAILABLE
      }
    }

    "return 503 for any error in getEmail" in new Setup {
      when(mockSubscriptionService.getEmailAddress(is(traderEORI)))
        .thenReturn(Future.failed(new NotFoundException("ShouldNotReturnThis")))

      running(app) {
        val result = route(app, getEmailAddressrequest).value
        status(result) mustBe SERVICE_UNAVAILABLE
      }
    }
  }

  trait Setup {

    val traderEORI: EORI       = EORI(EORI_VALUE)
    val enrolments: Enrolments =
      Enrolments(
        Set(Enrolment(ENROLMENT_KEY, Seq(EnrolmentIdentifier(ENROLMENT_IDENTIFIER, traderEORI.value)), "activated"))
      )

    val request: FakeRequest[AnyContentAsEmpty.type] =
      FakeRequest("GET", controllers.routes.SubscriptionController.getVerifiedEmail().url)

    val getEmailAddressrequest: FakeRequest[AnyContentAsEmpty.type] =
      FakeRequest("GET", controllers.routes.SubscriptionController.getEmail().url)

    val unVerifiedEmailRequest: FakeRequest[AnyContentAsEmpty.type] =
      FakeRequest("GET", controllers.routes.SubscriptionController.getUnverifiedEmail().url)

    val mockAuthConnector: CustomAuthConnector       = mock[CustomAuthConnector]
    val mockSubscriptionService: SubscriptionService = mock[SubscriptionService]

    when(mockAuthConnector.authorise[Enrolments](any, any)(any, any)).thenReturn(Future.successful(enrolments))

    val app: Application = GuiceApplicationBuilder()
      .overrides(
        inject.bind[CustomAuthConnector].toInstance(mockAuthConnector),
        inject.bind[SubscriptionService].toInstance(mockSubscriptionService)
      )
      .configure(
        "microservice.metrics.enabled" -> false,
        "metrics.enabled"              -> false,
        "auditing.enabled"             -> false
      )
      .build()
  }
}
