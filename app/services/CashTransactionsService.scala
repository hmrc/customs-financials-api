/*
 * Copyright 2021 HM Revenue & Customs
 *
 */

package services

import connectors.Acc31Connector
import domain.CashTransactions
import models.ErrorResponse
import uk.gov.hmrc.http.HeaderCarrier

import java.time.LocalDate
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class CashTransactionsService @Inject()(acc31Connector: Acc31Connector,
                                        domainService: DomainService)(implicit executionContext: ExecutionContext) {
  def retrieveCashTransactionsSummary(can: String, from: LocalDate, to: LocalDate)(implicit hc: HeaderCarrier): Future[Either[ErrorResponse, CashTransactions]] = {
    acc31Connector.retrieveCashTransactions(can, from, to, hc.requestId).map {
      case Right(value) =>
        value match {
          case Some(responseDetail) => Right(domainService.toDomainSummary(responseDetail))
          case None => Right(CashTransactions(Nil, Nil))
        }
      case Left(errorValue) => Left(errorValue)
    }
  }

  def retrieveCashTransactionsDetail(can: String, from: LocalDate, to: LocalDate)(implicit hc: HeaderCarrier): Future[Either[ErrorResponse, CashTransactions]] = {
    acc31Connector.retrieveCashTransactions(can, from, to, hc.requestId).map {
      case Right(value) =>
        value match {
          case Some(responseDetail) => Right(domainService.toDomainDetail(responseDetail))
          case None => Right(CashTransactions(Nil, Nil))
        }
      case Left(errorValue) => Left(errorValue)
    }
  }
}
