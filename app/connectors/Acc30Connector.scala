/*
 * Copyright 2021 HM Revenue & Customs
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
import models.EORI
import models.requests.manageAuthorities._
import play.api.http.Status
import services.{DateTimeService, MetricsReporterService}
import uk.gov.hmrc.http.HttpReads.Implicits._
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient, HttpResponse, RequestId}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class Acc30Connector @Inject()(httpClient: HttpClient,
                               appConfig: AppConfig,
                               dateTimeService: DateTimeService,
                               metricsReporterService: MetricsReporterService,
                               mdgHeaders: MdgHeaders)(implicit executionContext: ExecutionContext) {

  def grantAccountAuthorities(grantAuthorityRequest: GrantAuthorityRequest, eori: EORI, requestId: Option[RequestId]): Future[Boolean] = {
    makeRequest(
      ManageStandingAuthoritiesRequestDetail.grantAuthority(grantAuthorityRequest, eori),
      requestId
    )
  }

  def revokeAccountAuthorities(revokeAuthorityRequest: RevokeAuthorityRequest, eori: EORI, requestId: Option[RequestId]): Future[Boolean] = {
    makeRequest(
      ManageStandingAuthoritiesRequestDetail.revokeAuthority(revokeAuthorityRequest, eori),
      requestId
    )
  }

  private def makeRequest(detail: ManageStandingAuthoritiesRequestDetail, requestId: Option[RequestId]): Future[Boolean] = {
    val requestCommon = AuthoritiesRequestCommon(
      "CDS",
      receiptDate = dateTimeService.currentDateTimeAsIso8601,
      acknowledgementReference = mdgHeaders.acknowledgementReference(requestId),
      "MDTP"
    )

    val manageStandingAuthoritiesRequestContainer = ManageStandingAuthoritiesRequestContainer(
      ManageStandingAuthoritiesRequest(requestCommon, detail)
    )

    metricsReporterService.withResponseTimeLogging("hods.post.manage-standing-authority.grant") {
      httpClient.POST[ManageStandingAuthoritiesRequestContainer, HttpResponse](
        appConfig.acc30ManageAccountAuthoritiesEndpoint,
        manageStandingAuthoritiesRequestContainer,
        headers = mdgHeaders.headers(appConfig.acc30BearerToken, requestId, appConfig.acc30HostHeader)
      )(implicitly, implicitly, HeaderCarrier(), implicitly).map {
        _.status match {
          case Status.NO_CONTENT => true
          case _ => false
        }
      }.recover { case ex: Throwable => false }
    }
  }
}
