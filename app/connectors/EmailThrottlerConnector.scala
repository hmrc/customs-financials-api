/*
 * Copyright 2021 HM Revenue & Customs
 *
 */

package connectors

import config.AppConfig
import models.requests.EmailRequest
import play.api.http.Status
import play.api.{Logger, LoggerLike}
import services.MetricsReporterService
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient, HttpResponse}
import uk.gov.hmrc.http.HttpReads.Implicits._
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class EmailThrottlerConnector @Inject()(http: HttpClient,
                                        metricsReporter: MetricsReporterService)
                                       (implicit appConfig: AppConfig, ec: ExecutionContext) {

  val log: LoggerLike = Logger(this.getClass)

  def sendEmail(request: EmailRequest)(implicit hc: HeaderCarrier): Future[Boolean] = {
    metricsReporter.withResponseTimeLogging(s"email.post.${request.templateId}") {

      http.POST[EmailRequest, HttpResponse](appConfig.sendEmailEndpoint, request).collect {
        case response if (response.status == Status.ACCEPTED) =>
          log.info(s"successfuly sent email notification for ${request.templateId}")
          true
        case response =>
          log.error(s"Send email failed with status - ${response.status}")
          false
      }.recover {
        case ex: Throwable =>
          log.error(s"Send email threw an exception - ${ex.getMessage}")
          false
      }
    }
  }
}


