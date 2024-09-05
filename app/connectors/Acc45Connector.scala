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
import models.responses.{CashAccountStatementErrorResponse, CashAccountStatementResponseContainer, ErrorDetail}
import play.api.http.Status.{BAD_REQUEST, CREATED, INTERNAL_SERVER_ERROR, OK}
import play.api.{Logger, LoggerLike}
import services.{DateTimeService, MetricsReporterService}
import uk.gov.hmrc.http.HttpReads.Implicits._
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient, HttpResponse}
import play.api.libs.json.{JsValue, Json}
import models.requests.{
  CashAccountStatementRequest,
  CashAccountStatementRequestCommon,
  CashAccountStatementRequestDetail,
  CashAccountStatementRequestContainer
}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class Acc45Connector @Inject()(httpClient: HttpClient,
                               appConfig: AppConfig,
                               dateTimeService: DateTimeService,
                               metricsReporterService: MetricsReporterService,
                               mdgHeaders: MdgHeaders)(implicit executionContext: ExecutionContext) {

  val log: LoggerLike = Logger(this.getClass)


  def submitStatementRequest(reqDetail: CashAccountStatementRequestDetail
                            ): Future[Either[ErrorDetail, CashAccountStatementResponseContainer]] = {

    metricsReporterService.withResponseTimeLogging("hods.post.cash-account-statement-request") {

      val reqCommon = CashAccountStatementRequestCommon(
        originatingSystem = MDTP,
        receiptDate = dateTimeService.currentDateTimeAsIso8601,
        acknowledgementReference = mdgHeaders.acknowledgementReference)

      val request: CashAccountStatementRequest = CashAccountStatementRequest(reqCommon, reqDetail)
      val reqWrapper: CashAccountStatementRequestContainer = CashAccountStatementRequestContainer(request)

      httpClient.POST[CashAccountStatementRequestContainer, HttpResponse](
        appConfig.acc54SubmitCashAccountStatementRequestEndpoint,
        reqWrapper,
        mdgHeaders.headers(appConfig.acc45BearerToken, appConfig.acc45HostHeader)
      )(implicitly, implicitly, HeaderCarrier(), implicitly).map { response =>
        log.info(s"submitCashAccountStatementResponse :  $response")
        response.status match {
          case OK | CREATED => Right(handleSuccessCase(response.json))
          case BAD_REQUEST => Left(handleErrorCase(response.json))
          case INTERNAL_SERVER_ERROR => Left(handleErrorCase(response.json))
          case _ => Left(handleErrorCase(response.json))
        }
      }
    }
  }

  private def handleErrorCase(jsonObject: JsValue): ErrorDetail = {
    Json.fromJson[CashAccountStatementErrorResponse](jsonObject).get.errorDetail
  }

  private def handleUnknownErrorCase(jsonObject: JsValue): ErrorDetail = {
    Json.fromJson[CashAccountStatementErrorResponse](jsonObject).get.errorDetail
  }

  private def handleSuccessCase(jsonObject: JsValue): CashAccountStatementResponseContainer = {
    Json.fromJson[CashAccountStatementResponseContainer](jsonObject).get
  }
}
