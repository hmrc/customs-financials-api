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

import connectors.Acc41Connector
import domain.{Acc41ErrorResponse, AuthoritiesCsvGenerationResponse, InitiateAuthoritiesCsvGenerationRequest}
import models.EORI
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json
import play.api.mvc.AnyContentAsJson
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import play.api.{Application, inject}
import utils.SpecBase

import scala.concurrent.Future

class AuthoritiesCsvGenerationControllerSpec extends SpecBase {

  "initiateAuthoritiesCsvGeneration" should {

    "return INTERNAL_SERVER_ERROR when request returned error response" in new Setup {
      when(mockConnector.initiateAuthoritiesCSV(any, any)(any))
        .thenReturn(Future.successful(Left(Acc41ErrorResponse)))

      running(app) {
        val result = route(app, request).value
        status(result) mustBe INTERNAL_SERVER_ERROR
      }
    }

    "return 200 with requestAcceptedDate request successful" in new Setup {
      when(mockConnector.initiateAuthoritiesCSV(any, any)(any))
        .thenReturn(Future.successful(Right(AuthoritiesCsvGenerationResponse(Some("020-06-09T21:59:56Z")))))

      running(app) {
        val result = route(app, request).value
        status(result) mustBe OK
        contentAsJson(result) mustBe Json.toJson(AuthoritiesCsvGenerationResponse(Some("020-06-09T21:59:56Z")))
      }
    }

    "return InternalServerError when request returned with AuthoritiesCsvGenerationResponse" in new Setup {
      when(mockConnector.initiateAuthoritiesCSV(any, any)(any))
        .thenReturn(Future.successful(Left(AuthoritiesCsvGenerationResponse(Some("020-06-09T21:59:56Z")))))

      running(app) {
        val result = route(app, request).value
        status(result) mustBe INTERNAL_SERVER_ERROR
      }
    }
  }

  trait Setup {
    val mockConnector: Acc41Connector = mock[Acc41Connector]

    val response: AuthoritiesCsvGenerationResponse = AuthoritiesCsvGenerationResponse(Some("020-06-09T21:59:56Z"))

    val frontendRequest: InitiateAuthoritiesCsvGenerationRequest = InitiateAuthoritiesCsvGenerationRequest(
      EORI("someEori"), Some(EORI("someAltEori")))

    val request: FakeRequest[AnyContentAsJson] =
      FakeRequest("POST", routes.AuthoritiesCsvGenerationController.initiateAuthoritiesCsvGeneration().url)
        .withJsonBody(Json.toJson(frontendRequest))

    val app: Application = GuiceApplicationBuilder().overrides(
      inject.bind[Acc41Connector].toInstance(mockConnector)
    ).build()
  }
}
