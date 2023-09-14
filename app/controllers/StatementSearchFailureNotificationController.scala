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

import controllers.actions.{AuthorizationHeaderFilter, MdgHeaderFilter}
import models.responses.StatementSearchFailureNotificationErrorResponse
import play.api.libs.json.JsValue
import play.api.mvc.{Action, ControllerComponents}
import services.cache.HistoricDocumentRequestSearchCacheService
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController
import utils.JSONSchemaValidator
import utils.Utils.writable

import javax.inject.Inject
import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}

class StatementSearchFailureNotificationController @Inject()(
                                                              cc: ControllerComponents,
                                                              jsonSchemaValidator: JSONSchemaValidator,
                                                              authorizationHeaderFilter: AuthorizationHeaderFilter,
                                                              mdgHeaderFilter: MdgHeaderFilter,
                                                              cacheService:HistoricDocumentRequestSearchCacheService
                                                            )(implicit execution: ExecutionContext)
  extends BackendController(cc) {

  def processNotification(): Action[JsValue] = (authorizationHeaderFilter andThen mdgHeaderFilter)(parse.json) {
    implicit request =>
      jsonSchemaValidator.validatePayload(request.body,
        "/schemas/statement-search-failure-notification-request-schema.json") match {
        case Success(_) =>
          updateHistoricDocumentRequestSearchForStatReqId(request.body)
          NoContent
        case Failure(errors) =>
          import StatementSearchFailureNotificationErrorResponse.ssfnErrorResponseFormat
          BadRequest(buildErrorResponse(errors, request.headers.get("X-Correlation-ID").getOrElse("")))
      }
  }

private def buildErrorResponse(errors: Throwable, correlationId: String) = {
  StatementSearchFailureNotificationErrorResponse(errors,correlationId)
}
  private def updateHistoricDocumentRequestSearchForStatReqId(reqJsValue: JsValue): Unit = ()
}
