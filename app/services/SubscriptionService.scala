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
import domain.sub09.{EmailUnverifiedResponse, EmailVerifiedResponse}
import models.EORI

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class SubscriptionService @Inject() (sub09Connector: Sub09Connector)(implicit ec: ExecutionContext) {

  def getVerifiedEmail(eori: EORI): Future[EmailVerifiedResponse] =
    for (subscription <- sub09Connector.getSubscriptions(eori))
      yield subscription.subscriptionDisplayResponse.responseDetail.contactInformation match {
        case Some(ci) if ci.emailVerificationTimestamp.isDefined => EmailVerifiedResponse(ci.emailAddress)
        case _                                                   => EmailVerifiedResponse(None)
      }

  def getEmailAddress(eori: EORI): Future[EmailVerifiedResponse] =
    for (subscription <- sub09Connector.getSubscriptions(eori))
      yield subscription.subscriptionDisplayResponse.responseDetail.contactInformation match {
        case Some(ci) if ci.emailAddress.isDefined => EmailVerifiedResponse(ci.emailAddress)
        case _                                     => EmailVerifiedResponse(None)
      }

  def getUnverifiedEmail(eori: EORI): Future[EmailUnverifiedResponse] =
    for (subscription <- sub09Connector.getSubscriptions(eori))
      yield subscription.subscriptionDisplayResponse.responseDetail.contactInformation match {
        case Some(ci) if ci.emailVerificationTimestamp.isEmpty => EmailUnverifiedResponse(ci.emailAddress)
        case _                                                 => EmailUnverifiedResponse(None)
      }
}
