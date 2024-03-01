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

import connectors.{DataStoreConnector, EmailThrottlerConnector}
import domain.{Notification, NotificationsForEori}
import models.EORI
import org.joda.time.DateTime
import org.mockito.{ArgumentCaptor, ArgumentMatchers}
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.api.{Application, inject}
import services.{DateTimeService, NotificationCache}
import uk.gov.hmrc.auth.core.{Enrolment, EnrolmentIdentifier, Enrolments}
import utils.SpecBase
import utils.TestData.{CSV_FILE_NAME, FILE_ROLE_C79_CERTIFICATE, FILE_SIZE_1000L}

import scala.concurrent.Future

class SDESNotificationsControllerSpec extends SpecBase {

  "getNotifications" should {

    "return no notifications from a mongo when there isn't any" in new Setup {
      when(mockNotificationCache.getNotifications(any)).thenReturn(Future.successful(None))
      when(mockAuthConnector.authorise[Enrolments](any, any)(any, any)).thenReturn(Future.successful(enrolments))
      when(mockDateTimeService.utcDateTime).thenReturn(DateTime.parse("2021-07-07T10:05:29.352Z"))

      running(app) {
        val result = route(app, getRequest).value
        status(result) mustBe 200
        contentAsJson(result).as[NotificationsForEori].notifications mustBe Nil
      }
    }

    "return seq of notifications from a mongo when there are some" in new Setup {
      val notification: NotificationsForEori = NotificationsForEori(eori, Seq(
        Notification(
          eori,
          FILE_ROLE_C79_CERTIFICATE,
          CSV_FILE_NAME,
          FILE_SIZE_1000L,
          None,
          Map(
            "periodStartYear" -> "2019",
            "periodStartMonth" -> "1",
            "fileType" -> "csv",
            "fileRole" -> "C79Certificate",
            "fileName" -> CSV_FILE_NAME,
            "downloadURL" -> "http://localhost/abc.csv",
            "fileSize" -> "1000")
        )
      ),
        Some(DateTime.parse("2021-07-07T10:05:29.352Z")))

      when(mockNotificationCache.getNotifications(ArgumentMatchers.eq(eori)))
        .thenReturn(Future.successful(Some(notification)))

      when(mockAuthConnector.authorise[Enrolments](any, any)(any, any)).thenReturn(Future.successful(enrolments))

      running(app) {
        val result = route(app, getRequest).value
        val expectedContent = Json.toJson(notification)
        status(result) mustBe OK
        contentAsJson(result) mustBe expectedContent
      }
    }

    "return an error when different EORINumber found in auth record" in new Setup {
      when(mockAuthConnector.authorise[Enrolments](any, any)(any, any))
        .thenReturn(Future.successful(enrolments))

      val otherEoriGetRequest: FakeRequest[AnyContentAsEmpty.type] =
        FakeRequest(GET, "/customs-financials-api/eori/123456788/notifications")

      running(app) {
        val result = route(app, otherEoriGetRequest).value

        status(result) mustBe FORBIDDEN
        contentAsString(result) mustBe "Enrolment Identifier EORINumber 123456789 not matched with 123456788"
      }
    }
  }

  "deleteNonRequestedNotifications" should {

    "delete notifications successfully" in new Setup {
      when(mockNotificationCache.removeByFileRole(any, any)).thenReturn(Future.successful(()))
      when(mockAuthConnector.authorise[Enrolments](any, any)(any, any)).thenReturn(Future.successful(enrolments))

      val req: FakeRequest[AnyContentAsEmpty.type] =
        FakeRequest(DELETE, s"/customs-financials-api/eori/${eori.value}/notifications/C79Certificate")

      running(app) {
        val result = route(app, req).value
        status(result) mustBe OK
      }
    }

    "return an error when different EORINumber found in auth record" in new Setup {
      when(mockAuthConnector.authorise[Enrolments](any, any)(any, any))
        .thenReturn(Future.successful(enrolments))

      val req: FakeRequest[AnyContentAsEmpty.type] =
        FakeRequest(DELETE, s"/customs-financials-api/eori/differentEORI/notifications/C79Certificate")

      running(app) {
        val result = route(app, req).value

        status(result) mustBe FORBIDDEN
        contentAsString(result) mustBe s"Enrolment Identifier EORINumber ${eori.value} not matched with differentEORI"
      }
    }
  }

  "deleteRequestedNotifications" should {

    "delete notifications successfully" in new Setup {
      when(mockNotificationCache.removeRequestedByFileRole(any, any)).thenReturn(Future.successful(()))
      when(mockAuthConnector.authorise[Enrolments](any, any)(any, any)).thenReturn(Future.successful(enrolments))

      val req: FakeRequest[AnyContentAsEmpty.type] =
        FakeRequest(DELETE, s"/customs-financials-api/eori/${eori.value}/requested-notifications/C79Certificate")

      running(app) {
        val result = route(app, req).value
        status(result) mustBe OK
      }
    }

    "return an error when different EORINumber found in auth record" in new Setup {
      when(mockAuthConnector.authorise[Enrolments](any, any)(any, any))
        .thenReturn(Future.successful(enrolments))

      val req: FakeRequest[AnyContentAsEmpty.type] =
        FakeRequest(DELETE, s"/customs-financials-api/eori/differentEORI/requested-notifications/C79Certificate")

      running(app) {
        val result = route(app, req).value
        status(result) mustBe FORBIDDEN
        contentAsString(result) mustBe s"Enrolment Identifier EORINumber ${eori.value} not matched with differentEORI"
      }
    }
  }

  trait Setup {
    val eori: EORI = EORI("123456789")
    val enrolments: Enrolments =
      Enrolments(Set(Enrolment("HMRC-CUS-ORG", Seq(EnrolmentIdentifier("EORINumber", eori.value)), "activated")))

    val mockNotificationCache: NotificationCache = mock[NotificationCache]
    val mockEmailThrottler: EmailThrottlerConnector = mock[EmailThrottlerConnector]
    val mockAuthConnector: CustomAuthConnector = mock[CustomAuthConnector]
    val mockDataStore: DataStoreConnector = mock[DataStoreConnector]
    val mockDateTimeService: DateTimeService = mock[DateTimeService]

    val app: Application = GuiceApplicationBuilder().overrides(
      inject.bind[CustomAuthConnector].toInstance(mockAuthConnector),
      inject.bind[NotificationCache].toInstance(mockNotificationCache),
      inject.bind[EmailThrottlerConnector].toInstance(mockEmailThrottler),
      inject.bind[DataStoreConnector].toInstance(mockDataStore),
      inject.bind[DateTimeService].toInstance(mockDateTimeService)
    ).configure(
      "microservice.metrics.enabled" -> false,
      "metrics.enabled" -> false,
      "auditing.enabled" -> false
    ).build()

    val notificationForEoriCaptor: ArgumentCaptor[NotificationsForEori] =
      ArgumentCaptor.forClass(classOf[NotificationsForEori])

    val getRequest: FakeRequest[AnyContentAsEmpty.type] =
      FakeRequest(GET, "/customs-financials-api/eori/123456789/notifications")
  }
}
