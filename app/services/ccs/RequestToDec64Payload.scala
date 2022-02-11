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

import com.google.inject.Inject
import models.css._
import utils.RandomUUIDGenerator

class RequestToDec64Payload @Inject()(uuidGenerator: RandomUUIDGenerator) {

  def map(request: FileUploadRequest): List[Envelope] =
    request.properties.uploadedFiles.zipWithIndex.map { case (uploadedFile, index) =>
      Envelope(
        Body(
          BatchFileInterfaceMetadata(
            correlationID = uuidGenerator.generateUuid,
            batchID = request.caseNumber,
            batchCount = index.toLong + 1,
            batchSize = request.properties.uploadedFiles.length,
            checksum = uploadedFile.checkSum,
            sourceLocation = uploadedFile.downloadUrl,
            sourceFileName = uploadedFile.fileName,
            sourceFileMimeType = uploadedFile.fileMimeType,
            fileSize = uploadedFile.fileSize.toLong,
            properties = PropertiesType(
              List(
                PropertyType("CaseReference", request.caseNumber),
                PropertyType("Eori", request.eori.value),
                PropertyType("DeclarationId", "MRNNumer"),
                PropertyType("DeclarationType", "MRN"),
                PropertyType("ApplicationName", request.applicationName),
                PropertyType("DocumentType", request.documentType),
                PropertyType("DocumentReceivedDate", uploadedFile.uploadTimeStamp)
              )
            )
          )
        )
      )
    }.toList
}
