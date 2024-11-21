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

import models.{EORI, FileRole}
import play.api.libs.json.{JsValue, Json}
import utils.SpecBase
import utils.TestData.*

class NotificationSpec extends SpecBase {

  import SDESInputFormats.*

  val sdesMessage: JsValue = Json.parse(
    """
      |[
      |    {
      |       "eori":"testEORI",
      |       "fileName": "vat-2018-05.pdf",
      |       "fileSize": 75251,
      |       "metadata": [
      |            {"metadata": "PeriodStartYear", "value": "2018"},
      |            {"metadata": "PeriodStartMonth", "value": "5"},
      |            {"metadata": "FileType", "value": "PDF"},
      |            {"metadata": "FileRole", "value": "C79Certificate"}
      |        ]
      |    },
      |    {
      |        "eori":"someEORI",
      |        "fileName": "statement-2018-09-19.pdf",
      |        "fileSize": 2417804,
      |        "metadata": [
      |            {"metadata": "FileRole", "value": "SecurityStatement"},
      |            {"metadata": "FileType", "value": "PDF"},
      |            {"metadata": "issueDate", "value": "19/09/2018"},
      |            {"metadata": "PeriodEndDay", "value": "19"},
      |            {"metadata": "PeriodEndMonth", "value": "9"},
      |            {"metadata": "PeriodEndYear", "value": "2018"},
      |            {"metadata": "PeriodStartDay", "value": "13"},
      |            {"metadata": "PeriodStartMonth", "value": "9"},
      |            {"metadata": "PeriodStartYear", "value": "2018"}
      |        ]
      |    }
      |]
                      """.stripMargin)

  "NotificationReads" should {

    "parse message correctly" in {
      val parsedNotifications = Json.fromJson[Seq[Notification]](sdesMessage)

      val expectedNotifications = List(
        Notification(
          EORI(EORI_VALUE),
          FILE_ROLE_C79_CERTIFICATE,
          "vat-2018-05.pdf",
          FILE_SIZE_75251L,
          Some(CURRENT_LOCAL_DATE),
          Map("PeriodStartYear" -> "2018", "PeriodStartMonth" -> "5", "FileType" -> "PDF")),
        Notification(
          EORI(EORI_VALUE_1),
          FILE_ROLE_SECURITY_STATEMENT,
          "statement-2018-09-19.pdf",
          FILE_SIZE_2417804L,
          Some(CURRENT_LOCAL_DATE),
          Map("PeriodStartYear" -> "2018",
            "PeriodStartDay" -> "13",
            "PeriodEndDay" -> "19",
            "FileType" -> "PDF",
            "PeriodEndYear" -> "2018",
            "issueDate" -> "19/09/2018",
            "PeriodStartMonth" -> "9",
            "PeriodEndMonth" -> "9"))
      )

      parsedNotifications.get mustBe expectedNotifications
    }

    "extract the EORI from a compound EORI-DAN style identifier" in {
      val sdesDutyDefermentMessage = Json.parse(
        """
          |[
          |    {
          |       "eori":"testEORI-12345",
          |       "fileName": "filename",
          |       "fileSize": 75251,
          |       "metadata": [
          |            {"metadata": "FileRole", "value": "DutyDeferment"}
          |        ]
          |    }
          |]
            """.stripMargin)
      val parsedNotifications = Json.fromJson[Seq[Notification]](sdesDutyDefermentMessage)

      parsedNotifications.get mustBe List(
        Notification(EORI(EORI_VALUE),
          FileRole("DutyDeferment"),
          "filename",
          FILE_SIZE_75251L,
          Some(CURRENT_LOCAL_DATE),
          Map.empty))
    }
  }

  "toString" should {

    "return the correct value" in {

      val fileRole = FileRole("DutyDeferment")
      val fileName = "filename"
      val key = "DAN"
      val key1 = "test_key"
      val keyValue = "test_value"
      val metaData = Map(key -> keyValue)
      val metaData1 = Map(key1 -> keyValue)
      val expectedMetaData = Map(key -> "xxxxxx")

      Notification(
        EORI(EORI_VALUE),
        fileRole,
        fileName,
        FILE_SIZE_75251L,
        Some(CURRENT_LOCAL_DATE),
        metaData).toString mustBe
        s"Notification(xxxxxxxx, $fileRole, $fileName, $FILE_SIZE_75251L, ${Some(CURRENT_LOCAL_DATE)}, $expectedMetaData"

      Notification(
        EORI(EORI_VALUE),
        fileRole,
        fileName,
        FILE_SIZE_75251L,
        Some(CURRENT_LOCAL_DATE),
        metaData1).toString mustBe
        s"Notification(xxxxxxxx, $fileRole, $fileName, $FILE_SIZE_75251L, ${Some(CURRENT_LOCAL_DATE)}, $metaData1"
    }
  }
}
