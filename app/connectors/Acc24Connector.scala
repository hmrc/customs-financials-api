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
import play.api.libs.ws.writeableOf_JsValue
import play.api.{Logger, LoggerLike}
import services.MetricsReporterService
import uk.gov.hmrc.http.HttpReads.Implicits.*
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse, StringContextOps}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class Acc24Connector @Inject() (
  httpClient: HttpClientV2,
  appConfig: AppConfig,
  metricsReporterService: MetricsReporterService,
  mdgHeaders: MdgHeaders
)(implicit executionContext: ExecutionContext) {

  val log: LoggerLike = Logger(this.getClass)

  def sendHistoricDocumentRequest(historicDocumentRequest: HistoricDocumentRequest): Future[Boolean] =
    metricsReporterService.withResponseTimeLogging("hods.post.historical-statement-retrieval") {

      val acc24Url = url"${appConfig.acc24HistoricalStatementRetrievalEndpoint}"

      httpClient
        .post(acc24Url)(HeaderCarrier())
        .withBody[HistoricStatementRequest](HistoricStatementRequest.from(historicDocumentRequest))
        .setHeader(mdgHeaders.headers(appConfig.acc24BearerToken, appConfig.acc24HostHeader): _*)
        .execute[HttpResponse]
        .map { response =>
          log.info(s"HistoricDocumentResponse :  $response")
          response.status match {
            case Status.NO_CONTENT => true
            case _                 => false
          }
        }
        .recover { case _ =>
          false
        }
    }
}
