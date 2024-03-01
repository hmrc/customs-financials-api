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

package config

import config.MetaConfig.Platform
import utils.SpecBase

class MetaConfigSpec extends SpecBase {

  "Platform.MDTP" should {
    "return correct value for MDTP" in {
      Platform.MDTP mustBe "MDTP"
    }
  }

  "Platform.REGIME_CDS" should {
    "return correct value for MDTP" in {
      Platform.REGIME_CDS mustBe "CDS"
    }
  }

  "Platform.DIGITAL" should {
    "return correct value for MDTP" in {
      Platform.DIGITAL mustBe "Digital"
    }
  }
}
