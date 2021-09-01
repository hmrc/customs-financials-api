/*
 * Copyright 2021 HM Revenue & Customs
 *
 */

package services

import com.codahale.metrics.{Histogram, MetricRegistry}
import com.kenshoo.play.metrics.Metrics
import play.api.http.Status
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.Helpers.running
import play.api.{Application, inject}
import uk.gov.hmrc.http._
import utils.SpecBase

import java.time.OffsetDateTime
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class MetricsReporterServiceSpec extends SpecBase {
  "MetricsReporterService" should {
    "withResponseTimeLogging" should {
        "log successful call metrics" in new Setup {
          running(app) {
            await {
              service.withResponseTimeLogging("foo") {
                Future.successful("OK")
              }
            }
            verify(mockRegistry).histogram("responseTimes.foo.200")
            verify(mockHistogram).update(elapsedTimeInMillis)
          }
        }

        "log default error during call metrics" in new Setup {
          running(app) {
            assertThrows[InternalServerException] {
              await {
                service.withResponseTimeLogging("bar") {
                  Future.failed(new InternalServerException("boom"))
                }
              }
            }
            verify(mockRegistry).histogram("responseTimes.bar.500")
            verify(mockHistogram).update(elapsedTimeInMillis)
          }
        }

        "log not found call metrics" in new Setup {
          running(app) {
            assertThrows[NotFoundException] {
              await {
                service.withResponseTimeLogging("bar") {
                  Future.failed(new NotFoundException("boom"))
                }
              }
            }
            verify(mockRegistry).histogram("responseTimes.bar.404")
            verify(mockHistogram).update(elapsedTimeInMillis)
          }
        }

        "log bad request error call metrics" in new Setup {
          running(app) {
            assertThrows[BadRequestException] {
              await {
                service.withResponseTimeLogging("bar") {
                  Future.failed(new BadRequestException("boom"))
                }
              }
            }
            verify(mockRegistry).histogram("responseTimes.bar.400")
            verify(mockHistogram).update(elapsedTimeInMillis)
          }
        }

        "log 5xx error call metrics" in new Setup {
          running(app) {
            assertThrows[UpstreamErrorResponse] {
              await {
                service.withResponseTimeLogging("bar") {
                  Future.failed(UpstreamErrorResponse("boom", Status.SERVICE_UNAVAILABLE, Status.NOT_IMPLEMENTED))
                }
              }
            }
            verify(mockRegistry).histogram("responseTimes.bar.503")
            verify(mockHistogram).update(elapsedTimeInMillis)
          }
        }

        "log 4xx error call metrics" in new Setup {
          running(app) {
            assertThrows[UpstreamErrorResponse] {
              await {
                service.withResponseTimeLogging("bar") {
                  Future.failed(UpstreamErrorResponse("boom", Status.FORBIDDEN, Status.NOT_IMPLEMENTED))
                }
              }
            }
            verify(mockRegistry).histogram("responseTimes.bar.403")
            verify(mockHistogram).update(elapsedTimeInMillis)
          }
        }

    }
  }

  trait Setup {
    val startTimestamp: Long = OffsetDateTime.parse("2018-11-09T17:15:30+01:00").toInstant.toEpochMilli
    val endTimestamp: Long = OffsetDateTime.parse("2018-11-09T17:15:35+01:00").toInstant.toEpochMilli
    val elapsedTimeInMillis = 5000L

    val mockDateTimeService: DateTimeService = mock[DateTimeService]
    val mockHistogram: Histogram = mock[Histogram]
    val mockRegistry: MetricRegistry = mock[MetricRegistry]
    val mockMetrics: Metrics = mock[Metrics]

    when(mockDateTimeService.timeStamp())
      .thenReturn(startTimestamp, endTimestamp)

    when(mockRegistry.histogram(any)).thenReturn(mockHistogram)
    when(mockMetrics.defaultRegistry).thenReturn(mockRegistry)

    val app: Application = GuiceApplicationBuilder().overrides(
      inject.bind[DateTimeService].toInstance(mockDateTimeService),
      inject.bind[Histogram].toInstance(mockHistogram),
      inject.bind[Metrics].toInstance(mockMetrics)
    ).configure(
      "microservice.metrics.enabled" -> false,
      "metrics.enabled" -> false,
      "auditing.enabled" -> false
    ).build()

    val service: MetricsReporterService = app.injector.instanceOf[MetricsReporterService]
  }
}
