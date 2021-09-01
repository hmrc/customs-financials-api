/*
 * Copyright 2021 HM Revenue & Customs
 *
 */

package connectors

import play.api.{Logger, LoggerLike}
import services.DateTimeService
import uk.gov.hmrc.http.RequestId

import java.time.format.DateTimeFormatter
import java.util.UUID
import javax.inject.Inject

class MdgHeaders @Inject()(dateTimeService: DateTimeService) {
  val log: LoggerLike = Logger(this.getClass)

  private val MDG_MAX_CORRELATION_ID_LENGTH = 36
  private val MDG_MAX_ACKNOWLEDGEMENT_REFERENCE_LENGTH = 32

  def acknowledgementReference(requestId: Option[RequestId]): String =
    mdgCompliantCorrelationId(requestId).replace("-", "")
      .takeRight(MDG_MAX_ACKNOWLEDGEMENT_REFERENCE_LENGTH)

  private def mdgCompliantCorrelationId(requestId: Option[RequestId]): String =
    requestId.map(_.value.takeRight(MDG_MAX_CORRELATION_ID_LENGTH))
      .getOrElse(UUID.randomUUID().toString)


  private val httpDateFormatter = DateTimeFormatter.ofPattern("EEE, dd MMM yyyy HH:mm:ss 'GMT'")

  private def currentDateTimeAsRFC7231: String = {
    httpDateFormatter.format(dateTimeService.now())
  }

  def headers(authorization: String, requestId: Option[RequestId], maybeHostHeader: Option[String]): Seq[(String, String)] = {

    val mandatoryHeaders = Seq(
      "X-Forwarded-Host" -> "MDTP",
      "Authorization" -> s"Bearer $authorization",
      "Content-Type" -> "application/json",
      "Accept" -> "application/json",
      "Date" -> currentDateTimeAsRFC7231,
      "X-Correlation-ID" -> mdgCompliantCorrelationId(requestId)
    )

    log.info(s"Mandatory MDG headers: $mandatoryHeaders")

    maybeHostHeader match {
      case Some(hostHeader) => {
        log.info(s"Adding optional MDG host header: $hostHeader")
        mandatoryHeaders :+ ("Host" -> hostHeader)
      }
      case _ => mandatoryHeaders
    }
  }
}
