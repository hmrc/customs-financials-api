/*
 * Copyright 2021 HM Revenue & Customs
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

import models.requests.EmailRequest
import play.api.Application
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.Helpers._
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient, HttpResponse, NotFoundException}
import utils.SpecBase

import scala.concurrent.Future

class EmailThrottlerConnectorSpec extends SpecBase {

  "return true when the api responds with 202" in new Setup {
    when[Future[HttpResponse]](mockHttpClient.POST(any, any, any)(any, any, any, any))
      .thenReturn(Future.successful(HttpResponse(ACCEPTED, "")))

    running(app) {
      val result = await(connector.sendEmail(request))
      result mustBe true
    }
  }
  "return false when the api responds with a successful response that isn't 204" in new Setup {
    when[Future[HttpResponse]](mockHttpClient.POST(any, any, any)(any, any, any, any))
      .thenReturn(Future.successful(HttpResponse(OK, "")))

    running(app) {
      val result = await(connector.sendEmail(request))
      result mustBe false
    }
  }
  "return false when the api fails" in new Setup {
    when[Future[HttpResponse]](mockHttpClient.POST(any, any, any)(any, any, any, any))
      .thenReturn(Future.failed(new NotFoundException("error")))

    running(app) {
      val result = await(connector.sendEmail(request))
      result mustBe false
    }
  }

  trait Setup {
    implicit val hc: HeaderCarrier = HeaderCarrier()
    val mockHttpClient: HttpClient = mock[HttpClient]


    val request: EmailRequest = EmailRequest(List.empty, "", Map.empty, force = true, None, Some("eori"), None)

    val app: Application = GuiceApplicationBuilder().overrides(
      bind[HttpClient].toInstance(mockHttpClient)
    ).configure(
      "microservice.metrics.enabled" -> false,
      "metrics.enabled" -> false,
      "auditing.enabled" -> false
    ).build()

    val connector: EmailThrottlerConnector = app.injector.instanceOf[EmailThrottlerConnector]
  }

}
