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

package services

import com.mongodb.client.model.Indexes.ascending
import config.AppConfig
import domain.NotificationsForEori
import models.{EORI, FileRole}
import org.mongodb.scala.bson.conversions.Bson
import org.mongodb.scala.model.Filters._
import org.mongodb.scala.model._
import play.api.libs.json.Json
import uk.gov.hmrc.mongo.MongoComponent
import uk.gov.hmrc.mongo.play.json.{Codecs, PlayMongoRepository}
import java.time.LocalDate
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class DefaultNotificationCache @Inject()(
  mongoComponent: MongoComponent,
  appConfig: AppConfig
)(implicit executionContext: ExecutionContext)
  extends PlayMongoRepository[NotificationsForEori](
    collectionName = appConfig.notificationCacheCollectionName,
    mongoComponent = mongoComponent,
    domainFormat = NotificationsForEori.notificationsFormat,
    indexes = Seq(
      IndexModel(
        ascending("eori"),
        IndexOptions().name("eoriIndex")
          .unique(true)
          .sparse(true)
      ),
      IndexModel(
        ascending("lastUpdated"),
        IndexOptions().name("lastUpdatedIndex")
          .expireAfter(appConfig.dbTimeToLiveInSeconds, TimeUnit.SECONDS)
      ))
  ) with NotificationCache {

  override def getNotifications(eori: EORI): Future[Option[NotificationsForEori]] = {
    for {
      _ <- removeExpiredNotifications(eori)
      result <- collection.find(equal("eori", eori.value)).toSingle().toFutureOption()
    } yield result
  }

  override def removeByFileRole(eori: EORI, fileRole: FileRole): Future[Unit] = {
    val query = Filters.and(
      Filters.equal("fileRole", Codecs.toBson(Json.toJson(fileRole))),
      Filters.exists("metadata.statementRequestID", exists = false)
    )

    collection.updateOne(
      equal("eori", eori.value),
      Updates.pull("notifications", query))
      .toFuture()
      .map(_ => ())
  }

  override def removeEori(eori: EORI): Future[Unit] = {
    collection
      .deleteOne(equal("eori", eori.value))
      .toFuture()
      .map(_ => ())
  }

  override def removeRequestedByFileRole(eori: EORI, fileRole: FileRole): Future[Unit] = {
    val query = Filters.and(
      Filters.equal("fileRole", Codecs.toBson(Json.toJson(fileRole))),
      Filters.exists("metadata.statementRequestID", exists = true)
    )

    collection.updateOne(
      equal("eori", eori.value),
      Updates.pull("notifications", query))
      .toFuture()
      .map(_ => ())
  }

  override def removeByMetaData(eori: EORI, statementRequestID: Map[String, String]): Future[Unit] = {
    collection.updateOne(
      equal("eori", eori.value),
      Updates.pull("notifications", Codecs.toBson(Json.obj("metadata" -> statementRequestID))))
      .toFuture()
      .map(_ => ())
  }

  override def putNotifications(notificationsForEori: NotificationsForEori): Future[Unit] = {
    import NotificationsForEori._

    val lastUpdated: Bson = Updates.set("lastUpdated", Codecs.toBson(notificationsForEori.lastUpdated))
    val records = notificationsForEori.notifications.map(Codecs.toBson(_))
    val test = Updates.addEachToSet("notifications", records: _*)
    collection.updateOne(
      equal("eori", notificationsForEori.eori.value),
      Updates.combine(
        test,
        lastUpdated
      ),
      UpdateOptions().upsert(true)
    ).toFuture().map(_ => ())
  }

  private def removeExpiredNotifications(eori: EORI): Future[Seq[Unit]] = {
    val findResult = collection.find(equal("eori", eori.value)).toFuture().map(_.flatMap(_.notifications))

    findResult.flatMap { notifications =>
      Future.sequence(notifications.map { notification => {
        val retentionDays: Option[String] = notification.metadata.get("RETENTION_DAYS")
        val statementRequestIDExists: Boolean = notification.metadata.contains("statementRequestID")
        val created: Option[LocalDate] = notification.created
        (retentionDays, statementRequestIDExists, created) match {
          case (Some(days), true, Some(date)) if date.plusDays(days.toInt).isBefore(LocalDate.now) =>
            removeByMetaData(notification.eori, notification.metadata)
          case _ => Future.successful(())
        }
      }
      })
    }
  }
}

trait NotificationCache {
  def getNotifications(eori: EORI): Future[Option[NotificationsForEori]]

  def putNotifications(notificationsForEori: NotificationsForEori): Future[Unit]

  def removeByFileRole(eori: EORI, fileRole: FileRole): Future[Unit]

  def removeEori(eori: EORI): Future[Unit]

  def removeRequestedByFileRole(eori: EORI, fileRole: FileRole): Future[Unit]

  def removeByMetaData(eori: EORI, statementRequestID: Map[String, String]): Future[Unit]
}