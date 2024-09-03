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
import models.requests.{CashAccountTransactionSearchRequest, CashAccountTransactionSearchRequestDetails, CashAccountTransactionSearchRequestWrapper, CashTransactionsRequestCommon}
import models.responses.ErrorCode.code500
import models.responses.{CashAccountTransactionSearchResponseContainer, ErrorDetail, SourceFaultDetail}
import play.api.http.Status.{BAD_REQUEST, CREATED, INTERNAL_SERVER_ERROR, OK}
import play.api.libs.json._
import play.api.{Logger, LoggerLike}
import services.DateTimeService
import uk.gov.hmrc.http.HttpReads.Implicits._
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient, HttpResponse}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class Acc44Connector @Inject()(httpClient: HttpClient,
                               appConfig: AppConfig,
                               dateTimeService: DateTimeService,
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

    httpClient.POST[CashAccountTransactionSearchRequestWrapper, HttpResponse](
      appConfig.acc44CashTransactionSearchEndpoint,
      cashAccTransSearchRequestContainer,
      headers = headers.headers(appConfig.acc44BearerToken, None)
    )(implicitly, implicitly, HeaderCarrier(), implicitly).map { res =>
      res.status match {
        case OK | CREATED => if (Json.fromJson[ErrorDetail](res.json).isSuccess) {
          Left(Json.fromJson[ErrorDetail](res.json).get)
        } else {
          Right(Json.fromJson[CashAccountTransactionSearchResponseContainer](JsString(res.body)).get)
        }

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

}
