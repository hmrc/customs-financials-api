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
import domain.secureMessage.{Request, Response}
import models.{EORI, EmailAddress, HistoricDocumentRequestSearch}
import play.api.libs.json.{JsValue, Json}
import play.api.libs.ws.writeableOf_JsValue
import play.api.{Logger, LoggerLike}
import uk.gov.hmrc.http.HttpReads.Implicits.*
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, StringContextOps}
import utils.JSONSchemaValidator
import utils.Utils.emptyString

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

class SecureMessageConnector @Inject() (
  httpClient: HttpClientV2,
  appConfig: AppConfig,
  jsonSchemaValidator: JSONSchemaValidator,
  mdgHeaders: MdgHeaders,
  dataStore: DataStoreConnector
)(implicit executionContext: ExecutionContext) {

  def sendSecureMessage(histDoc: HistoricDocumentRequestSearch): Future[Either[String, Response]] = {

    val log: LoggerLike            = Logger(this.getClass)
    implicit val hc: HeaderCarrier = HeaderCarrier()

    val companyNameResult: Future[Option[String]] =
      dataStore.getCompanyName(EORI(histDoc.currentEori)).recoverWith { case exc: Exception =>
        log.error(s"Company name retrieval failed with error ::${exc.getMessage}")
        Future(Some(emptyString))
      }

    val result = for {
      companyName: Option[String]        <- companyNameResult
      emailAddress: Option[EmailAddress] <- dataStore.getVerifiedEmail(EORI(histDoc.currentEori))
    } yield {

      val request: Request =
        Request(histDoc, emailAddress.getOrElse(EmailAddress(emptyString)), companyName.getOrElse(emptyString))

      jsonSchemaValidator.validatePayload(
        requestBody(request),
        jsonSchemaValidator.ssfnSecureMessageRequestSchema
      ) match {
        case Success(_) =>
          val result: Future[Response] = httpClient
            .post(url"${appConfig.secureMessageEndpoint}")
            .withBody[Request](request)
            .setHeader(mdgHeaders.headers(appConfig.secureMessageBearerToken, appConfig.secureMessageHostHeader): _*)
            .execute[Response]
            .recover { case exception =>
              log.error(exception.getMessage)
              log.error(
                s"error occurred for " +
                  s"message id ${request.externalRef.id} while sending secure message"
              )
              Response(id = s"Secure Message API Error for :::${request.externalRef.id}")
            }

          result.map(res => Right(res))

        case Failure(exception) =>
          log.error(s"Json Schema Failed Validation for sendSecureMessage")
          Future(Left(exception.getMessage))
      }
    }.recoverWith { case exception: Exception =>
      Future(Left(exception.getMessage))
    }
    result.flatten
  }

  private def requestBody(request: Request): JsValue = Json.toJson(request)
}
