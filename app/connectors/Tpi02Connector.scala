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
import domain.tpi02.{GetSpecificClaimRequest, GetSpecificClaimRequestWrapper, RequestCommon, Response}

import javax.inject.Inject
import services.DateTimeService
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient, HttpResponse}

import scala.concurrent.{ExecutionContext, Future}

class Tpi02Connector @Inject()(httpClient: HttpClient,
                               appConfig: AppConfig,
                               dateTimeService: DateTimeService,
                               mdgHeaders: MdgHeaders)(implicit executionContext: ExecutionContext) {

  def retrieveSpecificClaim(cdfPayService: String,
                            cdfPayCaseNumber: String): Future[Response] = {

    val commonRequest = RequestCommon(
      receiptDate = dateTimeService.currentDateTimeAsIso8601,
      acknowledgementReference = mdgHeaders.acknowledgementReference,
      originatingSystem = "Digital"
    )

    val request = GetSpecificClaimRequestWrapper(
      GetSpecificClaimRequest(
        commonRequest,
        tpi02.RequestDetail(cdfPayService, cdfPayCaseNumber)
      )
    )

    httpClient.POST[GetSpecificClaimRequestWrapper, HttpResponse](
      appConfig.tpi02GetReimbursementClaimsEndpoint,
      request,
      headers = mdgHeaders.headers(appConfig.tpi02BearerToken, appConfig.tpi02HostHeader)
    )(implicitly, implicitly, HeaderCarrier(), implicitly).map { value =>
      println(value.body)
      value.body.asInstanceOf[Response]
    }
  }
}
