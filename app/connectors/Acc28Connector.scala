/*
 * Copyright 2021 HM Revenue & Customs
 *
 */

package connectors

import config.AppConfig
import models.requests._
import models.responses.{GetGGATransactionResponse, GuaranteeTransactionDeclaration, GuaranteeTransactionsResponse, ResponseCommon}
import models.{ErrorResponse, ExceededThresholdErrorException, NoAssociatedDataException}
import play.api.{Logger, LoggerLike}
import services.{DateTimeService, MetricsReporterService}
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient, RequestId}
import uk.gov.hmrc.http.HttpReads.Implicits._

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class Acc28Connector @Inject()(httpClient: HttpClient,
                               appConfig: AppConfig,
                               dateTimeService: DateTimeService,
                               metricsReporterService: MetricsReporterService,
                               mdgHeaders: MdgHeaders)(implicit executionContext: ExecutionContext) {

  val log: LoggerLike = Logger(this.getClass)

  def retrieveGuaranteeTransactions(request: GuaranteeAccountTransactionsRequest, requestId: Option[RequestId]): Future[Either[ErrorResponse, Seq[GuaranteeTransactionDeclaration]]] = {

    val requestCommon: RequestCommon = RequestCommon(
      dateTimeService.currentDateTimeAsIso8601,
      mdgHeaders.acknowledgementReference(requestId),
      RequestParameters("REGIME", "CDS")
    )
    val guaranteeTransactionsRequest = GuaranteeTransactionsRequest(
      GGATransactionListing(requestCommon, request.toRequestDetail()(appConfig))
    )

    metricsReporterService.withResponseTimeLogging("hods.post.get-ggatransaction-listing") {
      httpClient.POST[GuaranteeTransactionsRequest, GuaranteeTransactionsResponse](
        appConfig.acc28GetGGATransactionEndpoint,
        guaranteeTransactionsRequest,
        headers = mdgHeaders.headers(appConfig.acc28BearerToken, requestId, appConfig.acc28HostHeader)
      )(implicitly, implicitly, HeaderCarrier(), implicitly).map {
        gtr => transactions(gtr.getGGATransactionResponse)
      }
    }
  }

  private def transactions(resp: GetGGATransactionResponse): Either[ErrorResponse, Seq[GuaranteeTransactionDeclaration]] = resp.responseCommon match {
    case ResponseCommon(status@"OK", Some(msg), _) if msg.contains("025-No associated data found") => log.info(s"$status: $msg"); Left(NoAssociatedDataException)
    case ResponseCommon(status@"OK", Some(msg), _) if msg.contains("091-The query has exceeded the threshold, please refine the search") => log.info(s"$status: $msg"); Left(ExceededThresholdErrorException)
    case _ => Right(resp.responseDetail.map(_.declarations).getOrElse(Seq.empty))
  }

}
