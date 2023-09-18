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

package services

import connectors.Sub09Connector
import domain.sub09.{EmailVerifiedResponse, _}
import models.{EORI, EmailAddress}
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.Helpers.running
import play.api.{Application, inject}
import uk.gov.hmrc.http.HeaderCarrier
import utils.SpecBase

import scala.concurrent.{ExecutionContext, Future}

class SubscriptionServiceSpec extends SpecBase {

  "SubscriptionService" when {

    "calling Sub09 get subscriptions" should {
      "get SubscriptionDisplayResponse" in new Setup {
        when(mockSub09Connector.getSubscriptions(EORI("Trader EORI"))).thenReturn(
          Future.successful(subscriptionResponse))

        running(app) {
          val result = service.getVerifiedEmail(EORI("Trader EORI"))
          result.map {
            ev => ev mustBe EmailVerifiedResponse(None)
          }
        }
      }
    }

    "getEmailAddress" should {
      "return correct output when there is no contactInformation" in new Setup {
        when(mockSub09Connector.getSubscriptions(any)).thenReturn(Future.successful(subscriptionResponse))

        running(app) {
          val result: Future[EmailVerifiedResponse] = service.getEmailAddress(EORI("Trader EORI"))

          result.map {
            ev => ev mustBe EmailVerifiedResponse(None)
          }
        }
      }

      "return correct output when contactInformation is available" in new Setup {
        when(mockSub09Connector.getSubscriptions(any)).thenReturn(Future.successful(subscriptionResponseWithContactInfo))

        running(app) {
          val result: Future[EmailVerifiedResponse] = service.getEmailAddress(EORI("Trader EORI"))

          result.map {
            ev => ev mustBe EmailVerifiedResponse(Option(EmailAddress(emailAddress)))
          }
        }
      }
    }

    "getUnverifiedEmail" should {
      "return correct output when there is no contactInformation" in new Setup {
        when(mockSub09Connector.getSubscriptions(any)).thenReturn(Future.successful(subscriptionResponse))

        running(app) {
          val result: Future[EmailUnverifiedResponse] = service.getUnverifiedEmail(EORI("Trader EORI"))

          result.map {
            eunv => eunv mustBe EmailUnverifiedResponse(None)
          }
        }
      }

      "return correct output when contactInformation is available" in new Setup {
        when(mockSub09Connector.getSubscriptions(any)).thenReturn(Future.successful(
          subscriptionResponseWithContactInfo))

        running(app) {
          val result: Future[EmailUnverifiedResponse] = service.getUnverifiedEmail(EORI("Trader EORI"))

          result.map {
            eunv => eunv mustBe EmailUnverifiedResponse(Option(EmailAddress(emailAddress)))
          }
        }
      }
    }
  }

  trait Setup {
    implicit val hc: HeaderCarrier = HeaderCarrier()
    implicit val executionContext: ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global

    val eori: EORI = EORI("testEORI")
    val mockSub09Connector: Sub09Connector = mock[Sub09Connector]

    val app: Application = GuiceApplicationBuilder().overrides(
      inject.bind[Sub09Connector].toInstance(mockSub09Connector)
    ).configure(
      "microservice.metrics.enabled" -> false,
      "metrics.enabled" -> false,
      "auditing.enabled" -> false
    ).build()

    val service: SubscriptionService = app.injector.instanceOf[SubscriptionService]

    val responseCommon: ResponseCommon = ResponseCommon("OK", None, "2020-10-05T09:30:47Z", None)
    val cdsEstablishmentAddress: CdsEstablishmentAddress = CdsEstablishmentAddress(
      "1 street", "Southampton", Some("SO1 1AA"), "GB")

    val responseDetail: ResponseDetail = ResponseDetail(Some(eori), None, None, "CDSFullName",
      cdsEstablishmentAddress, None,
      None, None, None, None, None, None, None, ETMP_Master_Indicator = true, None)

    val emailAddress = "test@gmil.com"

    val contactInfo: ContactInformation = ContactInformation(None, None, None, None, None, None, None, None,
      emailAddress = Some(EmailAddress(emailAddress)), None)

    val responseDetailWithContactInfo: ResponseDetail = responseDetail.copy(contactInformation = Some(contactInfo))

    val subscriptionResponse: SubscriptionResponse = SubscriptionResponse(
      SubscriptionDisplayResponse(responseCommon, responseDetail))

    val subscriptionResponseWithContactInfo: SubscriptionResponse = SubscriptionResponse(
      SubscriptionDisplayResponse(responseCommon, responseDetailWithContactInfo))
  }
}
