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

package services

import java.util.UUID

import models.css.{BatchFileInterfaceMetadata, Body, Envelope, PropertiesType, PropertyType, UploadedFilesRequest}

class RequestToDec64Payload {

  def map(request: UploadedFilesRequest): List[Envelope] =
    request.properties.uploadedFiles.zipWithIndex.map { case (uploadedFile, index) =>
      Envelope(
        Body(
          BatchFileInterfaceMetadata(
            correlationID = UUID.randomUUID().toString,
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
                PropertyType("DeclarationId", "MrnNUmber"),
                PropertyType("DeclarationType", "MRN"),
                PropertyType("ApplicationName", "NDRC"),
                PropertyType("DocumentType", "TODO"),
                PropertyType("DocumentReceivedDate", uploadedFile.uploadTimeStamp)
              )
            )
          )
        )
      )
    }.toList
}
