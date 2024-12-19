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
import play.api.Application
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.Helpers.*
import uk.gov.hmrc.http.client.{HttpClientV2, RequestBuilder}
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse, NotFoundException}
import utils.SpecBase
import utils.Utils.emptyString

import java.time.LocalDate
import scala.concurrent.Future

class Acc30ConnectorSpec extends SpecBase {

  "grantAccountAuthorities" should {

    "return true when the api responds with 204" in new Setup {
      when(requestBuilder.withBody(any())(any(), any(), any())).thenReturn(requestBuilder)
      when(requestBuilder.setHeader(any[(String, String)]())).thenReturn(requestBuilder)
      when(mockHttpClient.post(any)(any)).thenReturn(requestBuilder)
      when(requestBuilder.execute(any, any)).thenReturn(Future.successful(HttpResponse(NO_CONTENT, emptyString)))

      running(app) {
        val result = await(connector.grantAccountAuthorities(grantRequest, EORI("someEori")))
        result mustBe true
      }
    }

    "return false when the api responds with a successful response that isn't 204" in new Setup {
      when(requestBuilder.withBody(any())(any(), any(), any())).thenReturn(requestBuilder)
      when(requestBuilder.setHeader(any[(String, String)]())).thenReturn(requestBuilder)
      when(mockHttpClient.post(any)(any)).thenReturn(requestBuilder)
      when(requestBuilder.execute(any, any)).thenReturn(Future.successful(HttpResponse(OK, emptyString)))

      running(app) {
        val result = await(connector.grantAccountAuthorities(grantRequest, EORI("someEori")))
        result mustBe false
      }
    }

    "return false when the api fails" in new Setup {
      when(requestBuilder.withBody(any())(any(), any(), any())).thenReturn(requestBuilder)
      when(requestBuilder.setHeader(any[(String, String)]())).thenReturn(requestBuilder)
      when(mockHttpClient.post(any)(any)).thenReturn(requestBuilder)
      when(requestBuilder.execute(any, any)).thenReturn(Future.failed(new NotFoundException("error")))

      running(app) {
        val result = await(connector.grantAccountAuthorities(grantRequest, EORI("someEori")))
        result mustBe false
      }
    }
  }

  "revokeAccountAuthorities" should {

    "return true when the api responds with 204" in new Setup {
      when(requestBuilder.withBody(any())(any(), any(), any())).thenReturn(requestBuilder)
      when(requestBuilder.setHeader(any[(String, String)]())).thenReturn(requestBuilder)
      when(mockHttpClient.post(any)(any)).thenReturn(requestBuilder)
      when(requestBuilder.execute(any, any)).thenReturn(Future.successful(HttpResponse(NO_CONTENT, emptyString)))

      running(app) {
        val result = await(connector.revokeAccountAuthorities(revokeRequest, EORI("someEori")))
        result mustBe true
      }
    }

    "return false when the api responds with a successful response that isn't 204" in new Setup {
      when(requestBuilder.withBody(any())(any(), any(), any())).thenReturn(requestBuilder)
      when(requestBuilder.setHeader(any[(String, String)]())).thenReturn(requestBuilder)
      when(mockHttpClient.post(any)(any)).thenReturn(requestBuilder)
      when(requestBuilder.execute(any, any)).thenReturn(Future.successful(HttpResponse(OK, emptyString)))

      running(app) {
        val result = await(connector.revokeAccountAuthorities(revokeRequest, EORI("someEori")))
        result mustBe false
      }
    }

    "return false when the api fails" in new Setup {
      when(requestBuilder.withBody(any())(any(), any(), any())).thenReturn(requestBuilder)
      when(requestBuilder.setHeader(any[(String, String)]())).thenReturn(requestBuilder)
      when(mockHttpClient.post(any)(any)).thenReturn(requestBuilder)
      when(requestBuilder.execute(any, any)).thenReturn(Future.failed(new NotFoundException("error")))

      running(app) {
        val result = await(connector.revokeAccountAuthorities(revokeRequest, EORI("someEori")))
        result mustBe false
      }
    }
  }

  trait Setup {
    implicit val hc: HeaderCarrier     = HeaderCarrier()
    val mockHttpClient: HttpClientV2   = mock[HttpClientV2]
    val requestBuilder: RequestBuilder = mock[RequestBuilder]

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

    val app: Application = GuiceApplicationBuilder()
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

    val connector: Acc30Connector = app.injector.instanceOf[Acc30Connector]
  }
}
