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

import models.AccountType
import play.api.libs.json.{JsSuccess, Json}
import utils.SpecBase

class ResponseSpec extends SpecBase {

  "ExternalReference" should {
    "generate correct output using the Reads" in new Setup {

      import domain.secureMessage.ExternalReference.extRefFormat

      Json.fromJson(Json.parse(externalRefJsValue)) mustBe JsSuccess(externalRefObject)
    }

    "generate correct output using the Writes" in new Setup {
      Json.toJson(externalRefObject) mustBe Json.parse(externalRefJsValue)
    }
  }

  "Recipient" should {
    "generate correct output using the Reads" in new Setup {

      import domain.secureMessage.Recipient.recipientFormat

      Json.fromJson(Json.parse(recipientJsValue)) mustBe JsSuccess(recipientObject)

    }

    "generate correct output using the Writes" in new Setup {
      Json.toJson(recipientObject) mustBe Json.parse(recipientJsValue)
    }
  }

  "TaxIdentifier" should {
    "generate correct output using the Reads" in new Setup {

      import domain.secureMessage.TaxIdentifier.taxFormat

      Json.fromJson(Json.parse(taxIdentifierJsValue)) mustBe JsSuccess(taxIdentifierObject)
    }

    "generate correct output using the Writes" in new Setup {
      Json.toJson(taxIdentifierObject) mustBe Json.parse(taxIdentifierJsValue)
    }
  }

  "Params" should {
    "generate correct output using the Reads" in new Setup {

      import domain.secureMessage.Params.paramsFormat

      Json.fromJson(Json.parse(paramsJsValue)) mustBe JsSuccess(paramsObject)
    }

    "generate correct output using the Writes" in new Setup {
      Json.toJson(paramsObject) mustBe Json.parse(paramsJsValue)
    }
  }

  "Tags" should {
    "generate correct output using the Reads" in new Setup {

      import domain.secureMessage.Tags.tagsFormat

      Json.fromJson(Json.parse(tagsJsValue)) mustBe JsSuccess(tagsObject)
    }

    "generate correct output using the Writes" in new Setup {
      Json.toJson(tagsObject) mustBe Json.parse(tagsJsValue)
    }
  }

  "Content" should {
    "generate correct output using the Reads" in new Setup {
      import domain.secureMessage.Content.contentFormat
      Json.fromJson(Json.parse(contentJsValue)) mustBe JsSuccess(contentObject)
    }

    "generate correct output using the Writes" in new Setup {
      Json.toJson(contentObject) mustBe Json.parse(contentJsValue)
    }
  }

  trait Setup {
    val externalRefJsValue: String = """{"id": "abcd12345","source": "mdtp"}""".stripMargin
    val externalRefObject: ExternalReference = ExternalReference(id = "abcd12345", source = "mdtp")

    val taxIdentifierJsValue: String = """{"name": "name","value": "value"}""".stripMargin
    val taxIdentifierObject: TaxIdentifier = TaxIdentifier(name = "name", value = "value")

    val paramsJsValue: String =
      """{"startMonth": "01", "startYear": "2022",
        |"endMonth": "01", "endYear": "2023",
        |"documentType": "CDS"}""".stripMargin
    val paramsObject: Params = Params(startMonth = "01", startYear = "2022",
      endMonth = "01", endYear = "2023", documentType = "CDS")

    val recipientJsValue: String =
      """{"regime": "regime", "taxIdentifier":{"name": "name","value": "value"},
        | "params": {"startMonth": "01", "startYear": "2022",
        |"endMonth": "01", "endYear": "2023",
        |"documentType": "CDS"}, "email": "email"}""".stripMargin
    val recipientObject: Recipient = Recipient(regime = "regime",
      taxIdentifier = taxIdentifierObject, params = paramsObject, email = "email")

    val tagsJsValue: String = """{"notificationType": "cds fin"}""".stripMargin
    val tagsObject: Tags = Tags(notificationType = "cds fin")

    val contentJsValue: String = """{"lang":"en", "subject":"AccountType", "body": "body"}""".stripMargin
    val contentObject: Content = Content(lang = "en", subject = AccountType("AccountType"), body = "body")
  }
}
