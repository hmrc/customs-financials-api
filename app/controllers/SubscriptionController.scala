/*
 * Copyright 2021 HM Revenue & Customs
 *
 */

package controllers

import play.api.Logger
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import services.SubscriptionService
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import javax.inject.Inject
import scala.concurrent.ExecutionContext
import scala.util.control.NonFatal

class SubscriptionController @Inject()(service: SubscriptionService,
                                       authorisedRequest: AuthorisedRequest,
                                       cc: ControllerComponents)(implicit ec: ExecutionContext) extends BackendController(cc) {

  val log: Logger = Logger(this.getClass)

  def getVerifiedEmail: Action[AnyContent] = authorisedRequest async { implicit request: RequestWithEori[AnyContent] =>

    service.getVerifiedEmail(request.eori)
      .map(response => Ok(Json.toJson(response)))
      .recover {
        case NonFatal(error) =>
          log.error(s"getSubscriptions failed: ${error.getMessage}")
          ServiceUnavailable
      }
  }

}
