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

import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import utils.SpecBase
import uk.gov.hmrc.http.client.HttpClientV2

class AuthorisedRequestSpec extends SpecBase {
  "CustomAuthConnector" should {

    "return correct serviceUrl and httpClientV2" in {
      val app: Application = GuiceApplicationBuilder()
        .configure(
          "microservice.metrics.enabled" -> false,
          "metrics.enabled"              -> false,
          "auditing.enabled"             -> false
        )
        .build()

      val customAuthConnector: CustomAuthConnector = app.injector.instanceOf[CustomAuthConnector]

      customAuthConnector.httpClientV2 mustBe a[HttpClientV2]
      customAuthConnector.serviceUrl mustBe "http://localhost:8500"
    }
  }
}
