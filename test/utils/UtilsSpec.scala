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

package utils

import models.responses.StatementSearchFailureNotificationErrorResponse
import utils.Utils._

import java.time.LocalDateTime

class UtilsSpec extends SpecBase {
  "emptyString" should {
    "return correct value" in {
      emptyString mustBe empty
    }

    "iso8601DateTimeRegEx" should {
      "return correct value of reg ex" in {
        iso8601DateTimeRegEx mustBe "[0-9]{4}-[0-9]{2}-[0-9]{2}T[0-9]{2}:[0-9]{2}:[0-9]{2}Z"
      }
    }

    "rfc7231DateTimePattern" should {
      "return correct value" in {
        rfc7231DateTimePattern mustBe "EEE, dd MMM yyyy HH:mm:ss 'GMT'"
      }
    }

    "dateTimeAsIso8601" should {
      "return correct ISO 8601 date time string for input date" in {
        val date = LocalDateTime.of(2023, 9, 11, 11, 10, 35)

        dateTimeAsIso8601(date) mustBe "2023-09-11T11:10:35Z"
      }
    }

    "isDateTimeStringInIso8601" should {
      "return true for the correct ISO DateTime string" in {
        isDateTimeStringInIso8601("2023-09-11T11:10:35Z") mustBe true
        isDateTimeStringInIso8601("2023-09-11T01:10:35Z") mustBe true
        isDateTimeStringInIso8601("2023-10-18T01:08:35Z") mustBe true
      }

      "return false for the incorrect ISO DateTime string" in {
        isDateTimeStringInIso8601("202-09-11T11:10:35Z") mustBe false
        isDateTimeStringInIso8601("2023-09-11T11:100:35Z") mustBe false
        isDateTimeStringInIso8601(emptyString) mustBe false
      }
    }

    "zeroPad" should {
      "return correct value" in {
        zeroPad(2) mustBe "02"
        zeroPad(5) mustBe "05"
        zeroPad(10) mustBe "10"
        zeroPad(9) mustBe "09"
      }
    }

    "currentDateTimeAsRFC7231" should {
      "return correct output datetime string " in {
        val localDateTime = LocalDateTime.of(2023,9,14,16,30,30)
        currentDateTimeAsRFC7231(localDateTime) mustBe "Thu, 14 Sep 2023 16:30:30 GMT"
      }
    }

    "writable" should {
      "return the correct result" in {
        val writableResult = Utils.writable(StatementSearchFailureNotificationErrorResponse.ssfnErrorResponseFormat)
        writableResult.contentType.value mustBe "application/json"
      }
    }

    "threeColons" should {
      "return correct value" in {
        threeColons mustBe ":::"
      }
    }
  }
}
