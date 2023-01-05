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
import domain._
import domain.tpi01.{GetPostClearanceCasesRequest, Request, Response}
import javax.inject.Inject
import models.EORI
import services.DateTimeService
import uk.gov.hmrc.http.HttpReads.Implicits._
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient}

import scala.concurrent.{ExecutionContext, Future}

class Tpi01Connector @Inject()(httpClient: HttpClient,
                               appConfig: AppConfig,
                               dateTimeService: DateTimeService,
                               mdgHeaders: MdgHeaders)(implicit executionContext: ExecutionContext) {

  def retrievePostClearanceCases(eori: EORI, appType: String): Future[Response] = {

    val commonRequest = tpi01.RequestCommon(
      receiptDate = dateTimeService.currentDateTimeAsIso8601,
      acknowledgementReference = mdgHeaders.acknowledgementReference,
      originatingSystem = "MDTP"
    )

    val request = Request(GetPostClearanceCasesRequest(
      commonRequest,
      tpi01.RequestDetail(eori, appType)
    ))

    httpClient.POST[Request, Response](
      appConfig.tpi01GetReimbursementClaimsEndpoint,
      request,
      headers = mdgHeaders.headers(appConfig.tpi01BearerToken, appConfig.tpi01HostHeader)
    )(implicitly, implicitly, HeaderCarrier(), implicitly)
  }
}
