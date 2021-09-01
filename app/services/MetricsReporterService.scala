/*
 * Copyright 2021 HM Revenue & Customs
 *
 */

package services

import com.google.inject.Inject
import com.kenshoo.play.metrics.Metrics
import play.api.http.Status
import uk.gov.hmrc.http.{BadRequestException, NotFoundException, UpstreamErrorResponse}

import javax.inject.Singleton
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}


@Singleton
class MetricsReporterService @Inject()(val metrics: Metrics, dateTimeService: DateTimeService) {

  def withResponseTimeLogging[T](resourceName: String)(future: Future[T])
                                (implicit ec: ExecutionContext): Future[T] = {
    val startTimeStamp = dateTimeService.timeStamp()
    future.andThen { case response =>
      val httpResponseCode = response match {
        case Success(_) => Status.OK
        case Failure(exception: NotFoundException) => exception.responseCode
        case Failure(exception: BadRequestException) => exception.responseCode
        case Failure(exception: UpstreamErrorResponse) => exception.statusCode
        case Failure(_) => Status.INTERNAL_SERVER_ERROR
      }
      updateResponseTimeHistogram(resourceName, httpResponseCode, startTimeStamp, dateTimeService.timeStamp())
    }
  }

  def updateResponseTimeHistogram(resourceName: String, httpResponseCode: Int,
                                  startTimeStamp: Long, endTimeStamp: Long): Unit = {
    val RESPONSE_TIMES_METRIC = "responseTimes"
    val histogramName = s"$RESPONSE_TIMES_METRIC.$resourceName.$httpResponseCode"
    val elapsedTimeInMillis = endTimeStamp - startTimeStamp
    metrics.defaultRegistry.histogram(histogramName).update(elapsedTimeInMillis)
  }
}
