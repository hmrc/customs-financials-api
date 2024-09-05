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

import connectors.{Acc31Connector, Acc45Connector}
import domain.CashTransactions
import models.ErrorResponse
import models.requests.CashAccountStatementRequestDetail
import models.responses.{CashAccountStatementResponseContainer, ErrorDetail}

import java.time.LocalDate
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class CashTransactionsService @Inject()(acc31Connector: Acc31Connector,
                                        acc45Connector: Acc45Connector,
                                        domainService: DomainService)(implicit executionContext: ExecutionContext) {
  def retrieveCashTransactionsSummary(can: String,
                                      from: LocalDate,
                                      to: LocalDate): Future[Either[ErrorResponse, CashTransactions]] = {
    acc31Connector.retrieveCashTransactions(can, from, to).map {
      case Right(value) =>
        value match {
          case Some(responseDetail) => Right(domainService.toDomainSummary(responseDetail))
          case None => Right(CashTransactions(Nil, Nil))
        }

      case Left(errorValue) => Left(errorValue)
    }
  }

  def retrieveCashTransactionsDetail(can: String,
                                     from: LocalDate,
                                     to: LocalDate): Future[Either[ErrorResponse, CashTransactions]] = {
    acc31Connector.retrieveCashTransactions(can, from, to).map {
      case Right(value) =>
        value match {
          case Some(responseDetail) => Right(domainService.toDomainDetail(responseDetail))
          case None => Right(CashTransactions(Nil, Nil))
        }

      case Left(errorValue) => Left(errorValue)
    }
  }


  def submitCashAccountStatementRequest(
                                         request: CashAccountStatementRequestDetail
                                       ): Future[Either[ErrorDetail, CashAccountStatementResponseContainer]] = {
    acc45Connector.submitStatementRequest(request)
  }
}
