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

import domain.{RequestDetail, SecureMessage}
import models.{AccountType, EORI}
import play.api.Application
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.Helpers.running
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient}
import utils.SpecBase

class SecureMessageConnectorSpec extends SpecBase {

  "SecureMessageConnector" should {
    "Populate RequestCommon" in new Setup {

      val commonRequest = SecureMessage.RequestCommon(
        externalRef = SecureMessage.ExternalReference("123456", "mdtp"),
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

    "getSubjetHeader" should {
      "return DutyDefermentStatement" in new Setup {
        running(app) {
          val result = connector.getSubjectHeader("DutyDefermentStatement")
          result mustBe dutyStatement
        }
      }

      "return C79Certificate" in new Setup {
        running(app) {
          val result = connector.getSubjectHeader("C79Certificate")
          result mustBe c79cert
        }
      }
      "return SecurityStatement" in new Setup {
        running(app) {
          val result = connector.getSubjectHeader("SecurityStatement")
          result mustBe sercStatement
        }
      }
      "return PostponedVATStatement" in new Setup {
        running(app) {
          val result = connector.getSubjectHeader("PostponedVATStatement")
          result mustBe PostPonedVATStatement
        }
      }

      "getContents" should {
        "return eng and cy in list" in new Setup {
          running(app) {
            val result = connector.getContents(dutyStatement)
            result mustBe TestContents
          }
        }
      }
    }

    "getRequestDetails" should {
      "return requestDetails" in new Setup{
        running(app) {
          val result = connector.getRequestDetail(EORI("GB123456789"))
          result mustBe TestContents
        }
      }
    }
  }

  trait Setup {

    val alert = "DEFAULT"
    val mType = "newMessageAlert"

    val eori: EORI = EORI("GB123456789")
    val requestDetail = SecureMessage.RequestDetail(eori, Option(EORI("")))

    val dutyStatement = AccountType("DutyDefermentStatement")
    val c79cert = AccountType("C79Certificate")
    val sercStatement = AccountType("SecurityStatement")
    val PostPonedVATStatement = AccountType("PostponedVATStatement")

    val TestContents = {
      List(SecureMessage.Content("en", AccountType("DutyDefermentStatement"), SecureMessage.SecureMessage.body),
        SecureMessage.Content("cy", AccountType("DutyDefermentStatement"), SecureMessage.SecureMessage.body))
    }

    val contents: List[SecureMessage.Content] = List(
      SecureMessage.Content("en", AccountType("asd"), "asd"),
      SecureMessage.Content("cy", AccountType("asd"), "asd")
    )

    val compareRequest = SecureMessage.RequestCommon(
      externalRef = SecureMessage.ExternalReference("123456", "mdtp"),
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

    implicit val hc: HeaderCarrier = HeaderCarrier()
    val mockHttpClient: HttpClient = mock[HttpClient]

    val app: Application = GuiceApplicationBuilder().overrides(
      bind[HttpClient].toInstance(mockHttpClient)
    ).configure(
      "microservice.metrics.enabled" -> false,
      "metrics.enabled" -> false,
      "auditing.enabled" -> false
    ).build()

    val connector: SecureMessageConnector = app.injector.instanceOf[SecureMessageConnector]
  }

}
