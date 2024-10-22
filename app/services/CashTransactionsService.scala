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

import connectors.{Acc31Connector, Acc44Connector, Acc45Connector}
import domain.CashTransactions
import models.ErrorResponse
import models.requests.CashAccountStatementRequestDetail
import models.requests.CashAccountTransactionSearchRequestDetails
import models.responses.ErrorSource.backEnd
import models.responses.EtmpErrorCode.INVALID_CASH_ACCOUNT_STATUS_TEXT
import models.responses._
import uk.gov.hmrc.http.HeaderCarrier
import utils.Utils.hyphen

import java.time.LocalDate
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class CashTransactionsService @Inject()(acc31Connector: Acc31Connector,
                                        acc44Connector: Acc44Connector,
                                        acc45Connector: Acc45Connector,
                                        domainService: DomainService,
                                        auditingService: AuditingService)(implicit executionContext: ExecutionContext) {
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

  def retrieveCashAccountTransactions(request: CashAccountTransactionSearchRequestDetails)
                                     (implicit hc: HeaderCarrier): Future[Either[ErrorDetail,
    CashAccountTransactionSearchResponseDetail]] = {

    auditingService.auditCashAccountTransactionsSearch(request)

    acc44Connector.cashAccountTransactionSearch(request).map {
      case Right(resValue) => populateSuccessfulResponseDetail(resValue)
      case Left(errorDetails) => Left(errorDetails)
    }
  }

  def submitCashAccountStatementRequest(request: CashAccountStatementRequestDetail)
                                       (implicit hc: HeaderCarrier): Future[Either[ErrorDetail, Acc45ResponseCommon]] = {
    auditingService.auditCashAccountStatementsRequest(request)

    acc45Connector.submitStatementRequest(request)
  }

  private def populateSuccessfulResponseDetail(resValue: CashAccountTransactionSearchResponseContainer): Either[ErrorDetail,
    CashAccountTransactionSearchResponseDetail] = {

    val responseDetailOptional = resValue.cashAccountTransactionSearchResponse.responseDetail

    if (responseDetailOptional.isDefined) {
      Right(responseDetailOptional.get)
    } else {
      Left(populateErrorDetails(resValue.cashAccountTransactionSearchResponse.responseCommon))
    }
  }

  private def populateErrorDetails(cashTranResponseCommon: CashTransactionsResponseCommon): ErrorDetail = {
    val statusText = cashTranResponseCommon.statusText.getOrElse(INVALID_CASH_ACCOUNT_STATUS_TEXT)
    val statusTextAfterSplitByHyphen: Array[String] = statusText.split(hyphen)

    val etmpErrorCode: String = statusTextAfterSplitByHyphen.head
    val etmpErrorMessage = statusTextAfterSplitByHyphen.tail.head

    ErrorDetail(
      timestamp = cashTranResponseCommon.processingDate,
      correlationId = "NA",
      errorCode = etmpErrorCode,
      errorMessage = etmpErrorMessage,
      source = backEnd,
      sourceFaultDetail = SourceFaultDetail(Seq())
    )
  }
}
