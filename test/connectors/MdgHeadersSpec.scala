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

package connectors

import services.DateTimeService
import utils.SpecBase

class MdgHeadersSpec extends SpecBase {

  "headers" should {
    "return the mandatory headers with Host when host header is provided" in {
      val hostHeader = "test_host"

      val mdgHeadersOb = new MdgHeaders(new DateTimeService())

      val outputHeaders: Seq[(String, String)] =
        mdgHeadersOb.headers("test_string", Some(hostHeader))

      outputHeaders.contains(("Host", hostHeader)) mustBe true
    }
  }
}
