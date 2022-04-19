/*
 * Copyright 2022 HM Revenue & Customs
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

import play.api.{Logger, LoggerLike}
import services.DateTimeService

import java.time.format.DateTimeFormatter
import java.util.UUID
import javax.inject.Inject

class MdgHeaders @Inject()(dateTimeService: DateTimeService) {
  val log: LoggerLike = Logger(this.getClass)

  private val MDG_MAX_CORRELATION_ID_LENGTH = 36
  private val MDG_MAX_ACKNOWLEDGEMENT_REFERENCE_LENGTH = 32

  def acknowledgementReference: String =
    UUID.randomUUID().toString.replace("-", "")
      .takeRight(MDG_MAX_ACKNOWLEDGEMENT_REFERENCE_LENGTH)

  private def mdgCompliantCorrelationId: String =
    UUID.randomUUID().toString.replace("-", "")
      .takeRight(MDG_MAX_CORRELATION_ID_LENGTH)


  private val httpDateFormatter = DateTimeFormatter.ofPattern("EEE, dd MMM yyyy HH:mm:ss 'GMT'")

  private def currentDateTimeAsRFC7231: String = {
    httpDateFormatter.format(dateTimeService.now())
  }

  def headers(authorization: String, maybeHostHeader: Option[String]): Seq[(String, String)] = {

    val mandatoryHeaders = Seq(
      "X-Forwarded-Host" -> "MDTP",
      "Authorization" -> s"Bearer $authorization",
      "Content-Type" -> "application/json",
      "Accept" -> "application/json",
      "Date" -> currentDateTimeAsRFC7231,
      "X-Correlation-ID" -> mdgCompliantCorrelationId
    )

    maybeHostHeader match {
      case Some(hostHeader) => {
        mandatoryHeaders :+ ("Host" -> hostHeader)
      }
      case _ => mandatoryHeaders
    }
  }
}
