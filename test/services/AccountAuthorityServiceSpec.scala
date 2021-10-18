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

package services

import connectors.{Acc29Connector, Acc30Connector}
import domain._
import models.requests.manageAuthorities._
import models.{AccountNumber, AccountStatus, AccountType, EORI}
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.Helpers.running
import play.api.{Application, inject}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.audit.http.connector.AuditResult
import utils.SpecBase

import scala.concurrent._

class AccountAuthorityServiceSpec extends SpecBase {
  "AccountAuthorityService" when {
    "calling ACC29 (get account authorities)" should {
      "get a list of accounts with authorities" in new Setup {
        running(app) {
          val aListOfAccountWithAuthorities = Seq(AccountWithAuthorities(AccountType("CDSCash"), AccountNumber("123456"), AccountStatus("Open"), Seq(StandingAuthority(EORI("Agent EORI"), "from date", Some("to date"), viewBalance = false))))
          when(mockAcc29Connector.getStandingAuthorities(eqTo(EORI("Trader EORI")))).thenReturn(Future.successful(aListOfAccountWithAuthorities))
          val result = await(service.getAccountAuthorities(EORI("Trader EORI")))
          result mustBe aListOfAccountWithAuthorities
        }
      }
    }

    "calling ACC30 grantAccountAuthorities" should {

      val grantAuthorityRequest = GrantAuthorityRequest(
        Accounts(Some("345"), Seq("123", "754"), Some("54345")),
        StandingAuthority(EORI("authorisedEori"), "2018-11-09", None, viewBalance = true),
        AuthorisedUser("some name", "some role"),
        editRequest = false
      )

      "propagate the connector's result when true" in new Setup {
        when(mockAuditingService.auditGrantAuthority(any, any)(any)).thenReturn(Future.successful(AuditResult.Success))

        running(app) {
          when(mockAcc30Connector.grantAccountAuthorities(eqTo(grantAuthorityRequest), eqTo(eori))).thenReturn(Future.successful(true))
          val actualResult = await(service.grantAccountAuthorities(grantAuthorityRequest, EORI("testEORI")))
          actualResult mustBe true
        }
      }

      "propagate the connector's result when false" in new Setup {
        when(mockAuditingService.auditGrantAuthority(any, any)(any)).thenReturn(Future.successful(AuditResult.Success))

        running(app) {
          when(mockAcc30Connector.grantAccountAuthorities(eqTo(grantAuthorityRequest), eqTo(eori))).thenReturn(Future.successful(false))
          val actualResult = await(service.grantAccountAuthorities(grantAuthorityRequest, EORI("testEORI")))
          actualResult mustBe false
        }
      }
    }

    "calling ACC30 revokeAccountAuthorities" should {

      val revokeAuthorityRequest = RevokeAuthorityRequest(
        AccountNumber("123"), CdsCashAccount, EORI("authorisedEori"), AuthorisedUser("some name", "some role")
      )

      "propagate the connector's result when true" in new Setup {
        when(mockAuditingService.auditRevokeAuthority(any, any)(any)).thenReturn(Future.successful(AuditResult.Success))

        running(app) {
          when(mockAcc30Connector.revokeAccountAuthorities(eqTo(revokeAuthorityRequest), eqTo(eori))).thenReturn(Future.successful(true))
          val actualResult = await(service.revokeAccountAuthorities(revokeAuthorityRequest, EORI("testEORI")))
          actualResult mustBe true
        }
      }

      "propagate the connector's result when false" in new Setup {
        when(mockAuditingService.auditRevokeAuthority(any, any)(any)).thenReturn(Future.successful(AuditResult.Success))

        running(app) {
          when(mockAcc30Connector.revokeAccountAuthorities(eqTo(revokeAuthorityRequest), eqTo(eori))).thenReturn(Future.successful(false))
          val actualResult = await(service.revokeAccountAuthorities(revokeAuthorityRequest, EORI("testEORI")))
          actualResult mustBe false
        }
      }
    }
  }

  trait Setup {
    implicit val hc: HeaderCarrier = HeaderCarrier()
    implicit val executionContext: ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global

    val eori: EORI = EORI("testEORI")
    val mockAcc29Connector: Acc29Connector = mock[Acc29Connector]
    val mockAcc30Connector: Acc30Connector = mock[Acc30Connector]
    val mockAuditingService: AuditingService = mock[AuditingService]

    val app: Application = GuiceApplicationBuilder().overrides(
      inject.bind[Acc29Connector].toInstance(mockAcc29Connector),
      inject.bind[Acc30Connector].toInstance(mockAcc30Connector),
      inject.bind[AuditingService].toInstance(mockAuditingService)
    ).configure(
      "microservice.metrics.enabled" -> false,
      "metrics.enabled" -> false,
      "auditing.enabled" -> false
    ).build()

    val service: AccountAuthorityService = app.injector.instanceOf[AccountAuthorityService]
  }
}
