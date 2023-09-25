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

import play.api.libs.json.Json
import java.time

case class Body(eori: String)

case class ExternalReference(id: String, source: String)
case class Recipient(regime: String, taxIdentifier: TaxIdentifier, name: Name, email: String)

case class TaxIdentifier(name: String, value: String)
case class Name(line1: String, line2: String2, line3: String)

case class Tags(notificationType: String)
case class AlertsDetails(data: Data)
case class Data(key1: String, key2: String)

case class Details(formId: Stirng, issueDate: String, batchId: String,
  sourceData: String, properties: List[Property])

case class Property(name: String, value: String)

case class Content(metaData: MetaData, messageType: String,
  validForm: String, alertQue: String)
case class MetaData(lang: String, subject: String, body: String)

object SecureMessage {
  val contentType = "application/json"
}

object SecureMessageResponse