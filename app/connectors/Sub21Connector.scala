/*
 * Copyright 2021 HM Revenue & Customs
 *
 */

package connectors

import com.google.inject.Inject
import config.AppConfig
import models.EORI
import models.responses.HistoricEoriResponse
import services._
import uk.gov.hmrc.http.HttpReads.Implicits._
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient}

import javax.inject.Singleton
import scala.concurrent._

@Singleton
class Sub21Connector @Inject()(appConfig: AppConfig,
                               metricsReporterService: MetricsReporterService,
                               httpClient: HttpClient)(implicit ec: ExecutionContext) {

  def getEORIHistory(eori: EORI): Future[HistoricEoriResponse] = {
    implicit val hc: HeaderCarrier = HeaderCarrier()
    metricsReporterService.withResponseTimeLogging("hods.get.get-eori-history.validate") {
      val url = s"${appConfig.sub21CheckEORIValidEndpoint}?eori=${eori.value}"
      httpClient.GET[HistoricEoriResponse](
        url,
        headers = Seq("Authorization" -> s"Bearer ${appConfig.sub21BearerToken}")
      )
    }
  }
}
