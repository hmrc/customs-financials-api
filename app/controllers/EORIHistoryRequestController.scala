/*
 * Copyright 2021 HM Revenue & Customs
 *
 */

package controllers

import connectors.Sub21Connector
import models.EORI
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import play.api.{Logger, LoggerLike}
import uk.gov.hmrc.http.UpstreamErrorResponse
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class EORIHistoryRequestController @Inject()(connector: Sub21Connector,
                                             cc: ControllerComponents)(implicit ec: ExecutionContext) extends BackendController(cc) {

  val log: LoggerLike = Logger(this.getClass)

  def validateEORI(eori: EORI): Action[AnyContent] = Action.async {
    connector.getEORIHistory(eori).map(eoriHistory => {
      eoriHistory.getEORIHistoryResponse.responseCommon.status match {
        case "OK" => Ok
      }
    }).recover {
      case UpstreamErrorResponse(_, NOT_FOUND, _, _) => NotFound
    }
  }
}
