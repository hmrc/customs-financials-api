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
import domain.secureMessage
import domain.secureMessage.{Request, Response}
import models.HistoricDocumentRequestSearch
import play.api.libs.json.{JsValue, Json}
import play.api.{Logger, LoggerLike}
import services.SubscriptionService
import uk.gov.hmrc.http.HttpReads.Implicits._
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient}
import utils.JSONSchemaValidator

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

class SecureMessageConnector @Inject()(
  httpClient: HttpClient,
  appConfig: AppConfig,
  jsonSchemaValidator: JSONSchemaValidator,
  mdgHeaders: MdgHeaders
)(implicit executionContext: ExecutionContext) {

  def sendSecureMessage(histDoc: HistoricDocumentRequestSearch): Future[secureMessage.Response] = {

    val log: LoggerLike = Logger(this.getClass)
    val request: Request = Request(histDoc)

    jsonSchemaValidator.validatePayload(requestBody(request),
      jsonSchemaValidator.ssfnSecureMessageRequestSchema) match {
      case Success(_) =>
        httpClient.POST[Request, Response](
          appConfig.secureMessageEndpoint,
          request,
          headers = mdgHeaders.headers(
            appConfig.secureMessageBearerToken,
            appConfig.secureMessageHostHeader)
        )(implicitly, implicitly, HeaderCarrier(), implicitly)
      case Failure(_) =>
        log.error(s"Json Schema Failed Validation for SendSecureMessage")
        Future(secureMessage.Response(histDoc.currentEori))
    }
  }

  private def requestBody(request: secureMessage.Request): JsValue = Json.toJson(request)
}
