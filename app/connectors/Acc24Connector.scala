/*
 * Copyright 2021 HM Revenue & Customs
 *
 */

package connectors

import config.AppConfig
import models.requests.{HistoricDocumentRequest, HistoricStatementRequest}
import play.api.http.Status
import services.MetricsReporterService
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient, HttpResponse, RequestId}
import uk.gov.hmrc.http.HttpReads.Implicits._

import java.util.UUID
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class Acc24Connector @Inject()(httpClient: HttpClient,
                               appConfig: AppConfig,
                               metricsReporterService: MetricsReporterService,
                               mdgHeaders: MdgHeaders)(implicit executionContext: ExecutionContext) {

  def sendHistoricDocumentRequest(historicDocumentRequest: HistoricDocumentRequest, requestId: Option[RequestId]): Future[Boolean] = {
    metricsReporterService.withResponseTimeLogging("hods.post.historical-statement-retrieval") {
      httpClient.POST[HistoricStatementRequest, HttpResponse](
        appConfig.acc24HistoricalStatementRetrievalEndpoint,
        HistoricStatementRequest.from(historicDocumentRequest, UUID.randomUUID()),
        mdgHeaders.headers(appConfig.acc24BearerToken, requestId, appConfig.acc24HostHeader)
      )(implicitly, implicitly, HeaderCarrier(), implicitly).map { response =>
        response.status match {
          case Status.NO_CONTENT => true
          case _ => false
        }
      }.recover {
        case _ => false
      }
    }
  }
}
