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

package connectors

import config.AppConfig
import config.MetaConfig.Platform.REGIME_CDS
import models.requests.*
import models.responses.{
  GetGGATransactionResponse, GuaranteeTransactionDeclaration, GuaranteeTransactionsResponse, ResponseCommon
}
import models.{ErrorResponse, ExceededThresholdErrorException, NoAssociatedDataException}
import play.api.libs.ws.writeableOf_JsValue
import play.api.{Logger, LoggerLike}
import services.{DateTimeService, MetricsReporterService}
import uk.gov.hmrc.http.HttpReads.Implicits.*
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, StringContextOps}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class Acc28Connector @Inject() (
  httpClient: HttpClientV2,
  appConfig: AppConfig,
  dateTimeService: DateTimeService,
  metricsReporterService: MetricsReporterService,
  mdgHeaders: MdgHeaders
)(implicit executionContext: ExecutionContext) {

  val log: LoggerLike = Logger(this.getClass)

  def retrieveGuaranteeTransactions(
    request: GuaranteeAccountTransactionsRequest
  ): Future[Either[ErrorResponse, Seq[GuaranteeTransactionDeclaration]]] = {

    val requestCommon: RequestCommon = RequestCommon(
      dateTimeService.currentDateTimeAsIso8601,
      mdgHeaders.acknowledgementReference,
      RequestParameters("REGIME", REGIME_CDS)
    )

    val guaranteeTransactionsRequest = GuaranteeTransactionsRequest(
      GGATransactionListing(requestCommon, request.toRequestDetail()(appConfig))
    )

    metricsReporterService.withResponseTimeLogging("hods.post.get-ggatransaction-listing") {
      httpClient
        .post(url"${appConfig.acc28GetGGATransactionEndpoint}")(HeaderCarrier())
        .withBody[GuaranteeTransactionsRequest](guaranteeTransactionsRequest)
        .setHeader(mdgHeaders.headers(appConfig.acc28BearerToken, appConfig.acc28HostHeader): _*)
        .execute[GuaranteeTransactionsResponse]
        .map { gtr =>
          transactions(gtr.getGGATransactionResponse)
        }
    }
  }

  private def transactions(
    resp: GetGGATransactionResponse
  ): Either[ErrorResponse, Seq[GuaranteeTransactionDeclaration]] =
    resp.responseCommon match {
      case ResponseCommon(status @ "OK", Some(msg), _) if msg.contains("025-No associated data found") =>
        log.info(s"$status: $msg")
        Left(NoAssociatedDataException)

      case ResponseCommon(status @ "OK", Some(msg), _) if hasQueryExceededThreshold(msg) =>
        log.info(s"$status: $msg")
        Left(ExceededThresholdErrorException)

      case _ => Right(resp.responseDetail.map(_.declarations).getOrElse(Seq.empty))
    }

  private def hasQueryExceededThreshold(msg: String) =
    msg.contains("091-The query has exceeded the threshold, please refine the search")
}
