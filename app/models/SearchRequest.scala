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

import play.api.libs.json.{Json, OFormat}
import utils.Utils.emptyString

case class SearchRequest(
  eoriNumber: String,
  statementRequestId: String,
  searchSuccessful: SearchResultStatus.SearchResultStatus,
  searchDateTime: String = emptyString,
  searchFailureReasonCode: String,
  failureRetryCount: Int
) {
  require(
    failureRetryCount >= 0 && failureRetryCount < 6,
    "invalid value for failureRetryCount, valid values are 0,1,2,3,4,5"
  )
}

object SearchRequest {
  implicit val searchRequestFormat: OFormat[SearchRequest] = Json.format[SearchRequest]
}
