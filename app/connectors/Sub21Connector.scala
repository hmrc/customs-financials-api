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

import com.google.inject.Inject
import config.AppConfig
import models.EORI
import models.responses.HistoricEoriResponse
import services.*
import uk.gov.hmrc.http.HttpReads.Implicits.*
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, StringContextOps}

import javax.inject.Singleton
import scala.concurrent.*

@Singleton
class Sub21Connector @Inject() (
  appConfig: AppConfig,
  metricsReporterService: MetricsReporterService,
  httpClient: HttpClientV2
)(implicit ec: ExecutionContext) {

  def getEORIHistory(eori: EORI): Future[HistoricEoriResponse] = {
    implicit val hc: HeaderCarrier = HeaderCarrier()

    metricsReporterService.withResponseTimeLogging("hods.get.get-eori-history.validate") {
      val sub21Url = s"${appConfig.sub21CheckEORIValidEndpoint}?eori=${eori.value}"

      httpClient
        .get(url"$sub21Url")
        .setHeader("Authorization" -> s"Bearer ${appConfig.sub21BearerToken}")
        .execute[HistoricEoriResponse]
    }
  }
}
