/*
 * Copyright 2021 HM Revenue & Customs
 *
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
