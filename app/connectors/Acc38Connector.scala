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
import config.MetaConfig.Platform.DIGITAL
import domain.acc38
import domain.acc38.GetCorrespondenceAddressRequest
import models.{AccountNumber, AccountType, EORI}
import play.api.libs.ws.writeableOf_JsValue
import services.DateTimeService
import uk.gov.hmrc.http.HttpReads.Implicits.*
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, StringContextOps}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class Acc38Connector @Inject()(httpClient: HttpClientV2,
                               appConfig: AppConfig,
                               dateTimeService: DateTimeService,
                               mdgHeaders: MdgHeaders)(implicit executionContext: ExecutionContext) {

  def getAccountContactDetails(dan: AccountNumber, eori: EORI): Future[acc38.Response] = {

    val commonRequest = acc38.RequestCommon(
      receiptDate = dateTimeService.currentDateTimeAsIso8601,
      acknowledgementReference = mdgHeaders.acknowledgementReference,
      originatingSystem = DIGITAL
    )

    val request = acc38.Request(
      GetCorrespondenceAddressRequest(
        commonRequest,
        acc38.RequestDetail(
          eori,
          acc38.AccountDetails(AccountType("DutyDeferment"), dan),
          None
        )
      )
    )

    httpClient.post(
      url"${appConfig.acc38DutyDefermentContactDetailsEndpoint}")(HeaderCarrier())
      .withBody[acc38.Request](request)
      .setHeader(mdgHeaders.headers(appConfig.acc38BearerToken, appConfig.acc38HostHeader): _*)
      .execute[acc38.Response]
  }
}
