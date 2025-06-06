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
import domain.{Acc41ErrorResponse, InitiateAuthoritiesCsvGenerationRequest}
import play.api.libs.json.Json
import play.api.mvc.{Action, ControllerComponents}
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class AuthoritiesCsvGenerationController @Inject() (acc41Connector: Acc41Connector, cc: ControllerComponents)(implicit
  ec: ExecutionContext
) extends BackendController(cc) {

  def initiateAuthoritiesCsvGeneration: Action[InitiateAuthoritiesCsvGenerationRequest] =
    Action.async(parse.json[InitiateAuthoritiesCsvGenerationRequest]) { implicit request =>
      acc41Connector.initiateAuthoritiesCSV(request.body.requestingEori, request.body.alternateEORI).map {
        case Left(Acc41ErrorResponse) => InternalServerError
        case Right(value)             => Ok(Json.toJson(value))
        case _                        => InternalServerError
      }
    }
}
