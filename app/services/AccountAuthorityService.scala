/*
 * Copyright 2021 HM Revenue & Customs
 *
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
class AccountAuthorityService @Inject()(acc29Connector: Acc29Connector,
                                        acc30Connector: Acc30Connector,
                                        auditingService: AuditingService) {


  def getAccountAuthorities(eori: EORI)(implicit hc: HeaderCarrier): Future[Seq[AccountWithAuthorities]] = {
    acc29Connector.getStandingAuthorities(eori, hc.requestId)
  }

  def grantAccountAuthorities(grantAuthorityRequest: GrantAuthorityRequest, eori: EORI)(implicit hc: HeaderCarrier): Future[Boolean] = {
    if (grantAuthorityRequest.editRequest) {
      auditingService.auditEditAuthority(grantAuthorityRequest, eori)
    } else {
      auditingService.auditGrantAuthority(grantAuthorityRequest, eori)
    }
    acc30Connector.grantAccountAuthorities(grantAuthorityRequest, eori, hc.requestId)
  }

  def revokeAccountAuthorities(revokeAuthorityRequest: RevokeAuthorityRequest, eori: EORI)(implicit hc: HeaderCarrier): Future[Boolean] = {
    auditingService.auditRevokeAuthority(revokeAuthorityRequest, eori)
    acc30Connector.revokeAccountAuthorities(revokeAuthorityRequest, eori, hc.requestId)
  }
}
