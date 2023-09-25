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

package domain.SecureMessage

import play.api.libs.json.{Json, OFormat}
import domain.SecureMessage

case class RequestCommon(
  externalRef: SecureMessage.ExternalReference,
  recipient: SecureMessage.Recipient,
  params: SecureMessage.Params,
  email: String,
  tags: SecureMessage.Tags,
  content: List[SecureMessage.Content],
  messageType: String,
  validForm: String,
  alertQueue: String
)

object RequestCommon {
  implicit val format: OFormat[RequestCommon] = Json.format[RequestCommon]
  implicit val Refformat: OFormat[SecureMessage.ExternalReference] = Json.format[SecureMessage.ExternalReference]
  implicit val Taxformat: OFormat[SecureMessage.TaxIdentifier] = Json.format[SecureMessage.TaxIdentifier]
  implicit val Recipientformat: OFormat[SecureMessage.Recipient] = Json.format[SecureMessage.Recipient]
  implicit val Paramformat: OFormat[SecureMessage.Params] = Json.format[SecureMessage.Params]
  implicit val Tagformat: OFormat[SecureMessage.Tags] = Json.format[SecureMessage.Tags]
  implicit val Contentformat: OFormat[SecureMessage.Content] = Json.format[SecureMessage.Content]
}