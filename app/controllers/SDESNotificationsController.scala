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

import domain.NotificationsForEori
import models.{EORI, FileRole}
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import play.api.{Logger, LoggerLike}
import services.{DateTimeService, NotificationCache}
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class SDESNotificationsController @Inject() (
  notificationCache: NotificationCache,
  authorisedRequest: AuthorisedRequest,
  dateTimeService: DateTimeService,
  cc: ControllerComponents
)(implicit ec: ExecutionContext)
    extends BackendController(cc)
    with ControllerChecks {

  val log: LoggerLike = Logger(this.getClass)

  def getNotifications(eori: EORI): Action[AnyContent] = authorisedRequest async { implicit req =>
    matchingEoriNumber(eori) { verifiedEori =>
      notificationCache
        .getNotifications(verifiedEori)
        .map(_.getOrElse(NotificationsForEori(verifiedEori, Nil, Some(dateTimeService.utcDateTime))))
        .map(notification => Ok(Json.toJson(notification)))
    }
  }

  def deleteRequestedNotifications(eori: EORI, fileRole: FileRole): Action[AnyContent] = authorisedRequest async {
    implicit req =>
      matchingEoriNumber(eori) { verifiedEori =>
        notificationCache
          .removeRequestedByFileRole(verifiedEori, fileRole)
          .map(_ => Ok(Json.obj("Status" -> "Ok")))
      }
  }

  def deleteNonRequestedNotifications(eori: EORI, fileRole: FileRole): Action[AnyContent] = authorisedRequest async {
    implicit req =>
      matchingEoriNumber(eori) { verifiedEori =>
        notificationCache
          .removeByFileRole(verifiedEori, fileRole)
          .map(_ => Ok(Json.obj("Status" -> "Ok")))
      }
  }
}
