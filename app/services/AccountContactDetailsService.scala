/*
 * Copyright 2021 HM Revenue & Customs
 *
 */

package services

import connectors.{Acc37Connector, Acc38Connector}
import domain.acc37.ContactDetails
import models.{AccountNumber, EORI}
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.Inject
import scala.concurrent.Future

class AccountContactDetailsService @Inject()(acc38Connector: Acc38Connector,
                                             acc37Connector: Acc37Connector) {
  def getAccountContactDetails(dan: AccountNumber,
                               eori: EORI
                              )(implicit hc: HeaderCarrier): Future[domain.acc38.Response] = {
    acc38Connector.getAccountContactDetails(dan, eori, hc.requestId)
  }

  def updateAccountContactDetails(dan: AccountNumber,
                                  eori: EORI,
                                  contactDetails: ContactDetails
                                 )(implicit hc: HeaderCarrier): Future[domain.acc37.Response] = {
    acc37Connector.updateAccountContactDetails(dan, eori, contactDetails, hc.requestId)
  }
}
