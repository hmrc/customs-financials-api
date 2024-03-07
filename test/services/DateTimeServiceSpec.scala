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

import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper
import utils.SpecBase

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class DateTimeServiceSpec extends SpecBase {

  val dateService = new DateTimeService

  "now" must {
    "return correct return type" in {
      dateService.now().isInstanceOf[LocalDateTime] mustBe true
      Option(dateService.now()) must not be empty
    }
  }

  "timeStamp" must {
    "return correct return type" in {
      dateService.timeStamp().isInstanceOf[Long] mustBe true
      dateService.timeStamp() should be > 0L
    }
  }

  "currentDateTimeAsIso8601" must {
    "return the output in correct format" in {
      val expectedFormat: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'")

      val result = LocalDateTime.parse(dateService.currentDateTimeAsIso8601, expectedFormat)

      Option(result) must not be empty
    }
  }

  "utcDateTime" must {
    "return the correct date time" in {
      dateService.utcDateTime.isInstanceOf[LocalDateTime] mustBe true
      Option(dateService.utcDateTime) must not be empty
    }
  }
}
