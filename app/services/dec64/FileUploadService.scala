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

package services.dec64

import connectors.Dec64Connector
import javax.inject.{Inject, Singleton}
import models.dec64._
import services.AuditingService
import uk.gov.hmrc.http.HeaderCarrier
import scala.concurrent.Future

@Singleton
class FileUploadService @Inject()(dec64Connector: Dec64Connector,
                                  dec64Headers: Dec64Headers,
                                  requestToDec64Payload: RequestToDec64Payload,
                                  auditingService: AuditingService) {

  def submitFileToDec64(request: FileUploadDetail): Future[Boolean] = {
    implicit val hc: HeaderCarrier = HeaderCarrier()
    auditingService.auditFileUploadDetail(request)
    val dec64xml = requestToDec64Payload.map(request)
    dec64Connector.submitFileUpload(Dec64SubmissionPayload(dec64xml, dec64Headers.getHeaders(hc)))
  }
}


