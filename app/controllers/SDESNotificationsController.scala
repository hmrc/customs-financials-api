/*
 * Copyright 2021 HM Revenue & Customs
 *
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
class SDESNotificationsController @Inject()(notificationCache: NotificationCache,
                                            authorisedRequest: AuthorisedRequest,
                                            dateTimeService: DateTimeService,
                                            cc: ControllerComponents)(implicit ec: ExecutionContext) extends BackendController(cc) with ControllerChecks {

  val log: LoggerLike = Logger(this.getClass)

  def getNotifications(eori: EORI): Action[AnyContent] = authorisedRequest async { implicit req =>
    matchingEoriNumber(eori) { verifiedEori =>
      notificationCache.getNotifications(verifiedEori)
        .map(_.getOrElse(NotificationsForEori(verifiedEori, Nil, Some(dateTimeService.utcDateTime))))
        .map(notification => Ok(Json.toJson(notification)))
    }
  }


  def deleteRequestedNotifications(eori: EORI, fileRole: FileRole): Action[AnyContent] = authorisedRequest async { implicit req =>
    matchingEoriNumber(eori) { verifiedEori =>
      notificationCache.removeRequestedByFileRole(verifiedEori, fileRole)
        .map(_ => Ok(Json.obj("Status" -> "Ok")))
    }
  }

  def deleteNonRequestedNotifications(eori: EORI, fileRole: FileRole): Action[AnyContent] = authorisedRequest async { implicit req =>
    matchingEoriNumber(eori) { verifiedEori =>
      notificationCache.removeByFileRole(verifiedEori, fileRole)
        .map(_ => Ok(Json.obj("Status" -> "Ok")))
    }
  }
}
