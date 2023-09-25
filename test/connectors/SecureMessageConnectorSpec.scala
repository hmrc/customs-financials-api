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

import java.time.LocalDate
import domain.SecureMessage
import models.AccountType
import utils.SpecBase

class SecureMessageConnectorSpec extends SpecBase {

  "SecureMessageConnector" should {
    "Populate RequestCommon" in new Setup {

      val commonRequest = SecureMessage.RequestCommon(
        externalRef = SecureMessage.ExternalReference("123456","mdtp"),
        recipient = SecureMessage.Recipient("CDS Financials",
          SecureMessage.TaxIdentifier("HMRC-CUS-ORG", "123123123")),
        params = SecureMessage.Params(LocalDate.now(), LocalDate.now(), "Financials"),
        email = "email@email.com",
        tags = SecureMessage.Tags("CDS Financials"),
        content = contents,
        messageType = "newMessageAlert",
        validForm = LocalDate.now().toString(),
        alertQueue = "DEFAULT"
      )

      commonRequest mustBe compareRequest
    }
  }

  trait Setup {

    val alert = "DEFAULT"
    val mType = "newMessageAlert"

    val contents: List[SecureMessage.Content] = List(
      SecureMessage.Content("en", AccountType("asd"), "asd"),
      SecureMessage.Content("cy", AccountType("asd"), "asd")
    )

    val compareRequest = SecureMessage.RequestCommon(
      externalRef = SecureMessage.ExternalReference("123456","mdtp"),
      recipient = SecureMessage.Recipient("CDS Financials",
        SecureMessage.TaxIdentifier("HMRC-CUS-ORG", "123123123")),
      params = SecureMessage.Params(LocalDate.now(), LocalDate.now(), "Financials"),
      email = "email@email.com",
      tags = SecureMessage.Tags("CDS Financials"),
      content = contents,
      messageType = mType,
      validForm = LocalDate.now().toString(),
      alertQueue = alert
    )
  }
}
