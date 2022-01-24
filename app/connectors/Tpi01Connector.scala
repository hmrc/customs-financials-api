/*
 * Copyright 2022 HM Revenue & Customs
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
import domain._
import domain.tpi01.{GetReimbursementClaims, GetReimbursementClaimsRequest, GetReimbursementClaimsResponse}
import javax.inject.Inject
import models.EORI
import play.api.libs.json.Json
import services.DateTimeService
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient}

import scala.concurrent.{ExecutionContext, Future}

class Tpi01Connector @Inject()(httpClient: HttpClient,
                               appConfig: AppConfig,
                               dateTimeService: DateTimeService,
                               mdgHeaders: MdgHeaders)(implicit executionContext: ExecutionContext) {

  def retrieveReimbursementClaims(eori: EORI): Future[GetReimbursementClaimsResponse] = {

    val commonRequest = tpi01.RequestCommon(
      receiptDate = dateTimeService.currentDateTimeAsIso8601,
      acknowledgementReference = mdgHeaders.acknowledgementReference,
      originatingSystem = "Digital"
    )

    val request = GetReimbursementClaimsRequest(GetReimbursementClaims(
        commonRequest,
        tpi01.RequestDetail(eori)
    ))

    httpClient.POST[GetReimbursementClaimsRequest, GetReimbursementClaimsResponse](
      appConfig.tpi01GetReimbursementClaimsEndpoint,
      request,
      headers = mdgHeaders.headers(appConfig.tpi01BearerToken, appConfig.tpi01HostHeader)
    )(implicitly, implicitly, HeaderCarrier(), implicitly)
  }
}
