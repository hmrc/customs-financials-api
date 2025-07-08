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
import models.requests.{
  CashAccountTransactionSearchRequest, CashAccountTransactionSearchRequestContainer,
  CashAccountTransactionSearchRequestDetails, CashTransactionsRequestCommon
}
import models.responses.ErrorCode.code500
import models.responses.ErrorSource.{backEnd, etmp, mdtp}
import models.responses.SourceFaultDetailMsg.*
import models.responses.{
  CashAccountTransactionSearchResponseContainer, ErrorDetail, ErrorDetailContainer, ErrorSource, SourceFaultDetail
}
import play.api.http.Status.{BAD_REQUEST, CREATED, INTERNAL_SERVER_ERROR, NOT_FOUND, OK}
import play.api.libs.json.*
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

class Acc44Connector @Inject() (
  httpClient: HttpClientV2,
  appConfig: AppConfig,
  dateTimeService: DateTimeService,
  jsonSchemaValidator: JSONSchemaValidator,
  metricsReporterService: MetricsReporterService,
  headers: MdgHeaders
)(implicit ec: ExecutionContext) {

  val log: LoggerLike = Logger(this.getClass)

  def cashAccountTransactionSearch(
    reqDetails: CashAccountTransactionSearchRequestDetails
  ): Future[Either[ErrorDetail, CashAccountTransactionSearchResponseContainer]] = {

    val commonRequest = CashTransactionsRequestCommon(
      originatingSystem = MDTP,
      receiptDate = dateTimeService.currentDateTimeAsIso8601,
      acknowledgementReference = headers.acknowledgementReference
    )

    val cashAccTransSearchRequest = CashAccountTransactionSearchRequest(commonRequest, reqDetails)

    val cashAccTransSearchRequestContainer: CashAccountTransactionSearchRequestContainer =
      CashAccountTransactionSearchRequestContainer(cashAccTransSearchRequest)

    jsonSchemaValidator.validatePayload(
      Json.toJson(cashAccTransSearchRequestContainer),
      jsonSchemaValidator.acc44RequestSchema
    ) match {

      case Success(_) => postValidRequest(cashAccTransSearchRequestContainer)

      case Failure(exception) =>
        log.error(s"Request validation failed against the schema and error is ::::: ${exception.getMessage}")
        Future(
          Left(
            ErrorDetail(
              dateTimeService.currentDateTimeAsIso8601,
              "MDTP_ID",
              BAD_REQUEST.toString,
              exception.getMessage,
              mdtp,
              SourceFaultDetail(Seq(REQUEST_SCHEMA_VALIDATION_ERROR))
            )
          )
        )
    }
  }

  private def postValidRequest(
    cashAccTransSearchRequestContainer: CashAccountTransactionSearchRequestContainer
  ): Future[Either[ErrorDetail, CashAccountTransactionSearchResponseContainer]] =
    metricsReporterService.withResponseTimeLogging("hods.post.cash-account-transaction-search") {
      httpClient
        .post(url"${appConfig.acc44CashTransactionSearchEndpoint}")(HeaderCarrier())
        .withBody[CashAccountTransactionSearchRequestContainer](cashAccTransSearchRequestContainer)
        .setHeader(headers.headers(appConfig.acc44BearerToken, None): _*)
        .execute[HttpResponse]
        .map { res =>
          res.status match {
            case OK => validateAndProcessIncomingSuccessResponse(res)

            case CREATED =>
              if (isResponseContainsErrorDetails(res)) {
                Left(retrieveErrorDetailsResponse(res))
              } else {
                Right(retrieveCashAccountTransactionSsearchResponse(res))
              }

            case resStatus =>
              if (isResponseContainsErrorDetails(res)) {
                Left(retrieveErrorDetailsResponse(res))
              } else {
                Left(
                  populateErrorDetails(
                    res.status.toString,
                    if (resStatus == NOT_FOUND) BACK_END_FAILURE else SERVER_CONNECTION_ERROR,
                    backEnd,
                    SourceFaultDetail(Seq(BACK_END_FAILURE))
                  )
                )
              }
          }
        }
        .recover { case exception: Throwable =>
          log.error("Error occurred while calling backend System")

          Left(
            populateErrorDetails(code500, exception.getMessage, etmp, SourceFaultDetail(Seq(ETMP_FAILURE)))
          )
        }
    }

  private def validateAndProcessIncomingSuccessResponse(
    res: HttpResponse
  ): Either[ErrorDetail, CashAccountTransactionSearchResponseContainer] =
    jsonSchemaValidator.validatePayload(res.json, jsonSchemaValidator.acc44ResponseSchema) match {
      case Success(_) =>
        if (isResponseContainsErrorDetails(res)) {
          Left(retrieveErrorDetailsResponse(res))
        } else {
          Right(retrieveCashAccountTransactionSsearchResponse(res))
        }

      case Failure(exception) =>
        log.error(s"Response is failed against schema and error is :::: ${exception.getMessage}")

        Left(
          ErrorDetail(
            dateTimeService.currentDateTimeAsIso8601,
            "MDTP_ID",
            code500,
            exception.getMessage,
            mdtp,
            SourceFaultDetail(Seq(SUCCESS_RESPONSE_SCHEMA_VALIDATION_ERROR))
          )
        )
    }

  private def retrieveCashAccountTransactionSsearchResponse(
    res: HttpResponse
  ): CashAccountTransactionSearchResponseContainer =
    Json.fromJson[CashAccountTransactionSearchResponseContainer](res.json).get

  private def isResponseContainsErrorDetails(res: HttpResponse) =
    if (res.body.isEmpty) false else Json.fromJson[ErrorDetailContainer](res.json).isSuccess

  private def retrieveErrorDetailsResponse(res: HttpResponse): ErrorDetail =
    Json.fromJson[ErrorDetailContainer](res.json).get.errorDetail

  private def populateErrorDetails(
    errorCode: String,
    errorMessage: String,
    source: String,
    sourceFaultDetail: SourceFaultDetail
  ) =
    ErrorDetail(
      dateTimeService.currentDateTimeAsIso8601,
      "MDTP_ID",
      errorCode,
      errorMessage,
      source,
      sourceFaultDetail
    )
}
