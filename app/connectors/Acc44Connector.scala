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
  CashAccountTransactionSearchRequest, CashAccountTransactionSearchRequestDetails,
  CashAccountTransactionSearchRequestWrapper, CashTransactionsRequestCommon
}
import models.responses.ErrorCode.code500
import models.responses.{CashAccountTransactionSearchResponseContainer, ErrorDetail, SourceFaultDetail}
import play.api.http.Status.{BAD_REQUEST, CREATED, INTERNAL_SERVER_ERROR, OK}
import play.api.libs.json._
import play.api.{Logger, LoggerLike}
import services.DateTimeService
import uk.gov.hmrc.http.HttpReads.Implicits._
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient, HttpResponse}
import utils.JSONSchemaValidator

import javax.inject.Inject
import scala.collection.immutable.Seq
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

class Acc44Connector @Inject()(httpClient: HttpClient,
                               appConfig: AppConfig,
                               dateTimeService: DateTimeService,
                               jsonSchemaValidator: JSONSchemaValidator,
                               headers: MdgHeaders)(implicit ec: ExecutionContext) {

  val log: LoggerLike = Logger(this.getClass)

  def cashAccountTransactionSearch(reqDetails: CashAccountTransactionSearchRequestDetails): Future[Either[ErrorDetail,
    CashAccountTransactionSearchResponseContainer]] = {

    val commonRequest = CashTransactionsRequestCommon(
      originatingSystem = MDTP,
      receiptDate = dateTimeService.currentDateTimeAsIso8601,
      acknowledgementReference = headers.acknowledgementReference)

    val cashAccTransSearchRequest = CashAccountTransactionSearchRequest(commonRequest, reqDetails)

    val cashAccTransSearchRequestContainer: CashAccountTransactionSearchRequestWrapper =
      CashAccountTransactionSearchRequestWrapper(cashAccTransSearchRequest)

    jsonSchemaValidator.validatePayload(
      Json.toJson(cashAccTransSearchRequestContainer), jsonSchemaValidator.acc44RequestSchema) match {

      case Success(_) => postValidRequest(cashAccTransSearchRequestContainer)

      case Failure(exception) =>
        log.error(s"Request validation failed against the schema and error is ::::: ${exception.getMessage}")
        Future(Left(
          ErrorDetail(
            dateTimeService.currentDateTimeAsIso8601,
            "MDTP_ID",
            BAD_REQUEST.toString,
            exception.getMessage,
            "MDTP",
            SourceFaultDetail(Seq("Failure while validating request against schema")))
        ))
    }
  }

  private def postValidRequest(cashAccTransSearchRequestContainer: CashAccountTransactionSearchRequestWrapper):
  Future[Either[ErrorDetail, CashAccountTransactionSearchResponseContainer]] = {

    httpClient.POST[CashAccountTransactionSearchRequestWrapper, HttpResponse](
      appConfig.acc44CashTransactionSearchEndpoint,
      cashAccTransSearchRequestContainer,
      headers = headers.headers(appConfig.acc44BearerToken, None)
    )(implicitly, implicitly, HeaderCarrier(), implicitly).map { res =>

      res.status match {
        case OK | CREATED => validateAndProcessIncomingSuccessResponse(res)

        case BAD_REQUEST | INTERNAL_SERVER_ERROR => if (Json.fromJson[ErrorDetail](res.json).isSuccess) {
          Left(Json.fromJson[ErrorDetail](res.json).get)
        } else {
          Left(ErrorDetail(dateTimeService.currentDateTimeAsIso8601, "MDTP_ID", res.status.toString,
            "Error connecting to the server", "Backend", SourceFaultDetail(Seq("Failure in backend System")))
          )
        }

        case _ => if (Json.fromJson[ErrorDetail](res.json).isSuccess) {
          Left(Json.fromJson[ErrorDetail](res.json).get)
        } else {
          Left(ErrorDetail(dateTimeService.currentDateTimeAsIso8601, "MDTP_ID", res.status.toString,
            "Error connecting to the server", "Backend", SourceFaultDetail(Seq("Failure in backend System")))
          )
        }
      }
    }.recover {
      case exception: Throwable =>
        log.error("Error occurred while calling backend System")

        Left(
          ErrorDetail(dateTimeService.currentDateTimeAsIso8601, "MDTP_ID", code500, exception.getMessage, "ETMP",
            SourceFaultDetail(Seq("Failure while calling ETMP")))
        )
    }
  }

  private def validateAndProcessIncomingSuccessResponse(res: HttpResponse): Either[ErrorDetail,
    CashAccountTransactionSearchResponseContainer] = {

    jsonSchemaValidator.validatePayload(res.json, jsonSchemaValidator.acc44ResponseSchema) match {
      case Success(_) =>
        if (Json.fromJson[ErrorDetail](res.json).isSuccess) {
          Left(Json.fromJson[ErrorDetail](res.json).get)
        } else {
          Right(Json.fromJson[CashAccountTransactionSearchResponseContainer](res.json).get)
        }

      case Failure(exception) =>
        log.error(s"Response is failed against schema and error is :::: ${exception.getMessage}")

        Left(
          ErrorDetail(
            dateTimeService.currentDateTimeAsIso8601,
            "MDTP_ID",
            code500,
            exception.getMessage,
            "MDTP",
            SourceFaultDetail(Seq("Failure while validating response against schema")))
        )
    }
  }
}
