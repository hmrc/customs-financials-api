/*
 * Copyright 2021 HM Revenue & Customs
 *
 */

package controllers

import connectors.Acc27Connector
import play.api.libs.json.JsValue
import play.api.mvc.{Action, ControllerComponents, Request}
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class CustomsAccountsController @Inject()(acc27Connector: Acc27Connector,
                                          cc: ControllerComponents)
                                         (implicit ec: ExecutionContext) extends BackendController(cc) {

  def getCustomsAccountsDod09: Action[JsValue] = Action.async(parse.json) { implicit request =>
    if (eoriPresent)
      acc27Connector.getAccounts(request.body, hc.requestId).map(Ok(_))
    else
      Future.successful(BadRequest)
  }

  private def eoriPresent()(implicit request: Request[JsValue]): Boolean =
    (request.body \\ "EORINo").headOption.nonEmpty
}


