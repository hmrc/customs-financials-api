/*
 * Copyright 2021 HM Revenue & Customs
 *
 */

package connectors

import config.AppConfig
import domain.sub09.SubscriptionResponse
import models.EORI
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient, RequestId}
import uk.gov.hmrc.http.HttpReads.Implicits._

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class Sub09Connector @Inject()(httpClient: HttpClient,
                               appConfig: AppConfig,
                               mdgHeaders: MdgHeaders)(implicit executionContext: ExecutionContext) {
  def getSubscriptions(eori: EORI, requestId: Option[RequestId]): Future[SubscriptionResponse] = {

    val url = s"${appConfig.sub09GetSubscriptionsEndpoint}?" +
      s"EORI=${eori.value}&" +
      s"acknowledgementReference=${mdgHeaders.acknowledgementReference(requestId)}&" +
      s"regime=CDS"

    httpClient.GET[SubscriptionResponse](
      url,
      headers = mdgHeaders.headers(appConfig.sub09BearerToken, requestId, appConfig.sub09HostHeader)
    )(implicitly, HeaderCarrier(), implicitly)
  }
}
