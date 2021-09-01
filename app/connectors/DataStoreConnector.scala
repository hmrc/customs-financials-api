/*
 * Copyright 2021 HM Revenue & Customs
 *
 */

package connectors

import config.AppConfig
import models.{EORI, EmailAddress}
import play.api.libs.json._
import play.api.{Logger, LoggerLike}
import services.MetricsReporterService
import uk.gov.hmrc.http.HttpReads.Implicits._
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient}

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class DataStoreConnector @Inject()(http: HttpClient,
                                   metricsReporter: MetricsReporterService)(implicit appConfig: AppConfig, ec: ExecutionContext) {

  val log: LoggerLike = Logger(this.getClass)

  def getVerifiedEmail(eori: EORI)(implicit hc: HeaderCarrier): Future[Option[EmailAddress]] =
    metricsReporter.withResponseTimeLogging("customs-data-store.get.verified-email") {
      val dataStoreEmailEndpoint = appConfig.dataStoreEndpoint + s"/eori/${eori.value}/verified-email"
      http.GET[EmailResponse](dataStoreEmailEndpoint)
        .map(_.address)
        .recover {
          case _ => None
        }
    }

  def getEoriHistory(eori: EORI)(implicit hc: HeaderCarrier): Future[Seq[EORI]] = {
    val dataStoreEndpoint = appConfig.dataStoreEndpoint + s"/eori/${eori.value}/eori-history"
    metricsReporter.withResponseTimeLogging("customs-data-store.get.eori-history") {
      http.GET[EoriHistoryResponse](dataStoreEndpoint)
        .map(response => response.eoriHistory.map(_.eori))
    }.recover {
      case _ => Seq.empty
    }
  }
}

case class EoriPeriod(
                       eori: EORI,
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
