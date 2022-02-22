/*
 * Copyright 2022 HM Revenue & Customs
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

import config.AppConfig
import models.dec64.Dec64SubmissionPayload
import play.api.Application
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.Helpers._
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient, HttpResponse, InternalServerException}
import utils.SpecBase
import scala.concurrent.Future

class Dec64ConnectorSpec extends SpecBase {

  "submitFileUpload" should {
    "return true on a successful file upload DEC64 POST" in new Setup {

      when[Future[HttpResponse]](mockHttpClient.POSTString(any, any, any)(any, any, any))
        .thenReturn(Future.successful(HttpResponse(NO_CONTENT, "")))

      running(app) {
        val result = await(connector.submitFileUpload(Dec64SubmissionPayload("", Seq())))
        result mustBe true
      }
    }

    "return false when fails file upload DEC64 POST" in new Setup {
      when[Future[HttpResponse]](mockHttpClient.POSTString(any, any, any)(any, any, any))
        .thenReturn(Future.successful(HttpResponse(BAD_REQUEST)))

      running(app) {
        val result = await(connector.submitFileUpload(Dec64SubmissionPayload("", Seq())))
        result mustBe false
      }
    }

    "return false when exception thrown for file upload DEC64 POST" in new Setup {
      when[Future[HttpResponse]](mockHttpClient.POSTString(any, any, any)(any, any, any))
        .thenReturn(Future.failed(new InternalServerException("boom")))

      running(app) {
        val result = await(connector.submitFileUpload(Dec64SubmissionPayload("", Seq())))
        result mustBe false
      }
    }
  }

  trait Setup {
    implicit val hc: HeaderCarrier = HeaderCarrier()
    val mockHttpClient: HttpClient = mock[HttpClient]
    val mockAppConfig: AppConfig = mock[AppConfig]

    val app: Application = GuiceApplicationBuilder().overrides(
      bind[HttpClient].toInstance(mockHttpClient)
    ).configure(
      "microservice.metrics.enabled" -> false,
      "metrics.enabled" -> false,
      "auditing.enabled" -> false
    ).build()

    val connector: Dec64Connector = app.injector.instanceOf[Dec64Connector]
  }
}
