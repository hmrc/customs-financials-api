/*
 * Copyright 2021 HM Revenue & Customs
 *
 */

package domain

import models.EORI
import org.joda.time.DateTime
import play.api.libs.json._
import uk.gov.hmrc.mongo.play.json.formats.MongoJodaFormats

case class NotificationsForEori(
                                 eori: EORI,
                                 notifications: Seq[Notification],
                                 lastUpdated: Option[DateTime]
                               )

object NotificationsForEori {
  implicit val lastUpdatedFormat: Format[DateTime] = MongoJodaFormats.dateTimeFormat
  implicit val notificationWrites: OFormat[Notification] = Json.format[Notification]
  implicit val notificationsFormat: OFormat[NotificationsForEori] = Json.format[NotificationsForEori]
}
