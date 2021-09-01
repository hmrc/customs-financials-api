/*
 * Copyright 2021 HM Revenue & Customs
 *
 */

package services

import connectors.Acc28Connector
import domain.GuaranteeTransaction
import models.ErrorResponse
import models.requests.GuaranteeAccountTransactionsRequest
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class GuaranteeTransactionsService @Inject()(acc28Connector: Acc28Connector,
                                             domainService: DomainService)(implicit executionContext: ExecutionContext) {

  def retrieveGuaranteeTransactionsSummary(request: GuaranteeAccountTransactionsRequest)(implicit hc: HeaderCarrier): Future[Either[ErrorResponse, Seq[GuaranteeTransaction]]] = {
    acc28Connector.retrieveGuaranteeTransactions(request, hc.requestId).map(_.map(_.map(domainService.toDomainSummary)))
  }

  def retrieveGuaranteeTransactionsDetail(request: GuaranteeAccountTransactionsRequest)(implicit hc: HeaderCarrier): Future[Either[ErrorResponse, Seq[GuaranteeTransaction]]] = {
    acc28Connector.retrieveGuaranteeTransactions(request, hc.requestId).map(_.map(_.map(domainService.toDomainDetail)))
  }
}
