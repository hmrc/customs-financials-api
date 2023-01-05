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

import java.util.UUID
import config.AppConfig
import javax.inject.Inject
import models.dec64.{Dec64SubmissionPayload, RFC7231DateTime}
import play.api.http.{ContentTypes, HeaderNames, MimeTypes, Status}
import play.api.mvc.Codec
import play.api.{Logger, LoggerLike}
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient, HttpReads, HttpResponse}
import scala.concurrent.{ExecutionContext, Future}

class Dec64Connector @Inject()(httpClient: HttpClient,
                               config: AppConfig)(implicit executionContext: ExecutionContext) {

  val log: LoggerLike = Logger(this.getClass)
  val xmlHeaders = Seq(
    HeaderNames.DATE -> RFC7231DateTime.now,
    "X-Correlation-ID" -> UUID.randomUUID().toString,
    HeaderNames.X_FORWARDED_HOST -> "MDTP",
    HeaderNames.CONTENT_TYPE -> ContentTypes.withCharset(MimeTypes.XML)(Codec.utf_8),
    HeaderNames.ACCEPT -> MimeTypes.XML,
    HeaderNames.AUTHORIZATION -> config.dec64BearerToken
  )

  def submitFileUpload(payload: Dec64SubmissionPayload)(implicit hc: HeaderCarrier): Future[Boolean] = {
    httpClient.POSTString[HttpResponse](config.dec64FileUploadEndpoint, payload.dec64Body,
      payload.headers ++ xmlHeaders)(HttpReads[HttpResponse], hc, executionContext).collect {
      case response if response.status == Status.NO_CONTENT =>
        log.info(s"[submitFileUpload] Successful request to DEC64 ")
        true
      case response =>
        log.error(s"[submitFileUpload] Failed with status - ${response.status} error - ${response.body}")
        false
    }.recover {
      case ex: Throwable =>
        log.error(s"[submitFileUpload] Received an exception with message - ${ex.getMessage}")
        false
    }
  }
}