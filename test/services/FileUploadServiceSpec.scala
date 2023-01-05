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

package services

import java.time.LocalDateTime

import connectors.Dec64Connector
import domain.FileUploadMongo
import models.EORI
import models.dec64._
import org.mockito.ArgumentMatchers
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.Helpers.running
import play.api.{Application, inject}
import services.dec64.{FileUploadService, RequestToDec64Payload}
import utils.SpecBase

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class FileUploadServiceSpec extends SpecBase {

  "FileUploadService" should {

    "submit uploaded files to dec64 should return true when successful" in new Setup {
      running(app) {
        await(for {
          response <- fileUploadService.submitFileToDec64(fileUploadDetail)
        } yield {
          response mustBe true
        })
      }
    }
  }

  trait Setup {
    val mockDec64Connector: Dec64Connector = mock[Dec64Connector]
    val mockRequestToDec64Payload: RequestToDec64Payload = mock[RequestToDec64Payload]

    val app: Application = GuiceApplicationBuilder().overrides(
      inject.bind[Dec64Connector].toInstance(mockDec64Connector),
      inject.bind[RequestToDec64Payload].toInstance(mockRequestToDec64Payload)
    ).configure(
      "microservice.metrics.enabled" -> false,
      "metrics.enabled" -> false,
      "auditing.enabled" -> false
    ).build()

    val fileUploadService: FileUploadService = app.injector.instanceOf[FileUploadService]

    val uploadedFiles: UploadedFile = UploadedFile(upscanReference = "upscanRef", downloadUrl = "url", uploadTimestamp = "String",
      checksum = "sum", fileName = "filename", fileMimeType = "mimeType", fileSize = 12, description = "Additional documents")

    val uploadedFilesRequest: FileUploadRequest = FileUploadRequest(id = "id", eori = EORI("eori"), caseNumber = "casenumber",
      applicationName = "appName", declarationId = "MRN", entryNumber = false, uploadedFiles = Seq(uploadedFiles))

    val fileUploadDetail: FileUploadDetail = FileUploadDetail(id = "id", eori = EORI("eori"), caseNumber = "casenumber", declarationId = "MRN",
      entryNumber = false, applicationName = "appName", declarationType = "MRN", fileCount = 0, file = uploadedFiles, index = 0)

    val fileUploadMongo: FileUploadMongo = FileUploadMongo(_id = "id", processing = false, receivedAt = LocalDateTime.now,
      fileUploadDetail)



    when(mockDec64Connector.submitFileUpload(ArgumentMatchers.any())(ArgumentMatchers.any())).thenReturn(Future.successful(true))
    when(mockRequestToDec64Payload.map(ArgumentMatchers.any())).thenReturn("")

  }
}
