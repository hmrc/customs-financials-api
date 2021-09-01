/*
 * Copyright 2021 HM Revenue & Customs
 *
 */

package connectors

import domain.StandingAuthority
import models.requests.manageAuthorities._
import models.{AccountNumber, EORI}
import play.api.Application
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.Helpers._
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient, HttpResponse, NotFoundException}
import utils.SpecBase

import java.time.LocalDate
import scala.concurrent.Future

class Acc30ConnectorSpec extends SpecBase {

  "grantAccountAuthorities" should {
    "return true when the api responds with 204" in new Setup {
      when[Future[HttpResponse]](mockHttpClient.POST(any, any, any)(any, any, any, any))
        .thenReturn(Future.successful(HttpResponse(NO_CONTENT, "")))

      running(app) {
        val result = await(connector.grantAccountAuthorities(grantRequest, EORI("someEori"), hc.requestId))
        result mustBe true
      }
    }
    "return false when the api responds with a successful response that isn't 204" in new Setup {
      when[Future[HttpResponse]](mockHttpClient.POST(any, any, any)(any, any, any, any))
        .thenReturn(Future.successful(HttpResponse(OK, "")))

      running(app) {
        val result = await(connector.grantAccountAuthorities(grantRequest, EORI("someEori"), hc.requestId))
        result mustBe false
      }
    }
    "return false when the api fails" in new Setup {
      when[Future[HttpResponse]](mockHttpClient.POST(any, any, any)(any, any, any, any))
        .thenReturn(Future.failed(new NotFoundException("error")))

      running(app) {
        val result = await(connector.grantAccountAuthorities(grantRequest, EORI("someEori"), hc.requestId))
        result mustBe false
      }
    }
  }

  "revokeAccountAuthorities" should {
    "return true when the api responds with 204" in new Setup {
      when[Future[HttpResponse]](mockHttpClient.POST(any, any, any)(any, any, any, any))
        .thenReturn(Future.successful(HttpResponse(NO_CONTENT, "")))

      running(app) {
        val result = await(connector.revokeAccountAuthorities(revokeRequest, EORI("someEori"), hc.requestId))
        result mustBe true
      }
    }
    "return false when the api responds with a successful response that isn't 204" in new Setup {
      when[Future[HttpResponse]](mockHttpClient.POST(any, any, any)(any, any, any, any))
        .thenReturn(Future.successful(HttpResponse(OK, "")))

      running(app) {
        val result = await(connector.revokeAccountAuthorities(revokeRequest, EORI("someEori"), hc.requestId))
        result mustBe false
      }
    }
    "return false when the api fails" in new Setup {
      when[Future[HttpResponse]](mockHttpClient.POST(any, any, any)(any, any, any, any))
        .thenReturn(Future.failed(new NotFoundException("error")))

      running(app) {
        val result = await(connector.revokeAccountAuthorities(revokeRequest, EORI("someEori"), hc.requestId))
        result mustBe false
      }
    }
  }

  trait Setup {
    implicit val hc: HeaderCarrier = HeaderCarrier()
    val mockHttpClient: HttpClient = mock[HttpClient]

    val grantRequest: GrantAuthorityRequest = GrantAuthorityRequest(
      Accounts(None, Seq.empty, None),
      StandingAuthority(EORI("authorised"), LocalDate.now().toString, None, viewBalance = true),
      AuthorisedUser("someUser", "someRole"),
      editRequest = true
    )

    val revokeRequest: RevokeAuthorityRequest = RevokeAuthorityRequest(
      AccountNumber("GAN"),
      CdsCashAccount,
      EORI("someEori"),
      AuthorisedUser("someUser", "someRole")
    )

    val app: Application = GuiceApplicationBuilder().overrides(
      bind[HttpClient].toInstance(mockHttpClient)
    ).configure(
      "microservice.metrics.enabled" -> false,
      "metrics.enabled" -> false,
      "auditing.enabled" -> false
    ).build()

    val connector: Acc30Connector = app.injector.instanceOf[Acc30Connector]
  }
}
