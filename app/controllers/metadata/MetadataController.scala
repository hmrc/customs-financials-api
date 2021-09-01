/*
 * Copyright 2021 HM Revenue & Customs
 *
 */

package controllers.metadata

import connectors.{DataStoreConnector, EmailThrottlerConnector}
import domain.{Notification, NotificationsForEori, SDESInputFormats}
import play.api.libs.json._
import play.api.mvc.{Action, ControllerComponents}
import play.api.{Logger, LoggerLike}
import play.mvc.Http
import services.{DateTimeService, NotificationCache}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}
import SDESInputFormats._
import models.EmailTemplate


@Singleton
class MetadataController @Inject()(
                                    notificationCache: NotificationCache,
                                    emailThrottlerConnector: EmailThrottlerConnector,
                                    dataStore: DataStoreConnector,
                                    dateTimeService: DateTimeService,
                                    cc: ControllerComponents
                                  )(implicit execution: ExecutionContext) extends BackendController(cc) {

  val log: LoggerLike = Logger(this.getClass)

  def addNotifications(): Action[Seq[Notification]] = Action.async(parse.json[Seq[Notification]]) { implicit req =>
    val notifications = req.body
    for {
      _ <- Future.successful(log.info(s"addNotifications: $notifications"))
      _ <- Future.sequence(notifications.map(sendEmailIfVerified))
      _ <- Future.sequence(notifications.groupBy(_.eori).map { case (k, v) =>
        notificationCache.putNotifications(NotificationsForEori(k, v, Some(dateTimeService.utcDateTime)))
      })
      result <- Future.successful(Ok(Json.obj("Status" -> "Ok")).as(Http.MimeTypes.JSON))
    } yield result
  }

  private def sendEmailIfVerified(notification: Notification)(implicit hc: HeaderCarrier): Future[Boolean] = {
    dataStore.getVerifiedEmail(notification.eori).flatMap {
      case Some(emailAddress) =>
        val maybeEmailRequest = EmailTemplate.fromNotification(emailAddress, notification)
        maybeEmailRequest match {
          case Some(value) => emailThrottlerConnector.sendEmail(value.toEmailRequest)
          case None => log.info("No end month/end year supplied from the metadata"); Future.successful(false)
        }
      case None =>
        log.info(s"unable to obtain a verified email address for user: ${notification.eori}")
        Future.successful(false)
    }.recover {
      case err =>
        log.info(s"recover: unable to obtain a verified email address for user: ${err.getMessage}")
        false
    }
  }
}
