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

package services.ccs

import connectors.CcsConnector
import javax.inject.{Inject, Singleton}
import models.css._
import ru.tinkoff.phobos.encoding.XmlEncoder
import uk.gov.hmrc.http.HeaderCarrier
import scala.concurrent.Future

@Singleton
class CcsService @Inject()(cssConnector: CcsConnector,
                           ccsHeaders: CcsHeaders,
                           requestToDec64Payload: RequestToDec64Payload) {

  def submitFileToCcs(request: FileUploadRequest): Future[Boolean] = {
    implicit val hc: HeaderCarrier = HeaderCarrier()
    val ccsSubmissionsPayload = requestToDec64Payload.map(request).map(data =>
      CcsSubmissionPayload(data, ccsHeaders.getHeaders(hc))).head
    cssConnector.submitFileUpload(ccsSubmissionsPayload)
  }
}


