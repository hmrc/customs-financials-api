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

package models.dec64

import models.EORI
import play.api.libs.json.{Format, Json}

case class FileUploadRequest(id: String,
                             eori: EORI,
                             caseNumber: String,
                             declarationId: String,
                             entryNumber: Boolean,
                             applicationName: String,
                             uploadedFiles: Seq[UploadedFile]) {

  def declarationType: String = {
    if (entryNumber) "Entry" else "MRN"
  }

  def toFileUploadDetail(file: UploadedFile, index: Int): FileUploadDetail = {
    FileUploadDetail(id = id,
      eori = eori,
      caseNumber = caseNumber,
      declarationId = declarationId,
      entryNumber = entryNumber,
      applicationName = applicationName,
      declarationType = declarationType,
      fileCount = uploadedFiles.length,
      file = file,
      index = index)
  }
}

object FileUploadRequest {
  implicit val format: Format[FileUploadRequest] = Json.format[FileUploadRequest]
}
