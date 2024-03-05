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

import domain.acc38.GetCorrespondenceAddressResponse
import models.requests.{GetContactDetailsRequest, UpdateContactDetailsRequest}
import models.responses.UpdateContactDetailsResponse
import play.api.libs.json.Json
import play.api.{Logger, LoggerLike}
import play.api.mvc.{Action, ControllerComponents}
import services.AccountContactDetailsService
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController
import javax.inject.Inject
import scala.concurrent.ExecutionContext

class DutyDefermentContactDetailsController @Inject()(service: AccountContactDetailsService,
                                                      cc: ControllerComponents)
                                                     (implicit ec: ExecutionContext) extends BackendController(cc) {

  private val log: LoggerLike = Logger(this.getClass)

  def getContactDetails: Action[GetContactDetailsRequest] =
    Action.async(parse.json[GetContactDetailsRequest]) { implicit request =>
      service.getAccountContactDetails(request.body.dan, request.body.eori)
        .map {
          case response if response.mdtpError => InternalServerError

          case domain.acc38.Response(GetCorrespondenceAddressResponse(_, Some(responseDetail))) =>
            Ok(Json.toJson(responseDetail.contactDetails))

          case domain.acc38.Response(GetCorrespondenceAddressResponse(_, None)) => BadRequest
        }
        .recover {
          case e =>
            log.error(s"getDutyDefermentContactDetails failed: ${e.getMessage}")
            ServiceUnavailable
        }
    }

  def updateContactDetails(): Action[UpdateContactDetailsRequest] =
    Action.async(parse.json[UpdateContactDetailsRequest]) { implicit request =>
      service.updateAccountContactDetails(
        request.body.dan,
        request.body.eori,
        domain.acc37.ContactDetails.fromRequest(request.body)
      ).map {
        case response if response.mdtpError => InternalServerError
        case _ => Ok(Json.toJson(UpdateContactDetailsResponse(true)))
      }.recover {
        case e =>
          log.error(s"Updating contact details failed: ${e.getMessage}")
          ServiceUnavailable
      }
    }
}
