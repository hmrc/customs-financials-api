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

import connectors.{Acc29Connector, Acc30Connector}
import domain._
import models.EORI
import models.requests.manageAuthorities.{GrantAuthorityRequest, RevokeAuthorityRequest}
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.{Inject, Singleton}
import scala.concurrent.Future

@Singleton
class AccountAuthorityService @Inject() (
  acc29Connector: Acc29Connector,
  acc30Connector: Acc30Connector,
  auditingService: AuditingService
) {

  def getAccountAuthorities(eori: EORI): Future[Seq[AccountWithAuthorities]] =
    acc29Connector.getStandingAuthorities(eori)

  def grantAccountAuthorities(grantAuthorityRequest: GrantAuthorityRequest)(implicit
    hc: HeaderCarrier
  ): Future[Boolean] = {

    if (grantAuthorityRequest.editRequest) {
      auditingService.auditEditAuthority(grantAuthorityRequest)
    } else {
      auditingService.auditGrantAuthority(grantAuthorityRequest)
    }

    acc30Connector.grantAccountAuthorities(grantAuthorityRequest)
  }

  def revokeAccountAuthorities(revokeAuthorityRequest: RevokeAuthorityRequest)(implicit
    hc: HeaderCarrier
  ): Future[Boolean] = {
    auditingService.auditRevokeAuthority(revokeAuthorityRequest)
    acc30Connector.revokeAccountAuthorities(revokeAuthorityRequest)
  }
}
