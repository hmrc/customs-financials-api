/*
 * Copyright 2021 HM Revenue & Customs
 *
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


