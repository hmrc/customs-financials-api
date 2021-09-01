/*
 * Copyright 2021 HM Revenue & Customs
 *
 */

package connectors

import config.AppConfig
import domain.AccountWithAuthorities
import models.EORI
import models.requests.manageAuthorities.{AuthoritiesRequestCommon, AuthoritiesRequestDetail, StandingAuthoritiesRequest}
import models.responses.StandingAuthoritiesResponse
import services.{DateTimeService, MetricsReporterService}
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient, RequestId}
import uk.gov.hmrc.http.HttpReads.Implicits._

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class Acc29Connector @Inject()(httpClient: HttpClient,
                               appConfig: AppConfig,
                               dateTimeService: DateTimeService,
                               metricsReporterService: MetricsReporterService,
                               mdgHeaders: MdgHeaders)(implicit executionContext: ExecutionContext) {

  def getStandingAuthorities(eori: EORI, requestId: Option[RequestId]): Future[Seq[AccountWithAuthorities]] = {
    val commonRequest = AuthoritiesRequestCommon(
      "CDS",
      receiptDate = dateTimeService.currentDateTimeAsIso8601,
      acknowledgementReference = mdgHeaders.acknowledgementReference(requestId),
      "MDTP"
    )

    val standingAuthoritiesRequest = StandingAuthoritiesRequest(commonRequest, AuthoritiesRequestDetail(ownerEori = eori))
    metricsReporterService.withResponseTimeLogging("hods.post.get-standing-authority-details") {
      httpClient.POST[StandingAuthoritiesRequest, StandingAuthoritiesResponse](
        appConfig.acc29GetStandingAuthoritiesEndpoint,
        standingAuthoritiesRequest,
        headers = mdgHeaders.headers(appConfig.acc29BearerToken, requestId, appConfig.acc29HostHeader)
      )(implicitly, implicitly, HeaderCarrier(), implicitly).map(_.accounts)
    }
  }
}
