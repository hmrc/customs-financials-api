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
import models.requests.{HistoricDocumentRequest, HistoricStatementRequest}
import play.api.http.Status
import play.api.{Logger, LoggerLike}
import services.MetricsReporterService
import uk.gov.hmrc.http.HttpReads.Implicits._
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient, HttpResponse}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class Acc24Connector @Inject()(httpClient: HttpClient,
                               appConfig: AppConfig,
                               metricsReporterService: MetricsReporterService,
                               mdgHeaders: MdgHeaders)(implicit executionContext: ExecutionContext) {

  val log: LoggerLike = Logger(this.getClass)

  def sendHistoricDocumentRequest(historicDocumentRequest: HistoricDocumentRequest): Future[Boolean] = {
    metricsReporterService.withResponseTimeLogging("hods.post.historical-statement-retrieval") {

      httpClient.POST[HistoricStatementRequest, HttpResponse](
        appConfig.acc24HistoricalStatementRetrievalEndpoint,
        HistoricStatementRequest.from(historicDocumentRequest),
        mdgHeaders.headers(appConfig.acc24BearerToken, appConfig.acc24HostHeader)
      )(implicitly, implicitly, HeaderCarrier(), implicitly).map { response =>
        log.info(s"HistoricDocumentResponse :  $response")
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
