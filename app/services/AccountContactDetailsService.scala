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

import connectors.{Acc37Connector, Acc38Connector}
import domain.acc37.ContactDetails
import models.{AccountNumber, EORI}

import javax.inject.Inject
import scala.concurrent.Future

class AccountContactDetailsService @Inject()(acc38Connector: Acc38Connector,
                                             acc37Connector: Acc37Connector) {
  def getAccountContactDetails(dan: AccountNumber,
                               eori: EORI): Future[domain.acc38.Response] = {
    acc38Connector.getAccountContactDetails(dan, eori)
  }

  def updateAccountContactDetails(dan: AccountNumber,
                                  eori: EORI,
                                  contactDetails: ContactDetails): Future[domain.acc37.Response] = {
    acc37Connector.updateAccountContactDetails(dan, eori, contactDetails)
  }
}
