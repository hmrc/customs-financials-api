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

    "encodeToUTF8Charsets" should {
      "Return encoded String" in {
        val result = encodeToUTF8Charsets("abc123")
        result mustBe "YWJjMTIz"

      }
    }

    "threeColons" should {
      "return correct value" in {
        threeColons mustBe ":::"
      }
    }

    "englishLangKey" should {
      "return correct value" in {
        englishLangKey mustBe "en"
      }
    }

    "welshLangKey" should {
      "return correct value" in {
        welshLangKey mustBe "cy"
      }
    }

    "convertMonthValueToFullMonthName" should {
      "return correct output for English" in {
        convertMonthValueToFullMonthName("01") mustBe "January"
        convertMonthValueToFullMonthName("02") mustBe "February"
        convertMonthValueToFullMonthName("03") mustBe "March"
        convertMonthValueToFullMonthName("04") mustBe "April"
        convertMonthValueToFullMonthName("05") mustBe "May"
        convertMonthValueToFullMonthName("06") mustBe "June"
        convertMonthValueToFullMonthName("07") mustBe "July"
        convertMonthValueToFullMonthName("08") mustBe "August"
        convertMonthValueToFullMonthName("09") mustBe "September"
        convertMonthValueToFullMonthName("10") mustBe "October"
        convertMonthValueToFullMonthName("11") mustBe "November"
        convertMonthValueToFullMonthName("12") mustBe "December"
      }

      "return correct output for Welsh" in {
        convertMonthValueToFullMonthName("01", welshLangKey) mustBe "Ionawr"
        convertMonthValueToFullMonthName("02", welshLangKey) mustBe "Chwefror"
        convertMonthValueToFullMonthName("03", welshLangKey) mustBe "Mawrth"
        convertMonthValueToFullMonthName("04", welshLangKey) mustBe "Ebrill"
        convertMonthValueToFullMonthName("05", welshLangKey) mustBe "Mai"
        convertMonthValueToFullMonthName("06", welshLangKey) mustBe "Mehefin"
        convertMonthValueToFullMonthName("07", welshLangKey) mustBe "Gorffennaf"
        convertMonthValueToFullMonthName("08", welshLangKey) mustBe "Awst"
        convertMonthValueToFullMonthName("09", welshLangKey) mustBe "Medi"
        convertMonthValueToFullMonthName("10", welshLangKey) mustBe "Hydref"
        convertMonthValueToFullMonthName("11", welshLangKey) mustBe "Tachwedd"
        convertMonthValueToFullMonthName("12", welshLangKey) mustBe "Rhagfyr"
      }

      "return return no value for invalid input" in {
        convertMonthValueToFullMonthName("13") mustBe emptyString
        convertMonthValueToFullMonthName("00") mustBe emptyString
      }
    }

    "monthValueToNameMap" should {
      "create the correct map for English" in {
        monthValueToNameMap(englishLangKey) mustBe Map(
          "01" -> "January",
          "02" -> "February",
          "03" -> "March",
          "04" -> "April",
          "05" -> "May",
          "06" -> "June",
          "07" -> "July",
          "08" -> "August",
          "09" -> "September",
          "10" -> "October",
          "11" -> "November",
          "12" -> "December")
      }

      "create the correct map for Welsh" in {
        monthValueToNameMap(welshLangKey) mustBe Map(
          "01" -> "Ionawr",
          "02" -> "Chwefror",
          "03" -> "Mawrth",
          "04" -> "Ebrill",
          "05" -> "Mai",
          "06" -> "Mehefin",
          "07" -> "Gorffennaf",
          "08" -> "Awst",
          "09" -> "Medi",
          "10" -> "Hydref",
          "11" -> "Tachwedd",
          "12" -> "Rhagfyr")
      }

      "singleSpace" should {
        "return correct value" in {
          singleSpace mustBe " "
        }
      }
    }

    "createHyperLink" should {
      "create the hyperLink with correct link, text and default style class" in {

        createHyperLink("test_text", "test_link@test.com") mustBe
          "<a class=\"govuk-link\" href=\"test_link@test.com\">test_text</a>"
      }

      "create the hyperLink with correct link, text and given style class" in {
        createHyperLink("test_text", "test_link@test.com", "test_class") mustBe
          "<a class=\"test_class\" href=\"test_link@test.com\">test_text</a>"
      }
    }
  }
}
