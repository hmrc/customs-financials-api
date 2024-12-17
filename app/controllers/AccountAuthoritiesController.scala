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

import domain.AccountWithAuthorities
import models.EORI
import models.requests.manageAuthorities.{GrantAuthorityRequest, RevokeAuthorityRequest}
import play.api.Logger
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import services.AccountAuthorityService
import uk.gov.hmrc.http.UpstreamErrorResponse
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import javax.inject.Inject
import scala.concurrent.ExecutionContext
import scala.util.control.NonFatal

class AccountAuthoritiesController @Inject() (
  service: AccountAuthorityService,
  authorisedRequest: AuthorisedRequest,
  cc: ControllerComponents
)(implicit ec: ExecutionContext)
    extends BackendController(cc) {

  val log: Logger = Logger(this.getClass)

  def get(eori: EORI): Action[AnyContent] = authorisedRequest async {

    service
      .getAccountAuthorities(eori)
      .map { (accountWithAuthorities: Seq[AccountWithAuthorities]) =>
        Ok(Json.toJson(accountWithAuthorities))
      }
      .recover {
        case UpstreamErrorResponse(msg, INTERNAL_SERVER_ERROR, _, _) if msg.contains("JSON validation") =>
          log.error(s"getAccountAuthorities failed: $msg")
          InternalServerError("JSON Validation Error")

        case UpstreamErrorResponse(msg, BAD_REQUEST, _, _) if hasNoAccountsForEoriMsg(msg) =>
          log.error(s"Bad Request as no accounts found related to ${eori.value}")
          Ok(Json.toJson(Seq.empty[AccountWithAuthorities]))

        case UpstreamErrorResponse(msg, status_code, _, _) =>
          log.error(s"getAccountAuthorities failed with status code: $status_code and error is : $msg")
          ServiceUnavailable

        case NonFatal(error) =>
          log.error(s"getAccountAuthorities failed: ${error.getMessage}")
          ServiceUnavailable
      }
  }

  def grant(eori: EORI): Action[JsValue] = authorisedRequest.async(parse.json) {
    implicit request: RequestWithEori[JsValue] =>
      withJsonBody[GrantAuthorityRequest] { grantAuthorityRequest =>
        service.grantAccountAuthorities(grantAuthorityRequest, eori).map {
          case true  => NoContent
          case false => InternalServerError
        }
      }
  }

  def revoke(eori: EORI): Action[JsValue] = authorisedRequest.async(parse.json) {
    implicit request: RequestWithEori[JsValue] =>
      withJsonBody[RevokeAuthorityRequest] { revokeAuthorityRequest =>
        service.revokeAccountAuthorities(revokeAuthorityRequest, eori).map {
          case true  => NoContent
          case false => InternalServerError
        }
      }
  }

  private def hasNoAccountsForEoriMsg(exceptionMsg: String): Boolean =
    exceptionMsg.contains("could not find accounts related to eori")
}
