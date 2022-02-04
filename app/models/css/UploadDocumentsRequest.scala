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

package models.css

import java.util.UUID
import models.EORI
import ru.tinkoff.phobos.derivation.semiauto.deriveElementEncoder
import ru.tinkoff.phobos.encoding.ElementEncoder

case class UploadDocumentsRequest(id: UUID,
                                  eori: EORI,
                                  caseNumber: String,
                                  properties: UploadedFileMetaData)


object UploadDocumentsRequest {
implicit val uploadDocumentsFormat: ElementEncoder[UploadDocumentsRequest] = deriveElementEncoder[UploadDocumentsRequest]
}
