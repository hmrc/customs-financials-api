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
import play.api.libs.json.{JsObject, JsValue}
import play.api.libs.ws.writeableOf_JsValue
import uk.gov.hmrc.http.HttpReads.Implicits.*
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, StringContextOps}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class Acc27Connector @Inject() (httpClient: HttpClientV2, appConfig: AppConfig, mdgHeaders: MdgHeaders)(implicit
  executionContext: ExecutionContext
) {

  def getAccounts(requestBody: JsValue): Future[JsObject] =
    httpClient
      .post(url"${appConfig.hodsEndpoint}")(HeaderCarrier())
      .withBody[JsValue](requestBody)
      .setHeader(mdgHeaders.headers(appConfig.bearerToken, None): _*)
      .execute[JsObject]
}
