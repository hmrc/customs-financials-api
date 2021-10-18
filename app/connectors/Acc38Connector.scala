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
import domain.acc38
import domain.acc38.GetCorrespondenceAddressRequest
import models.{AccountNumber, AccountType, EORI}
import services.DateTimeService
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient, RequestId}
import uk.gov.hmrc.http.HttpReads.Implicits._

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class Acc38Connector @Inject()(httpClient: HttpClient,
                               appConfig: AppConfig,
                               dateTimeService: DateTimeService,
                               mdgHeaders: MdgHeaders)(implicit executionContext: ExecutionContext) {

  def getAccountContactDetails(dan: AccountNumber, eori: EORI, requestId: Option[RequestId]): Future[acc38.Response] = {

    val commonRequest = acc38.RequestCommon(
      receiptDate = dateTimeService.currentDateTimeAsIso8601,
      acknowledgementReference = mdgHeaders.acknowledgementReference,
      originatingSystem = "Digital"
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

    httpClient.POST[acc38.Request, acc38.Response](
      appConfig.acc38DutyDefermentContactDetailsEndpoint,
      request,
      headers = mdgHeaders.headers(appConfig.acc38BearerToken, appConfig.acc38HostHeader)
    )(implicitly, implicitly, HeaderCarrier(), implicitly)
  }

}
