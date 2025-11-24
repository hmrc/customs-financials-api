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

package controllers.metadata

import connectors.{DataStoreConnector, EmailThrottlerConnector}
import domain.SDESInputFormats.*
import domain.{Notification, NotificationsForEori}
import models.{EmailTemplate, HistoricDocumentRequestSearch, SearchResultStatus}
import play.api.libs.json.*
import play.api.mvc.{Action, ControllerComponents}
import play.api.{Logger, LoggerLike}
import play.mvc.Http
import services.cache.HistoricDocumentRequestSearchCacheService
import services.{DateTimeService, NotificationCache}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController
import utils.Utils.emptyString

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class MetadataController @Inject() (
  notificationCache: NotificationCache,
  emailThrottlerConnector: EmailThrottlerConnector,
  dataStore: DataStoreConnector,
  dateTimeService: DateTimeService,
  histDocReqSearchCacheService: HistoricDocumentRequestSearchCacheService,
  cc: ControllerComponents
)(implicit execution: ExecutionContext)
    extends BackendController(cc) {

  val log: LoggerLike = Logger(this.getClass)

  def addNotifications(): Action[Seq[Notification]] = Action.async(parse.json[Seq[Notification]]) { implicit req =>
    val notifications = req.body
    for {
      _      <- Future.successful(log.info(s"addNotifications: $notifications"))
      _      <- Future.sequence(notifications.map(checkHistDocSearchStatusAndSendEmail))
      _      <- Future.sequence(notifications.groupBy(_.eori).map { case (k, v) =>
                  notificationCache.putNotifications(NotificationsForEori(k, v, Some(dateTimeService.utcDateTime)))
                })
      result <- Future.successful(Ok(Json.obj("Status" -> "Ok")).as(Http.MimeTypes.JSON))
    } yield result
  }

  private def checkHistDocSearchStatusAndSendEmail(
    notification: Notification
  )(implicit hc: HeaderCarrier): Future[Boolean] = {
    val statementRequestID = notification.metadata.getOrElse("statementRequestID", emptyString)

    if (statementRequestID.nonEmpty) {
      val result = for {
        optHisDocReq <-
          histDocReqSearchCacheService.retrieveHistDocRequestSearchDocForStatementReqId(statementRequestID)
      } yield updateHistReqSearchDocumentAndSendMail(notification, statementRequestID, optHisDocReq)

      result.flatten
    } else {
      sendEmailIfVerified(notification)
    }
  }

  private def sendEmailIfVerified(notification: Notification)(implicit hc: HeaderCarrier): Future[Boolean] =
    dataStore.getVerifiedEmail
      .flatMap {
        case Some(emailAddress) =>
          val companyNameResult: Future[Option[String]] =
            dataStore.getCompanyName.recoverWith { case exc: Exception =>
              log.error(s"Company name retrieval failed with error ::${exc.getMessage}")
              Future(Some(emptyString))
            }

          companyNameResult.flatMap {
            case Some(companyName) =>
              val maybeEmailRequest = EmailTemplate.fromNotification(emailAddress, notification, companyName)
              maybeEmailRequest match {
                case Some(value) => emailThrottlerConnector.sendEmail(value.toEmailRequest)
                case None        =>
                  log.info("No end month/end year supplied from the metadata")
                  Future.successful(false)
              }

            case None =>
              log.info("Company name is not available.")
              Future.successful(false)
          }

        case None =>
          log.info(s"unable to obtain a verified email address for user: ${notification.eori}")
          Future.successful(false)
      }
      .recover { case err =>
        log.info(s"recover: unable to obtain a verified email address for user: ${err.getMessage}")
        false
      }

  private def updateHistReqSearchDocumentAndSendMail(
    notification: Notification,
    statementRequestID: String,
    optHisDocReq: Option[HistoricDocumentRequestSearch]
  )(implicit hc: HeaderCarrier): Future[Boolean] =
    optHisDocReq match {
      case Some(histDocReq) =>
        if (histDocReq.resultsFound == SearchResultStatus.inProcess) {

          val emailSentResult: Future[Boolean] = {
            histDocReqSearchCacheService.processSDESNotificationForStatReqId(histDocReq, statementRequestID).recover {
              case err =>
                log.error(
                  s"update failed for historic request search document and" +
                    s" error is ::: ${err.getMessage}"
                )
                histDocReq
            }

            log.info(s"sending email for statementRequestID ::: $statementRequestID")
            sendEmailIfVerified(notification)
          }

          emailSentResult
        } else {
          Future(false)
        }

      case _ => Future(false)
    }

}
