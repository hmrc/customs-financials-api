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
import models.{EORI, EmailAddress, CompanyInformation}
import play.api.libs.json._
import play.api.{Logger, LoggerLike}
import services.MetricsReporterService
import uk.gov.hmrc.http.HttpReads.Implicits._
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient}

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class DataStoreConnector @Inject()(http: HttpClient,
                                   metricsReporter: MetricsReporterService)
                                  (implicit appConfig: AppConfig, ec: ExecutionContext) {

  val log: LoggerLike = Logger(this.getClass)

  def getVerifiedEmail(eori: EORI)(implicit hc: HeaderCarrier): Future[Option[EmailAddress]] = {
    metricsReporter.withResponseTimeLogging(resourceName = "customs-data-store.get.verified-email") {
      val dataStoreEmailEndpoint = appConfig.dataStoreEndpoint + s"/eori/${eori.value}/verified-email"

      http.GET[EmailResponse](dataStoreEmailEndpoint)
        .map(_.address)
        .recover {
          case e =>
            log.error(s"Call to data stored failed for getVerifiedEmail exception=$e")
            None
        }
    }
  }

  def getEoriHistory(eori: EORI)(implicit hc: HeaderCarrier): Future[Seq[EORI]] = {
    val dataStoreHistoryEndpoint = appConfig.dataStoreEndpoint + s"/eori/${eori.value}/eori-history"

    metricsReporter.withResponseTimeLogging("customs-data-store.get.eori-history") {
      http.GET[EoriHistoryResponse](dataStoreHistoryEndpoint)
        .map(response => response.eoriHistory.map(_.eori))
    }.recover {
      case e =>
        log.error(s"Call to data stored failed for getEoriHistory exception=$e")
        Seq.empty
    }
  }

  def getCompanyName(eori: EORI)(implicit hc: HeaderCarrier): Future[Option[String]] = {
    metricsReporter.withResponseTimeLogging("customs-data-store.get.company-name") {
      val dataStoreCorpEndpoint = appConfig.dataStoreEndpoint + s"/eori/${eori.value}/company-information"
      http.GET[CompanyInformation](dataStoreCorpEndpoint).map(response => Some(response.name))
    }.recover {
      case e =>
        log.error(s"Call to data stored failed for getCompanyName exception=$e")
        None
    }
  }

}

case class EoriPeriod(eori: EORI,
                      validFrom: Option[String],
                      validUntil: Option[String])

object EoriPeriod {
  implicit val format: OFormat[EoriPeriod] = Json.format[EoriPeriod]
}

case class EoriHistoryResponse(eoriHistory: Seq[EoriPeriod])

object EoriHistoryResponse {
  implicit val format: OFormat[EoriHistoryResponse] = Json.format[EoriHistoryResponse]
}

case class EmailResponse(address: Option[EmailAddress], timestamp: Option[String])

object EmailResponse {
  implicit val format: OFormat[EmailResponse] = Json.format[EmailResponse]
}
