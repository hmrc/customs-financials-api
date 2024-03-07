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

package domain

import models.EORI
import play.api.libs.json._
import uk.gov.hmrc.mongo.play.json.formats.MongoJavatimeFormats

import java.time.format.DateTimeFormatter
import java.time.{Instant, LocalDateTime}

case class NotificationsForEori(eori: EORI,
                                notifications: Seq[Notification],
                                lastUpdated: Option[LocalDateTime])

object NotificationsForEori {

  private val formatterTest: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS")

  private val javaDateReads: Reads[LocalDateTime] = Reads[LocalDateTime](js =>
    js.validate[String].map[LocalDateTime](dtString => LocalDateTime.parse(dtString, formatterTest))
  )

  private val javaDateWrites: Writes[LocalDateTime] =
    (d: LocalDateTime) => JsString(d.format(DateTimeFormatter.ISO_DATE_TIME))

  implicit val lastUpdatedFormat: Format[Instant] = MongoJavatimeFormats.instantFormat
  implicit val notificationWrites: OFormat[Notification] = Json.format[Notification]
  implicit val dateTimeJF: Format[LocalDateTime] = Format(javaDateReads, javaDateWrites)
  implicit val notificationsFormat: OFormat[NotificationsForEori] = Json.format[NotificationsForEori]
}
