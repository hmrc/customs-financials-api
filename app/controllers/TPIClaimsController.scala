/*
 * Copyright 2022 HM Revenue & Customs
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

import domain.tpi01.GetReimbursementClaimsResponse
import domain.tpi02.GetSpecificClaimResponse
import javax.inject.Inject
import models.requests.{ReimbursementClaimsRequest, SpecificClaimRequest}
import play.api.Logger
import play.api.libs.json.Json
import play.api.mvc.{Action, ControllerComponents}
import services.TPIClaimsService
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import scala.concurrent.ExecutionContext
import scala.util.control.NonFatal

class TPIClaimsController @Inject()(service: TPIClaimsService,
                                    cc: ControllerComponents)(implicit ec: ExecutionContext) extends BackendController(cc) {

  val log: Logger = Logger(this.getClass)

    def getReimbursementClaims: Action[ReimbursementClaimsRequest] = Action.async(parse.json[ReimbursementClaimsRequest]) { implicit request =>
    service.getClaims(request.body.eori)
      .map {
        case response if response.getReimbursementClaimsResponse.mdtpError => InternalServerError
        case domain.tpi01.Response(GetReimbursementClaimsResponse(_, Some(responseDetail)))
          if responseDetail.CDFPayClaimsFound => Ok(Json.toJson(responseDetail))
        case _ => NoContent
      }
      .recover {
        case ex if ex.getMessage.contains("JSON validation") =>
          log.error(s"getReimbursementClaims failed: ${ex.getMessage}")
          InternalServerError("JSON Validation Error")
        case NonFatal(error) =>
          log.error(s"getReimbursementClaims failed: ${error.getMessage}")
          ServiceUnavailable
      }
  }

  def getSpecificClaim: Action[SpecificClaimRequest] = Action.async(parse.json[SpecificClaimRequest]) { implicit request =>
      service.getSpecificClaim(request.body.cdfPayService, request.body.cdfPayCaseNumber)
        .map {
          case domain.tpi02.Response(GetSpecificClaimResponse(_, Some(responseDetail))) => Ok(Json.toJson(responseDetail))
          case domain.tpi02.Response(GetSpecificClaimResponse(_, None)) => NoContent
          case _ => InternalServerError
        }
        .recover {
          case ex if ex.getMessage.contains("JSON validation") =>
            log.error(s"getSpecificClaim failed: ${ex.getMessage}")
            InternalServerError("JSON Validation Error")
          case NonFatal(error) =>
            log.error(s"getSpecificClaim failed: ${error.getMessage}")
            ServiceUnavailable
        }
    }
}
