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

import domain.CashDailyStatement._
import models.requests.CashAccountTransactionSearchRequestDetails
import models.responses.ErrorDetail
import models.responses.EtmpErrorCode._
import models.{ErrorResponse, ExceededThresholdErrorException, NoAssociatedDataException}
import play.api.libs.json.{JsValue, Json, OFormat}
import play.api.mvc.{Action, ControllerComponents, Result}
import services.CashTransactionsService
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController
import utils.Utils.writable

import java.time.LocalDate
import javax.inject.Inject
import scala.concurrent.ExecutionContext

class CashTransactionsController @Inject()(service: CashTransactionsService,
                                           cc: ControllerComponents)
                                          (implicit ec: ExecutionContext) extends BackendController(cc) {

  def getSummary: Action[JsValue] = Action.async(parse.json) { implicit request =>
    withJsonBody[CashTransactionsRequest] { cashTransactionsReq =>
      service.retrieveCashTransactionsSummary(cashTransactionsReq.can, cashTransactionsReq.from, cashTransactionsReq.to)
        .map {
          case Right(cashDailyStatements) => Ok(Json.toJson(cashDailyStatements))
          case Left(errorValue) => failedResponse(errorValue)
        }
    }
  }

  def getDetail: Action[JsValue] = Action.async(parse.json) { implicit request =>
    withJsonBody[CashTransactionsRequest] { cashTransactionsReq =>
      service.retrieveCashTransactionsDetail(cashTransactionsReq.can, cashTransactionsReq.from, cashTransactionsReq.to)
        .map {
          case Right(cashDailyStatements) => Ok(Json.toJson(cashDailyStatements))
          case Left(errorValue) => failedResponse(errorValue)
        }
    }
  }

  def retrieveCashAccountTransactions(): Action[JsValue] = Action.async(parse.json) { implicit request =>
    withJsonBody[CashAccountTransactionSearchRequestDetails] { cashTransactionsSearchReq =>

      service.retrieveCashAccountTransactions(cashTransactionsSearchReq).map {
        case Right(cashAccTransSearchResponse) => Ok(Json.toJson(cashAccTransSearchResponse))
        case Left(errorDetail) => checkErrorCodeAndCreateResponse(errorDetail)
      }
    }
  }

  private def checkErrorCodeAndCreateResponse(errorDetail: ErrorDetail): Result = {
    val etmpErrorCodes = List(code001, code002, code003, code004, code005)

    errorDetail.errorCode match {
      case "400" => BadRequest(errorDetail)
      case "500" => InternalServerError(errorDetail)
      case etmpErrorCode if (etmpErrorCodes.contains(etmpErrorCode)) => PreconditionFailed(errorDetail)
      case _ => ServiceUnavailable(errorDetail)
    }
  }

  private def failedResponse(errorResponse: ErrorResponse): Result = errorResponse match {
    case NoAssociatedDataException => NotFound
    case ExceededThresholdErrorException => EntityTooLarge
  }
}

case class CashTransactionsRequest(can: String,
                                   from: LocalDate,
                                   to: LocalDate)

object CashTransactionsRequest {
  implicit val cashTransactionRequestFormat: OFormat[CashTransactionsRequest] = Json.format[CashTransactionsRequest]
}
