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
import domain.acc37.{AccountDetails, AmendCorrespondenceAddressRequest}
import models.{AccountNumber, AccountType, EORI}
import play.api.libs.ws.writeableOf_JsValue
import services.DateTimeService
import uk.gov.hmrc.http.HttpReads.Implicits.*
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, StringContextOps}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class Acc37Connector @Inject() (
  httpClient: HttpClientV2,
  appConfig: AppConfig,
  dateTimeService: DateTimeService,
  headers: MdgHeaders
)(implicit executionContext: ExecutionContext) {

  def updateAccountContactDetails(
    dan: AccountNumber,
    eori: EORI,
    contactInformation: domain.acc37.ContactDetails
  ): Future[domain.acc37.Response] = {

    val request = domain.acc37.Request(
      AmendCorrespondenceAddressRequest(
        domain.acc37.RequestCommon(DIGITAL, dateTimeService.currentDateTimeAsIso8601, headers.acknowledgementReference),
        domain.acc37.RequestDetail(eori, AccountDetails(AccountType("DutyDeferment"), dan), contactInformation, None)
      )
    )

    httpClient
      .post(url"${appConfig.acc37UpdateAccountContactDetailsEndpoint}")(HeaderCarrier())
      .withBody[domain.acc37.Request](request)
      .setHeader(headers.headers(appConfig.acc37BearerToken, appConfig.acc37HostHeader): _*)
      .execute[domain.acc37.Response]
  }
}
