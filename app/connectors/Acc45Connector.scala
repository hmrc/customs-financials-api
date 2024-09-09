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
import play.api.http.Status.{BAD_REQUEST, CREATED, INTERNAL_SERVER_ERROR, OK, SERVICE_UNAVAILABLE}
import play.api.{Logger, LoggerLike}
import services.{DateTimeService, MetricsReporterService}
import uk.gov.hmrc.http.HttpReads.Implicits._
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient, HttpResponse}
import play.api.libs.json.{JsValue, Json}
import models.requests._
import models.responses._


import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class Acc45Connector @Inject()(httpClient: HttpClient,
                               appConfig: AppConfig,
                               dateTimeService: DateTimeService,
                               metricsReporterService: MetricsReporterService,
                               mdgHeaders: MdgHeaders)(implicit executionContext: ExecutionContext) {

  val log: LoggerLike = Logger(this.getClass)

  def submitStatementRequest(reqDetail: CashAccountStatementRequestDetail): Future[Either[ErrorDetail,
    Acc45ResponseCommon]] = {

    metricsReporterService.withResponseTimeLogging("hods.post.cash-account-statement-request") {

      val reqCommon = CashAccountStatementRequestCommon(
        originatingSystem = MDTP,
        receiptDate = dateTimeService.currentDateTimeAsIso8601,
        acknowledgementReference = mdgHeaders.acknowledgementReference)

      val cashAccSttReq: CashAccountStatementRequest = CashAccountStatementRequest(reqCommon, reqDetail)
      val cashAccSttReqContainer: CashAccountStatementRequestContainer =
        CashAccountStatementRequestContainer(cashAccSttReq)

      httpClient.POST[CashAccountStatementRequestContainer, HttpResponse](
        appConfig.acc45SubmitCashAccountStatementRequestEndpoint,
        cashAccSttReqContainer,
        mdgHeaders.headers(appConfig.acc45BearerToken, appConfig.acc45HostHeader)
      )(implicitly, implicitly, HeaderCarrier(), implicitly).map { response =>

        log.info(s"submitCashAccountStatementResponse :  $response")
        response.status match {
          case OK | CREATED => Right(handleSuccessCase(response.json))
          case BAD_REQUEST | INTERNAL_SERVER_ERROR => Left(handleErrorCase(response.json))
          case _ => Left(handleUnknownErrorCase(
            SERVICE_UNAVAILABLE.toString,
            response.status.toString(),
            "Failure in backend System"))
        }
      }.recover {
        case exception: Exception => Left(handleUnknownErrorCase(
          SERVICE_UNAVAILABLE.toString,
          exception.toString,
          "Failure in backend System"))
      }
    }
  }

  private def handleErrorCase(jsonObject: JsValue): ErrorDetail = {
    Json.fromJson[CashAccountStatementErrorResponse](jsonObject).get.errorDetail
  }

  private def handleUnknownErrorCase(errorCode: String,
                                     exceptionDetails: String,
                                     sourceFaultDetail: String): ErrorDetail = {

    ErrorDetail(dateTimeService.currentDateTimeAsIso8601, "MDTP_ID", errorCode, exceptionDetails, "MDTP",
      SourceFaultDetail(Seq(sourceFaultDetail)))
  }

  private def handleSuccessCase(jsonObject: JsValue): Acc45ResponseCommon = {
    Json.fromJson[CashAccountStatementResponseContainer](jsonObject).get.cashAccountStatementResponse.responseCommon
  }
}
