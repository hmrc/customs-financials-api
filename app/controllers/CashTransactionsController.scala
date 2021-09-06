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

package controllers

import domain.CashDailyStatement._
import models.{ErrorResponse, ExceededThresholdErrorException, NoAssociatedDataException}
import play.api.libs.json.{JsValue, Json, OFormat}
import play.api.mvc.{Action, ControllerComponents, Result}
import services.CashTransactionsService
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import java.time.LocalDate
import javax.inject.Inject
import scala.concurrent.ExecutionContext

class CashTransactionsController @Inject()(service: CashTransactionsService,
                                           cc: ControllerComponents)(implicit ec: ExecutionContext) extends BackendController(cc) {

  def getSummary: Action[JsValue] = Action.async(parse.json) { implicit request =>
    withJsonBody[CashTransactionsRequest] { cashTransactionsRequest =>
      service.retrieveCashTransactionsSummary(cashTransactionsRequest.can, cashTransactionsRequest.from, cashTransactionsRequest.to)
        .map {
          case Right(cashDailyStatements) => Ok(Json.toJson(cashDailyStatements))
          case Left(errorValue) => failedResponse(errorValue)
        }
    }
  }

  def getDetail: Action[JsValue] = Action.async(parse.json) { implicit request =>
    withJsonBody[CashTransactionsRequest] { cashTransactionsRequest =>
      service.retrieveCashTransactionsDetail(cashTransactionsRequest.can, cashTransactionsRequest.from, cashTransactionsRequest.to)
        .map {
          case Right(cashDailyStatements) => Ok(Json.toJson(cashDailyStatements))
          case Left(errorValue) => failedResponse(errorValue)
        }
    }
  }

  private def failedResponse(errorResponse: ErrorResponse): Result = errorResponse match {
    case NoAssociatedDataException => NotFound
    case ExceededThresholdErrorException => EntityTooLarge
  }
}

case class CashTransactionsRequest(can: String, from: LocalDate, to: LocalDate)

object CashTransactionsRequest {
  implicit val cashTransactionRequestFormat: OFormat[CashTransactionsRequest] = Json.format[CashTransactionsRequest]
}
