/*
 * Copyright 2021 HM Revenue & Customs
 *
 */

package controllers


import domain.AccountWithAuthorities
import models.requests.manageAuthorities.{GrantAuthorityRequest, RevokeAuthorityRequest}
import play.api.Logger
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import services.AccountAuthorityService
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import javax.inject.Inject
import scala.concurrent.ExecutionContext
import scala.util.control.NonFatal

class AccountAuthoritiesController @Inject()(service: AccountAuthorityService,
                                             authorisedRequest: AuthorisedRequest,
                                             cc: ControllerComponents)(implicit ec: ExecutionContext) extends BackendController(cc) {

  val log: Logger = Logger(this.getClass)

  def get: Action[AnyContent] = authorisedRequest async { implicit request: RequestWithEori[AnyContent] =>
    service.getAccountAuthorities(request.eori)
      .map { accountWithAuthorities: Seq[AccountWithAuthorities] =>
        Ok(Json.toJson(accountWithAuthorities))
      }
      .recover {
        case ex if ex.getMessage.contains("JSON validation") =>
          log.error(s"getAccountAuthorities failed: ${ex.getMessage}")
          InternalServerError("JSON Validation Error")
        case NonFatal(error) =>
          log.error(s"getAccountAuthorities failed: ${error.getMessage}")
          ServiceUnavailable
      }
  }

  def grant: Action[JsValue] = authorisedRequest.async(parse.json) { implicit request: RequestWithEori[JsValue] =>
    withJsonBody[GrantAuthorityRequest] { grantAuthorityRequest =>
      service.grantAccountAuthorities(grantAuthorityRequest, request.eori).map {
        case true => NoContent
        case false => InternalServerError
      }
    }
  }

  def revoke: Action[JsValue] = authorisedRequest.async(parse.json) { implicit request: RequestWithEori[JsValue] =>
    withJsonBody[RevokeAuthorityRequest] { revokeAuthorityRequest =>
      service.revokeAccountAuthorities(revokeAuthorityRequest, request.eori).map {
        case true => NoContent
        case false => InternalServerError
      }
    }
  }

}
