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

import connectors.Acc27Connector
import org.mockito.ArgumentMatchers.{eq => meq}
import org.mockito.Mockito.when
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.*
import play.api.mvc.AnyContentAsJson
import play.api.test.*
import play.api.test.Helpers.*
import play.api.{Application, inject}
import utils.SpecBase

import scala.concurrent.*

class CustomsAccountsControllerSpec extends SpecBase {

  "get customs accounts" should {

    "return success response" in new Setup {
      running(app) {
        val result = route(app, request).value

        status(result) mustBe OK
        contentAsJson(result) mustBe Json.obj("response" -> expectedResponse)
      }
    }

    "return bad request error" when {
      "request bad request if EORINo not present" in {

        val app: Application = GuiceApplicationBuilder()
          .overrides()
          .configure(
            "microservice.metrics.enabled" -> false,
            "metrics.enabled"              -> false,
            "auditing.enabled"             -> false
          )
          .build()

        val invalidRequest: FakeRequest[AnyContentAsJson] =
          FakeRequest(POST, controllers.routes.CustomsAccountsController.getCustomsAccountsDod09().url)
            .withJsonBody(Json.obj("invalid" -> "request"))

        running(app) {
          val result = route(app, invalidRequest).value
          status(result) mustBe BAD_REQUEST
        }
      }
    }
  }

  trait Setup {
    val EORI                       = "testEORI"
    val expectedResponse: JsString = JsString("TheGoodResponse")
    val requestBody: JsObject      = Json.obj("EORINo" -> JsString(EORI))

    val request: FakeRequest[AnyContentAsJson] =
      FakeRequest(POST, controllers.routes.CustomsAccountsController.getCustomsAccountsDod09().url)
        .withJsonBody(requestBody)

    val mockAcc27Connector: Acc27Connector = mock[Acc27Connector]

    when(mockAcc27Connector.getAccounts(meq(requestBody)))
      .thenReturn(Future.successful(Json.obj("response" -> expectedResponse)))

    val app: Application = GuiceApplicationBuilder()
      .overrides(
        inject.bind[Acc27Connector].toInstance(mockAcc27Connector)
      )
      .configure(
        "microservice.metrics.enabled" -> false,
        "metrics.enabled"              -> false,
        "auditing.enabled"             -> false
      )
      .build()
  }
}
