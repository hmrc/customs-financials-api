package services

import java.util.UUID

import models.css.{BatchFileInterfaceMetadata, Body, Envelope, PropertiesType, PropertyType, UploadDocumentsRequest}

class RequestToDec64Payload {

  def map(request: UploadDocumentsRequest): List[Envelope] =
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
                PropertyType("DeclarationId", "TODO"),
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
