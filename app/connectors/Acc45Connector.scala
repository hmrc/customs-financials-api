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
import config.MetaConfig.Platform.MDTP
import models.requests.*
import models.responses.*
import models.responses.SourceFaultDetailMsg.BACK_END_FAILURE
import play.api.http.Status.*
import play.api.libs.json.{JsValue, Json}
import play.api.libs.ws.writeableOf_JsValue
import play.api.{Logger, LoggerLike}
import services.{DateTimeService, MetricsReporterService}
import uk.gov.hmrc.http.HttpReads.Implicits.*
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse, StringContextOps}
import utils.JSONSchemaValidator

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

class Acc45Connector @Inject() (
  httpClient: HttpClientV2,
  appConfig: AppConfig,
  dateTimeService: DateTimeService,
  jsonSchemaValidator: JSONSchemaValidator,
  metricsReporterService: MetricsReporterService,
  mdgHeaders: MdgHeaders
)(implicit executionContext: ExecutionContext) {

  val log: LoggerLike = Logger(this.getClass)

  def submitStatementRequest(
    reqDetail: CashAccountStatementRequestDetail
  ): Future[Either[ErrorDetail, Acc45ResponseCommon]] = {

    val reqCommon = CashAccountStatementRequestCommon(
      originatingSystem = MDTP,
      receiptDate = dateTimeService.currentDateTimeAsIso8601,
      acknowledgementReference = mdgHeaders.acknowledgementReference
    )

    val cashAccSttReq: CashAccountStatementRequest = CashAccountStatementRequest(reqCommon, reqDetail)

    val cashAccSttReqContainer: CashAccountStatementRequestContainer =
      CashAccountStatementRequestContainer(cashAccSttReq)

    jsonSchemaValidator.validatePayload(
      Json.toJson(cashAccSttReqContainer),
      jsonSchemaValidator.acc45RequestSchema
    ) match {

      case Success(_) => postValidRequest(cashAccSttReqContainer)

      case Failure(exception) =>
        log.error(s"Request validation failed against the schema and error is ::::: ${exception.getMessage}")
        Future(Left(handleUnknownErrorCase(BAD_REQUEST.toString, exception.toString, BACK_END_FAILURE)))
    }
  }

  private def postValidRequest(
    cashAccSttRequestContainer: CashAccountStatementRequestContainer
  ): Future[Either[ErrorDetail, Acc45ResponseCommon]] =
    metricsReporterService.withResponseTimeLogging("hods.post.cash-account-statement-request") {
      httpClient
        .post(url"${appConfig.acc45CashAccountStatementRequestEndpoint}")(HeaderCarrier())
        .withBody[CashAccountStatementRequestContainer](cashAccSttRequestContainer)
        .setHeader(mdgHeaders.headers(appConfig.acc45BearerToken, None): _*)
        .execute[HttpResponse]
        .map { response =>

          log.info(s"submitCashAccountStatementResponse :  $response")
          response.status match {
            case OK | CREATED                        => Right(handleSuccessCase(response.json))
            case BAD_REQUEST | INTERNAL_SERVER_ERROR => Left(handleErrorCase(response.json))
            case _                                   =>
              Left(handleUnknownErrorCase(SERVICE_UNAVAILABLE.toString, response.status.toString, BACK_END_FAILURE))
          }
        }
        .recover { case exception: Exception =>
          Left(handleUnknownErrorCase(SERVICE_UNAVAILABLE.toString, exception.toString, BACK_END_FAILURE))
        }
    }

  private def handleErrorCase(jsonObject: JsValue): ErrorDetail =
    Json.fromJson[CashAccountStatementErrorResponse](jsonObject).get.errorDetail

  private def handleUnknownErrorCase(
    errorCode: String,
    exceptionDetails: String,
    sourceFaultDetail: String
  ): ErrorDetail =
    ErrorDetail(
      dateTimeService.currentDateTimeAsIso8601,
      "MDTP_ID",
      errorCode,
      exceptionDetails,
      ErrorSource.mdtp,
      SourceFaultDetail(Seq(sourceFaultDetail))
    )

  private def handleSuccessCase(jsonObject: JsValue): Acc45ResponseCommon =
    Json.fromJson[CashAccountStatementResponseContainer](jsonObject).get.cashAccountStatementResponse.responseCommon
}
