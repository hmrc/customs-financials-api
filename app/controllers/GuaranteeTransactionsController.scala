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

import models.{ErrorResponse, ExceededThresholdErrorException, NoAssociatedDataException}
import models.requests.GuaranteeAccountTransactionsRequest
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.{Action, ControllerComponents, Result}
import services.GuaranteeTransactionsService
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class GuaranteeTransactionsController @Inject()(service: GuaranteeTransactionsService,
                                                cc: ControllerComponents)(implicit ec: ExecutionContext) extends BackendController(cc) {

  def retrieveOpenGuaranteeTransactionsSummary(): Action[JsValue] = Action.async(parse.json) { implicit request =>
    withJsonBody[GuaranteeAccountTransactionsRequest] { guaranteeAccountTransactionsRequest =>
      service.retrieveGuaranteeTransactionsSummary(guaranteeAccountTransactionsRequest)
        .map {
          case Right(transactions) => Ok(Json.toJson(transactions))
          case Left(errResponse) => failedResponse(errResponse)
        }
    }
  }

  def retrieveOpenGuaranteeTransactionsDetail(): Action[JsValue] = Action.async(parse.json) { implicit request =>
    withJsonBody[GuaranteeAccountTransactionsRequest] { guaranteeAccountTransactionsRequest =>
      service.retrieveGuaranteeTransactionsDetail(guaranteeAccountTransactionsRequest)
        .map {
          case Right(transactions) => Ok(Json.toJson(transactions))
          case Left(errResponse) => failedResponse(errResponse)
        }
    }
  }

  private def failedResponse(errorResponse: ErrorResponse): Result = errorResponse match {
    case NoAssociatedDataException => NotFound
    case ExceededThresholdErrorException => EntityTooLarge
    case _ => ServiceUnavailable
  }
}


