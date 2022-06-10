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

package controllers

import connectors.Acc40Connector
import domain.{AuthoritiesFound, ErrorResponse, NoAuthoritiesFound, SearchAuthoritiesRequest}
import models.EORI
import play.api.{Application, inject}
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json
import play.api.mvc.AnyContentAsJson
import play.api.test.FakeRequest
import utils.SpecBase
import play.api.test.Helpers._

import scala.concurrent.Future

class SearchAuthoritiesControllerSpec extends SpecBase {

  "searchAuthorities" should {
    "return NO_CONTENT if no authorities returned" in new Setup {
      when(mockConnector.searchAuthorities(any, any))
        .thenReturn(Future.successful(Left(NoAuthoritiesFound)))

      running(app) {
        val result = route(app, request).value
        status(result) mustBe NO_CONTENT
      }
    }

    "return INTERNAL_SERVER_ERROR if an error response returns from the connector" in new Setup {
      when(mockConnector.searchAuthorities(any, any))
        .thenReturn(Future.successful(Left(ErrorResponse)))

      running(app) {
        val result = route(app, request).value
        status(result) mustBe INTERNAL_SERVER_ERROR
      }
    }

    "return OK with authorities if authorities returns from the connector" in new Setup {
      when(mockConnector.searchAuthorities(any, any))
        .thenReturn(Future.successful(Right(authorities)))

      running(app) {
        val result = route(app, request).value
        status(result) mustBe OK
      }
    }
  }

  trait Setup {
    val mockConnector: Acc40Connector = mock[Acc40Connector]

    val authorities: AuthoritiesFound = AuthoritiesFound(Some(1), Some(Seq.empty), None, None)

    val frontendRequest: SearchAuthoritiesRequest = SearchAuthoritiesRequest(EORI("someEori"), EORI("otherEori"))

    val request: FakeRequest[AnyContentAsJson] = FakeRequest("POST", routes.SearchAuthoritiesController.searchAuthorities().url)
      .withJsonBody(Json.toJson(frontendRequest))

    val app: Application = GuiceApplicationBuilder().overrides(
      inject.bind[Acc40Connector].toInstance(mockConnector)
    ).build()
  }
}
