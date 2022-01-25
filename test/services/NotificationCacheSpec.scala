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

package services

import config.AppConfig
import domain.{Notification, NotificationsForEori}
import models.{EORI, FileRole}
import org.joda.time.{DateTime, DateTimeZone}
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.Helpers.running
import play.api.{Application, inject}
import utils.SpecBase

import java.time.LocalDate
import scala.concurrent.ExecutionContext.Implicits.global

class NotificationCacheSpec extends SpecBase {

  "NotificationCache" should {
    "put and get notifications from mongo db" in new Setup {
      running(app) {
        val result = for {
          _ <- cache.putNotifications(NotificationsForEori(Eori1, notifications1, lastUpdated))
          _ <- cache.putNotifications(NotificationsForEori(Eori1, notifications2, None))
          _ <- cache.putNotifications(NotificationsForEori(Eori1, notifications3, None))
          _ <- cache.putNotifications(NotificationsForEori(Eori1, notifications4, lastUpdated))
          notifications <- cache.getNotifications(Eori1)
        } yield notifications

        val expectedNotifications = List(securityStmtNotification1, securityStmtNotification2, c79CertNotification1, c79CertNotification2,
          c79CertNotification3, c79CertNotification4, securityStmtNotification3, securityStmtNotification4,
          pvatStmtNotification1, pvatStmtNotification2, pvatStmtNotification3, pvatStmtNotification4)

        await(result) mustBe Some(NotificationsForEori(Eori1, expectedNotifications, lastUpdated))
      }
    }

    "get statement request notifications which are not expired from mongo db" in new Setup {
      running(app) {
        val result = for {
          _ <- cache.putNotifications(NotificationsForEori(Eori1, notifications1, lastUpdated))
          _ <- cache.putNotifications(NotificationsForEori(Eori1, notifications2, None))
          _ <- cache.putNotifications(NotificationsForEori(Eori1, notifications3, None))
          _ <- cache.putNotifications(NotificationsForEori(Eori1, notifications4, lastUpdated))
          _ <- cache.putNotifications(NotificationsForEori(Eori1, notifications5, lastUpdated))
          notifications <- cache.getNotifications(Eori1)
        } yield notifications

        val expectedNotifications = List(securityStmtNotification1, securityStmtNotification2, c79CertNotification1, c79CertNotification2,
          c79CertNotification3, c79CertNotification4, securityStmtNotification3, securityStmtNotification4,
          pvatStmtNotification1, pvatStmtNotification2, pvatStmtNotification3, pvatStmtNotification4, c79StatementRequest2)

        await(result) mustBe Some(NotificationsForEori(Eori1, expectedNotifications, lastUpdated))
      }
    }

    "delete non-requested C79Certificate by fileRole" in new Setup {
      val requestedStatement: Notification =
        Notification(
          Eori1,
          FileRole("C79Certificate"),
          "abc.csv",
          1000,
          Some(LocalDate.now),
          Map("periodStartYear" -> "2019", "periodStartMonth" -> "4", "fileType" -> "csv", "statementRequestID" -> "12345678")
        )

      val nonRequestedStatement: Notification =
        Notification(
          Eori1,
          FileRole("C79Certificate"),
          "abc.csv",
          1000,
          Some(LocalDate.now),
          Map("periodStartYear" -> "2019", "periodStartMonth" -> "4", "fileType" -> "csv")
        )

      running(app) {
        val result = await(for {
          _ <- cache.putNotifications(NotificationsForEori(Eori1, Seq(requestedStatement, nonRequestedStatement), lastUpdated))
          _ <- cache.removeByFileRole(Eori1, FileRole("C79Certificate"))
          notifications <- cache.getNotifications(Eori1)
        } yield notifications)

        result mustBe Some(NotificationsForEori(Eori1, Seq(requestedStatement), lastUpdated))
      }
    }

    "delete requested C79Certificate by fileRole" in new Setup {
      val requestedStatement1: Notification =
        Notification(
          Eori1,
          FileRole("C79Certificate"),
          "abc23.csv",
          1000,
          Some(LocalDate.now),
          Map("periodStartYear" -> "2018", "periodStartMonth" -> "4", "fileType" -> "csv", "statementRequestID" -> "12345678")
        )

      val requestedStatement2: Notification =
        Notification(
          Eori1,
          FileRole("C79Certificate"),
          "abc.csv",
          1000,
          Some(LocalDate.now),
          Map("periodStartYear" -> "2019", "periodStartMonth" -> "4", "fileType" -> "csv", "statementRequestID" -> "12345678")
        )

      val nonRequestedStatement: Notification =
        Notification(
          Eori1,
          FileRole("C79Certificate"),
          "abc.csv",
          1000,
          Some(LocalDate.now),
          Map("periodStartYear" -> "2019", "periodStartMonth" -> "4", "fileType" -> "csv")
        )

      running(app) {
        val result = await(for {
          _ <- cache.putNotifications(NotificationsForEori(Eori1, Seq(requestedStatement1, requestedStatement2, nonRequestedStatement), lastUpdated))
          _ <- cache.removeRequestedByFileRole(Eori1, FileRole("C79Certificate"))
          notifications <- cache.getNotifications(Eori1)
        } yield notifications)

        result mustBe Some(NotificationsForEori(Eori1, Seq(nonRequestedStatement), lastUpdated))
      }
    }

  }

  trait Setup {

    val Eori1: EORI = EORI("testEORI")

    val securityStmtNotification1: Notification = Notification(Eori1, FileRole("SecurityStatement"), "abc.csv", 1000, None, Map("periodStartYear" -> "2019", "periodStartMonth" -> "1", "fileType" -> "csv"))
    val securityStmtNotification2: Notification = Notification(Eori1, FileRole("SecurityStatement"), "abc.csv", 1000, Some(LocalDate.now), Map("periodStartYear" -> "2019", "periodStartMonth" -> "2", "fileType" -> "csv"))
    val securityStmtNotification3: Notification = Notification(Eori1, FileRole("SecurityStatement"), "abc.csv", 1000, None, Map("periodStartYear" -> "2019", "periodStartMonth" -> "3", "fileType" -> "csv"))
    val securityStmtNotification4: Notification = Notification(Eori1, FileRole("SecurityStatement"), "abc.csv", 1000, Some(LocalDate.now), Map("periodStartYear" -> "2019", "periodStartMonth" -> "4", "fileType" -> "csv"))

    val c79CertNotification1: Notification = Notification(Eori1,FileRole("C79Certificate"), "abc.csv", 1000, Some(LocalDate.now), Map("periodStartYear" -> "2019", "periodStartMonth" -> "1", "fileType" -> "csv"))
    val c79CertNotification2: Notification = Notification(Eori1,FileRole("C79Certificate"), "abc.csv", 1000, None, Map("periodStartYear" -> "2019", "periodStartMonth" -> "2", "fileType" -> "csv"))
    val c79CertNotification3: Notification = Notification(Eori1,FileRole("C79Certificate"), "abc.csv", 1000, Some(LocalDate.now), Map("periodStartYear" -> "2019", "periodStartMonth" -> "3", "fileType" -> "csv"))
    val c79CertNotification4: Notification = Notification(Eori1,FileRole("C79Certificate"), "abc.csv", 1000, None, Map("periodStartYear" -> "2019", "periodStartMonth" -> "4", "fileType" -> "csv"))

    val pvatStmtNotification1: Notification = Notification(Eori1,FileRole("PostponedVat"), "abc.csv", 1000, None, Map("periodStartYear" -> "2019", "periodStartMonth" -> "1", "fileType" -> "csv"))
    val pvatStmtNotification2: Notification = Notification(Eori1,FileRole("PostponedVat"), "abc.csv", 1000, None, Map("periodStartYear" -> "2019", "periodStartMonth" -> "2", "fileType" -> "csv"))
    val pvatStmtNotification3: Notification = Notification(Eori1,FileRole("PostponedVat"), "abc.csv", 1000, None, Map("periodStartYear" -> "2019", "periodStartMonth" -> "3", "fileType" -> "csv"))
    val pvatStmtNotification4: Notification = Notification(Eori1,FileRole("PostponedVat"), "abc.csv", 1000, Some(LocalDate.now), Map("periodStartYear" -> "2019", "periodStartMonth" -> "4", "fileType" -> "csv"))

    val c79StatementRequest1: Notification = Notification(Eori1,FileRole("C79Certificate"), "abc.csv", 1000, Some(LocalDate.now.minusDays(2)), Map("periodStartYear" -> "2019", "periodStartMonth" -> "4", "fileType" -> "csv", "statementRequestID" -> "1234567", "RETENTION_DAYS" -> "1"))
    val c79StatementRequest2: Notification = Notification(Eori1,FileRole("C79Certificate"), "abc.csv", 1000, Some(LocalDate.now), Map("periodStartYear" -> "2019", "periodStartMonth" -> "4", "fileType" -> "csv", "statementRequestID" -> "12345678", "RETENTION_DAYS" -> "10"))

    val notifications1: Seq[Notification] = Seq(securityStmtNotification1, securityStmtNotification2, c79CertNotification1, c79CertNotification2)
    val notifications2: Seq[Notification] = Seq(securityStmtNotification1, securityStmtNotification2, c79CertNotification3, c79CertNotification4)
    val notifications3: Seq[Notification] = Seq(securityStmtNotification3, securityStmtNotification4, c79CertNotification1, c79CertNotification2)
    val notifications4: Seq[Notification] = Seq(pvatStmtNotification1, pvatStmtNotification2, pvatStmtNotification3, pvatStmtNotification4)
    val notifications5: Seq[Notification] = Seq(c79StatementRequest1, c79StatementRequest2)

    val requestedAndNonRequestedNotifications = Seq(c79CertNotification1, c79StatementRequest1)

    val lastUpdated: Option[DateTime] = Some(DateTime.now(DateTimeZone.UTC))
    val mockAppConfig: AppConfig = mock[AppConfig]
    when(mockAppConfig.dbTimeToLiveInSeconds).thenReturn(10)
    when(mockAppConfig.notificationCacheCollectionName).thenReturn("notificationStore-test")
    when(mockAppConfig.authUrl).thenReturn("/")


    val app: Application = GuiceApplicationBuilder().overrides(
      inject.bind[AppConfig].toInstance(mockAppConfig)
    ).configure(
      "microservice.metrics.enabled" -> false,
      "metrics.enabled" -> false,
      "auditing.enabled" -> false
    ).build()

    val cache: DefaultNotificationCache = app.injector.instanceOf[DefaultNotificationCache]
    await(cache.collection.drop().toFuture())

  }
}
