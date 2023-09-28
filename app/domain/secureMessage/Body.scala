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

import com.google.common.base.Charsets
import com.google.common.io.BaseEncoding
import models.AccountType
import play.api.libs.json.{Json, OFormat}

case class Body(eori: String)

case class ExternalReference(id: String, source: String)

object ExternalReference {
  implicit val extRefFormat: OFormat[ExternalReference] = Json.format[ExternalReference]
}

case class Recipient(regime: String, taxIdentifier: TaxIdentifier, params: Params, email: String)

object Recipient {
  implicit val recipientFormat: OFormat[Recipient] = Json.format[Recipient]
}

case class TaxIdentifier(name: String, value: String)

object TaxIdentifier {
  implicit val taxFormat: OFormat[TaxIdentifier] = Json.format[TaxIdentifier]
}

case class Params(startMonth: String, startYear: String, endMonth: String, endYear: String, documentType: String)

object Params {
  implicit val paramFormat: OFormat[Params] = Json.format[Params]
}

case class Tags(notificationType: String)

object Tags {
  implicit val tagsFormat: OFormat[Tags] = Json.format[Tags]
}

case class Content(lang: String, subject: AccountType, body: String)

object Content {
  implicit val contentFormat: OFormat[Content] = Json.format[Content]
}

object SecureMessage {
  val body: String =
    s"Dear Apples & Pears Ltd\n\n" +
      s"The notification of adjustment statements you requested for March 2021 to May 2021 were not found.\n\n" +
      "There are 2 possible reasons for this:\n\n" +
      "Statements are only created for the periods in which you imported goods. " +
      "Check that you imported goods during the dates you requested.\n" +
      "Notification of adjustment statements for declarations made using " +
      "Customs Handling of Import and Export Freight (CHIEF) cannot be requested " +
      "using the Customs Declaration Service. (Insert guidance on how to get CHIEF NOA statements).\n" +
      "From the Customs Declaration Service"

  val encoded = BaseEncoding.base64().encode(body.getBytes(Charsets.UTF_8))
}

object SecureMessageResponse
