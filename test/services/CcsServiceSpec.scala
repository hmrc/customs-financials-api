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

import java.time.LocalDateTime

import connectors.CcsConnector
import domain.FileUploadMongo
import models.EORI
import models.css.{FileUploadRequest, _}
import org.mockito.ArgumentMatchers
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.Helpers.running
import play.api.{Application, inject}
import services.ccs.{CcsService, RequestToDec64Payload}
import utils.SpecBase

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class CcsServiceSpec extends SpecBase {

  "CcsService" should {

    "submit uploaded files to ccs should return true when successful" in new Setup {
      running(app) {
        await(for {
          response <- ccsService.submitFileToCcs(uploadedFilesRequest)
        } yield {
          response mustBe true
        })
      }
    }
  }

  trait Setup {
    val mockCcsConnector: CcsConnector = mock[CcsConnector]
    val mockRequestToDec64Payload: RequestToDec64Payload = mock[RequestToDec64Payload]

    val app: Application = GuiceApplicationBuilder().overrides(
      inject.bind[CcsConnector].toInstance(mockCcsConnector),
      inject.bind[RequestToDec64Payload].toInstance(mockRequestToDec64Payload)
    ).configure(
      "microservice.metrics.enabled" -> false,
      "metrics.enabled" -> false,
      "auditing.enabled" -> false
    ).build()

    val ccsService: CcsService = app.injector.instanceOf[CcsService]

    val uploadedFiles: UploadedFiles = UploadedFiles(upscanReference = "upscanRef", downloadUrl = "url", uploadTimeStamp = "String",
      checkSum = "sum", fileName = "filename", fileMimeType = "mimeType", fileSize = "12" , previousUrl = "url")

    val uploadedFileMetaData: UploadedFileMetaData = UploadedFileMetaData(nonce = "nonce", uploadedFiles = Seq(uploadedFiles))

    val uploadedFilesRequest: FileUploadRequest = FileUploadRequest(id = "id", eori = EORI("eori"), caseNumber = "casenumber",
      applicationName = "appName", documentType = "docType", properties = uploadedFileMetaData)

    val fileUploadMongo: FileUploadMongo = FileUploadMongo(_id = "id", uploadDocumentsRequest = uploadedFilesRequest,
      processing = false, receivedAt = LocalDateTime.now)

    val batchFileInterfaceMetadata: BatchFileInterfaceMetadata = BatchFileInterfaceMetadata(sourceSystem = "TPI", sourceSystemType = "sourceSystemType", interfaceName = "",
      interfaceVersion = "", correlationID = "", batchID = "", batchSize = 75098112, batchCount = 75098112, checksum = "", checksumAlgorithm = "",
      fileSize = 75098112, compressed = true, properties = PropertiesType(List(PropertyType("CaseReference", ""), PropertyType("Eori", ""),
        PropertyType("DeclarationId", "TODO"), PropertyType("DeclarationType", "MRN"),
        PropertyType("ApplicationName", "NDRC"), PropertyType("DocumentType", "TODO"),
        PropertyType("DocumentReceivedDate", "timestamp"))), sourceLocation = "", sourceFileName = "",
      sourceFileMimeType = "", destinations = Destinations(List(Destination(""))))

    when(mockCcsConnector.submitFileUpload(ArgumentMatchers.any())(ArgumentMatchers.any())).thenReturn(Future.successful(true))
    when(mockRequestToDec64Payload.map(ArgumentMatchers.any())).thenReturn(List(batchFileInterfaceMetadata.toString))

  }
}
