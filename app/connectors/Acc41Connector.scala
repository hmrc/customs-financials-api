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
import config.MetaConfig.Platform.{MDTP, REGIME_CDS}
import domain._
import domain.acc41.StandingAuthoritiesForEORIResponse
import models.EORI
import services.{AuditingService, DateTimeService}
import uk.gov.hmrc.http.HttpReads.Implicits._
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class Acc41Connector @Inject()(httpClient: HttpClient,
                               auditingService: AuditingService,
                               appConfig: AppConfig,
                               dateTimeService: DateTimeService,
                               mdgHeaders: MdgHeaders)(implicit executionContext: ExecutionContext) {

  def initiateAuthoritiesCSV(requestingEori: EORI, alternateEORI: Option[EORI])
                            (implicit hc: HeaderCarrier): Future[Either[Acc41Response, AuthoritiesCsvGenerationResponse]] = {

    val commonRequest = acc41.RequestCommon(
      receiptDate = dateTimeService.currentDateTimeAsIso8601,
      acknowledgementReference = mdgHeaders.acknowledgementReference,
      originatingSystem = MDTP,
      regime = REGIME_CDS
    )

    val requestDetail = alternateEORI match {
      case Some(x) if x.value.nonEmpty => acc41.RequestDetail(requestingEori, alternateEORI)
      case _ => acc41.RequestDetail(requestingEori, None)
    }

    val request = acc41.StandingAuthoritiesForEORIRequest(acc41.Request(
      commonRequest,
      requestDetail
    ))

    val result: Future[StandingAuthoritiesForEORIResponse] =
      httpClient.POST[acc41.StandingAuthoritiesForEORIRequest, acc41.StandingAuthoritiesForEORIResponse](
        appConfig.acc41AuthoritiesCsvGenerationEndpoint,
        request,
        headers = mdgHeaders.headers(appConfig.acc41BearerToken, appConfig.acc41HostHeader)
      )(implicitly, implicitly, HeaderCarrier(), implicitly)

    result.map {
      res =>
        val resDetail = res.standingAuthoritiesForEORIResponse.responseDetail

        if (resDetail.errorMessage.isDefined) {
          Left(Acc41ErrorResponse)
        } else {
          auditingService.auditRequestAuthCSVStatementRequest(
            resDetail, res.standingAuthoritiesForEORIResponse.requestDetail)

          Right(resDetail.toAuthoritiesCsvGeneration)
        }
    }.recover {
      case _ => Left(Acc41ErrorResponse)
    }
  }
}
