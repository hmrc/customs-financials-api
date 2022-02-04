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

import javax.inject.Inject
import models.css.CcsSubmissionPayload
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient, HttpReads, HttpResponse}
import scala.concurrent.{ExecutionContext, Future}

class CcsConnector @Inject()(httpClient: HttpClient)(implicit executionContext: ExecutionContext) {

  private val ccsSubmissionUrl: String = ""


  def postCcsSubmissionPayload(cssSubmissionPayload: CcsSubmissionPayload)(implicit hc: HeaderCarrier): Future[HttpResponse] = {
    httpClient.POSTString[HttpResponse](ccsSubmissionUrl, cssSubmissionPayload.dec64Body,
      cssSubmissionPayload.headers)(HttpReads[HttpResponse], hc, executionContext)
  }
}