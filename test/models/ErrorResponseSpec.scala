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

package models

import play.api.libs.json.{JsSuccess, Json}
import utils.SpecBase

class ErrorResponseSpec extends SpecBase {

  "ExceededThresholdErrorException" should {

    "read the correct value" in {
      import models.responses.GuaranteeTransactionsResponse.thresholdErrorFormat

      Json.fromJson(Json.toJson(ExceededThresholdErrorException)) mustBe JsSuccess(ExceededThresholdErrorException)
    }

    "writes the correct value" in {
      import models.responses.GuaranteeTransactionsResponse.thresholdErrorFormat

      Json.toJson(ExceededThresholdErrorException) mustBe Json.obj()
    }
  }

  "NoAssociatedDataException" should {

    "read the correct value" in {
      import models.responses.GuaranteeTransactionsResponse.noAssociatedDataFormat

      Json.fromJson(Json.toJson(NoAssociatedDataException)) mustBe JsSuccess(NoAssociatedDataException)
    }

    "writes the correct value" in {
      import models.responses.GuaranteeTransactionsResponse.noAssociatedDataFormat

      Json.toJson(NoAssociatedDataException) mustBe Json.obj()
    }
  }
}
