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

package controllers.definition

import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import utils.SpecBase

class DocumentationControllerSpec extends SpecBase {

  "DocumentController" should {
    "return controllers.definition view" in {

      val app: Application = GuiceApplicationBuilder()
        .configure(
          "microservice.metrics.enabled" -> false,
          "metrics.enabled"              -> false,
          "auditing.enabled"             -> false
        )
        .build()

      val view = views.txt.definition().toString

      running(app) {
        val result =
          route(app, FakeRequest(GET, controllers.definition.routes.DocumentationController.definition().url)).value

        contentAsString(result) mustBe view
      }
    }
  }
}
