/*
 * Copyright 2021 HM Revenue & Customs
 *
 */

package controllers

import domain.sub09.EmailVerifiedResponse
import models.EORI
import org.mockito.ArgumentMatchers.{eq => is}
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.api.{Application, inject}
import services.SubscriptionService
import uk.gov.hmrc.auth.core.{Enrolment, EnrolmentIdentifier, Enrolments}
import uk.gov.hmrc.http.NotFoundException
import utils.SpecBase

import scala.concurrent.Future

class SubscriptionControllerSpec extends SpecBase {

  "SubscriptionController.get" should {
    "return 200 status code" in new Setup {
      val subscriptionResponse: EmailVerifiedResponse = EmailVerifiedResponse(None)

      when(mockSubscriptionService.getVerifiedEmail(is(traderEORI))(any))
        .thenReturn(Future.successful(subscriptionResponse))

      running(app) {
        val result = route(app, request).value
        status(result) mustBe OK
      }
    }

    "return 503 for any error" in new Setup {
      when(mockSubscriptionService.getVerifiedEmail(is(traderEORI))(any))
        .thenReturn(Future.failed(new NotFoundException("ShouldNotReturnThis")))

      running(app) {
        val result = route(app, request).value
        status(result) mustBe SERVICE_UNAVAILABLE
      }
    }
  }

  trait Setup {

    val traderEORI: EORI = EORI("testEORI")
    val enrolments: Enrolments = Enrolments(Set(Enrolment("HMRC-CUS-ORG", Seq(EnrolmentIdentifier("EORINumber", traderEORI.value)), "activated")))
    val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest("GET", controllers.routes.SubscriptionController.getVerifiedEmail().url)

    val mockAuthConnector: CustomAuthConnector = mock[CustomAuthConnector]
    val mockSubscriptionService: SubscriptionService = mock[SubscriptionService]

    when(mockAuthConnector.authorise[Enrolments](any, any)(any, any)).thenReturn(Future.successful(enrolments))

    val app: Application = GuiceApplicationBuilder().overrides(
      inject.bind[CustomAuthConnector].toInstance(mockAuthConnector),
      inject.bind[SubscriptionService].toInstance(mockSubscriptionService)
    ).configure(
      "microservice.metrics.enabled" -> false,
      "metrics.enabled" -> false,
      "auditing.enabled" -> false
    ).build()
  }
}
