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

package domain.secureMessage

import models.Params
import utils.{SpecBase, Utils}

class BodySpec extends SpecBase {

  "DateRange.apply" should {
    "create the DateRange object with correct contents for English" in new Setup {
      DateRange(params, Utils.englishLangKey).message mustBe "February 2021 to April 2021"
    }

    "create the DateRange object with correct contents for Welsh" in new Setup {
      DateRange(params, Utils.welshLangKey).message mustBe "February 2021 to April 2021"
    }
  }

  trait Setup {
    val params: Params = Params(periodStartMonth = "02",
      periodStartYear = "2021",
      periodEndMonth = "04",
      periodEndYear = "2021",
      accountType = "PostponedVATStatement",
      dan = "1234567")
  }
}
