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
import play.api.libs.json.{JsSuccess, Json}
import utils.SpecBase
import utils.TestData._

import java.time.format.DateTimeFormatter
import java.time.{LocalDate, LocalDateTime}

class NotificationsForEoriSpec extends SpecBase {

  "Writes" should {

    "generate the correct output" in new Setup {
      Json.toJson(notificationsForEori) mustBe Json.parse(expectedJsValue)
    }
  }

  "Reads" should {

    "generate the correct output" in new Setup {

      import domain.NotificationsForEori.notificationsFormat

      Json.fromJson(Json.parse(expectedJsValue)) mustBe JsSuccess(notificationsForEori)
    }

    "return current LocalDateTime when  input string does not match the format" in new Setup {

      import domain.NotificationsForEori.notificationsFormat

      Json.fromJson(Json.parse(jsValueWithIncorrectDateTimeString)).isSuccess mustBe true
    }
  }

  trait Setup {
    val eori: EORI = EORI(EORI_VALUE)
    val localDate: LocalDate = LocalDate.of(YEAR_2023, MONTH_3, DAY_11)

    val lastUpdatedTime: LocalDateTime =
      LocalDateTime.parse("2023-03-11T10:05:30.352Z", DateTimeFormatter.ISO_DATE_TIME)

    val notification: Notification = Notification(eori = eori,
      fileRole = FILE_ROLE_C79_CERTIFICATE,
      fileName = TEST_FILE_NAME,
      fileSize = FILE_SIZE_1024L,
      created = Some(localDate),
      metadata = Map("Something" -> "Random"))

    val notificationsForEori: NotificationsForEori = NotificationsForEori(eori, Seq(notification), Some(lastUpdatedTime))

    val expectedJsValue: String =
      """{
        |"eori":"testEORI",
        |"notifications":[{"eori":"testEORI",
        |"fileRole":"C79Certificate",
        |"fileName":"test_file",
        |"fileSize":1024,
        |"created":"2023-03-11",
        |"metadata":{"Something":"Random"}}],"lastUpdated":1678529130352
        |}""".stripMargin

    val jsValueWithIncorrectDateTimeString: String =
      """{
        |"eori":"testEORI",
        |"notifications":[{"eori":"testEORI",
        |"fileRole":"C79Certificate",
        |"fileName":"test_file",
        |"fileSize":1024,
        |"created":"2023-03-11",
        |"metadata":{"Something":"Random"}}],"lastUpdated":"2023-03-11T10:05:30"
        |}""".stripMargin
  }
}
