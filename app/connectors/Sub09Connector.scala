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
import domain.sub09.SubscriptionResponse
import models.EORI
import uk.gov.hmrc.http.HttpReads.Implicits._
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class Sub09Connector @Inject()(httpClient: HttpClient,
                               appConfig: AppConfig,
                               mdgHeaders: MdgHeaders)(implicit executionContext: ExecutionContext) {
  def getSubscriptions(eori: EORI): Future[SubscriptionResponse] = {

    val url = s"${appConfig.sub09GetSubscriptionsEndpoint}?" +
      s"EORI=${eori.value}&" +
      s"acknowledgementReference=${mdgHeaders.acknowledgementReference}&" +
      s"regime=CDS"

    httpClient.GET[SubscriptionResponse](
      url,
      headers = mdgHeaders.headers(appConfig.sub09BearerToken, appConfig.sub09HostHeader)
    )(implicitly, HeaderCarrier(), implicitly)
  }
}
