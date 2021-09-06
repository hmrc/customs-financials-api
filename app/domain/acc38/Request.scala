/*
 * Copyright 2021 HM Revenue & Customs
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

package domain.acc38

import models.EORI
import play.api.libs.json.{Json, OFormat}

case class Request(getCorrespondenceAddressRequest: GetCorrespondenceAddressRequest)

object Request {
  implicit val format: OFormat[Request] = Json.format[Request]
}

case class GetCorrespondenceAddressRequest(requestCommon: RequestCommon,
                                           requestDetail: RequestDetail)

object GetCorrespondenceAddressRequest {
  implicit val format: OFormat[GetCorrespondenceAddressRequest] = Json.format[GetCorrespondenceAddressRequest]
}

case class RequestCommon(originatingSystem: String,
                         receiptDate: String,
                         acknowledgementReference: String)

object RequestCommon {
  implicit val format: OFormat[RequestCommon] = Json.format[RequestCommon]
}

case class RequestDetail(eori: EORI,
                         accountDetails: AccountDetails,
                         referenceDate: Option[String])

object RequestDetail {
  implicit val format: OFormat[RequestDetail] = Json.format[RequestDetail]
}
