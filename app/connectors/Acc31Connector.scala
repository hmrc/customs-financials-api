/*
 * Copyright 2022 HM Revenue & Customs
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

package connectors

import config.AppConfig
import models.requests._
import models.responses.{CashTransactionsResponse, CashTransactionsResponseCommon, CashTransactionsResponseDetail, GetCashAccountTransactionListingResponse}
import models.{ErrorResponse, ExceededThresholdErrorException, NoAssociatedDataException}
import play.api.{Logger, LoggerLike}
import services.{DateTimeService, MetricsReporterService}
import uk.gov.hmrc.http.HttpReads.Implicits._
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient}

import java.time.LocalDate
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class Acc31Connector @Inject()(httpClient: HttpClient,
                               appConfig: AppConfig,
                               dateTimeService: DateTimeService,
                               metricsReporterService: MetricsReporterService,
                               mdgHeaders: MdgHeaders)(implicit executionContext: ExecutionContext) {

  val log: LoggerLike = Logger(this.getClass)

  def retrieveCashTransactions(can: String,
                               from: LocalDate,
                               to: LocalDate
                              ): Future[Either[ErrorResponse, Option[CashTransactionsResponseDetail]]] = {

    val requestCommon = CashTransactionsRequestCommon(
      "MDTP",
      dateTimeService.currentDateTimeAsIso8601,
      mdgHeaders.acknowledgementReference
    )

    val requestDetail = CashTransactionsRequestDetail(
      can,
      CashTransactionsRequestDates(from.toString, to.toString)
    )

    val cashTransactionsRequest = CashTransactionsRequest(
      GetCashAccountTransactionListingRequest(requestCommon, requestDetail)
    )

    metricsReporterService.withResponseTimeLogging("hods.post.get-cash-account-transaction-listing") {
      val eventualResponse = httpClient.POST[CashTransactionsRequest, CashTransactionsResponse](
        appConfig.acc31GetCashAccountTransactionListingEndpoint,
        cashTransactionsRequest,
        headers = mdgHeaders.headers(appConfig.acc31BearerToken, appConfig.acc31HostHeader)
      )(implicitly, implicitly, HeaderCarrier(), implicitly)
      eventualResponse.map { ctr => cashAccountTransactions(ctr.getCashAccountTransactionListingResponse) }
    }
  }

  private def cashAccountTransactions(resp: GetCashAccountTransactionListingResponse): Either[ErrorResponse, Option[CashTransactionsResponseDetail]] = resp.responseCommon match {
    case CashTransactionsResponseCommon(status@"OK", Some(msg), _) if msg.contains("025-No associated data found") =>
      log.info(s"$status: $msg"); Left(NoAssociatedDataException)
    case CashTransactionsResponseCommon(status@"OK", Some(msg), _) if msg.contains("091-The query has exceeded the threshold, please refine the search") =>
      log.info(s"$status: $msg"); Left(ExceededThresholdErrorException)
    case _ => Right(resp.responseDetail)
  }
}
