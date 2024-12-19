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
import models.EORI
import models.requests.manageAuthorities.*
import play.api.http.Status
import play.api.libs.ws.writeableOf_JsValue
import services.{DateTimeService, MetricsReporterService}
import uk.gov.hmrc.http.HttpReads.Implicits.*
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse, StringContextOps}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class Acc30Connector @Inject() (
  httpClient: HttpClientV2,
  appConfig: AppConfig,
  dateTimeService: DateTimeService,
  metricsReporterService: MetricsReporterService,
  mdgHeaders: MdgHeaders
)(implicit executionContext: ExecutionContext) {

  def grantAccountAuthorities(grantAuthorityRequest: GrantAuthorityRequest, eori: EORI): Future[Boolean] =
    makeRequest(
      ManageStandingAuthoritiesRequestDetail.grantAuthority(grantAuthorityRequest, eori)
    )

  def revokeAccountAuthorities(revokeAuthorityRequest: RevokeAuthorityRequest, eori: EORI): Future[Boolean] =
    makeRequest(
      ManageStandingAuthoritiesRequestDetail.revokeAuthority(revokeAuthorityRequest, eori)
    )

  private def makeRequest(detail: ManageStandingAuthoritiesRequestDetail): Future[Boolean] = {
    val requestCommon = AuthoritiesRequestCommon(
      REGIME_CDS,
      receiptDate = dateTimeService.currentDateTimeAsIso8601,
      acknowledgementReference = mdgHeaders.acknowledgementReference,
      MDTP
    )

    val manageStandingAuthoritiesRequestContainer = ManageStandingAuthoritiesRequestContainer(
      ManageStandingAuthoritiesRequest(requestCommon, detail)
    )

    metricsReporterService.withResponseTimeLogging("hods.post.manage-standing-authority.grant") {
      httpClient
        .post(url"${appConfig.acc30ManageAccountAuthoritiesEndpoint}")(HeaderCarrier())
        .withBody[ManageStandingAuthoritiesRequestContainer](manageStandingAuthoritiesRequestContainer)
        .setHeader(mdgHeaders.headers(appConfig.acc30BearerToken, appConfig.acc30HostHeader): _*)
        .execute[HttpResponse]
        .map {
          _.status match {
            case Status.NO_CONTENT => true
            case _                 => false
          }
        }
        .recover { case _: Throwable => false }
    }
  }
}
