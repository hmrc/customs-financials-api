/*
 * Copyright 2021 HM Revenue & Customs
 *
 */

package services

import connectors.Sub09Connector
import domain.sub09.EmailVerifiedResponse
import models.EORI
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class SubscriptionService @Inject()(sub09Connector: Sub09Connector)(implicit ec: ExecutionContext) {

  def getVerifiedEmail(eori: EORI)(implicit hc: HeaderCarrier): Future[EmailVerifiedResponse] = {
    for (subscription <- sub09Connector.getSubscriptions(eori, hc.requestId))
      yield {
        subscription.subscriptionDisplayResponse.responseDetail.contactInformation match {
          case Some(ci) if ci.emailVerificationTimestamp.isDefined => EmailVerifiedResponse(ci.emailAddress)
          case _ => EmailVerifiedResponse(None)
        }
      }
  }
}
