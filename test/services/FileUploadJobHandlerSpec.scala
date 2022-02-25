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

import domain.FileUploadMongo
import models.EORI
import models.css.{FileUploadRequest, UploadedFiles}
import org.mockito.ArgumentMatchers
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.Helpers.running
import services.ccs.{CcsService, DefaultFileUploadCache, FileUploadJobHandler}
import utils.SpecBase

import java.time.LocalDateTime
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class FileUploadJobHandlerSpec extends SpecBase {

  "FileUploadJobHandlerSpec" should {

    "process job" should {

      "fetch job from FileUploadJob" in new Setup {
        running(app) {

          when(mockDefaultFileUploadCache.nextJob).thenReturn(Future.successful(Some(fileUploadMongo.uploadDocumentsRequest)))

          await(handler.processJob())

          verify(mockDefaultFileUploadCache).nextJob
        }
      }

      "submit file upload to CCS" in new Setup {
        running(app) {

          await(handler.processJob())

          verify(mockCcsService).submitFileToCcs(ArgumentMatchers.any())
        }
      }

      "delete completed job if submission was successful" in new Setup {
        running(app) {

          await(handler.processJob())

          verify(mockDefaultFileUploadCache).deleteJob(ArgumentMatchers.any())
        }
      }

      "delete not completed job when submission to ccs was unsuccessful" in  {

        val app: Application = GuiceApplicationBuilder().overrides().configure(
          "microservice.metrics.enabled" -> false,
          "metrics.enabled" -> false,
          "auditing.enabled" -> false
        ).build()

        val mockCcsService: CcsService = mock[CcsService]
        val mockDefaultFileUploadCache: DefaultFileUploadCache = mock[DefaultFileUploadCache]
        val handler = new FileUploadJobHandler(mockDefaultFileUploadCache, mockCcsService)

        val uploadedFiles: UploadedFiles = UploadedFiles(upscanReference = "upscanRef", downloadUrl = "url", uploadTimestamp = "String",
          checksum = "sum", fileName = "filename", fileMimeType = "mimeType", fileSize = 12, description = "Additional documents")


        val uploadedFilesRequest: FileUploadRequest = FileUploadRequest(id = "id", eori = EORI("eori"), caseNumber = "casenumber",
          applicationName = "appName", declarationId = "MRN", entryNumber = false, uploadedFiles = Seq(uploadedFiles))

        val fileUploadMongo: FileUploadMongo = FileUploadMongo(_id = "id", uploadDocumentsRequest = uploadedFilesRequest,
          processing = false, receivedAt = LocalDateTime.now)

        when(mockDefaultFileUploadCache.nextJob).thenReturn(Future.successful(Some(fileUploadMongo.uploadDocumentsRequest)))

        when(mockCcsService.submitFileToCcs(ArgumentMatchers.any())).thenReturn(Future.successful(false))

        running(app) {

          await(handler.processJob())

          verify(mockCcsService).submitFileToCcs(ArgumentMatchers.any())

          verifyZeroInteractions(mockDefaultFileUploadCache)
        }

      }

      "housekeeping " in {
        val app: Application = GuiceApplicationBuilder().overrides().configure(
          "microservice.metrics.enabled" -> false,
          "metrics.enabled" -> false,
          "auditing.enabled" -> false
        ).build()

        running(app) {

          val mockCcsService: CcsService = mock[CcsService]
          val mockDefaultFileUploadCache: DefaultFileUploadCache = mock[DefaultFileUploadCache]
          val service = new FileUploadJobHandler(mockDefaultFileUploadCache, mockCcsService)

          service.houseKeeping()

          verify(mockDefaultFileUploadCache).resetProcessing
        }
      }
    }
  }

  trait Setup {

    val app: Application = GuiceApplicationBuilder().overrides().configure(
      "microservice.metrics.enabled" -> false,
      "metrics.enabled" -> false,
      "auditing.enabled" -> false
    ).build()

    val mockCcsService: CcsService = mock[CcsService]
    val mockDefaultFileUploadCache: DefaultFileUploadCache = mock[DefaultFileUploadCache]
    val handler = new FileUploadJobHandler(mockDefaultFileUploadCache, mockCcsService)

    val uploadedFiles: UploadedFiles = UploadedFiles(upscanReference = "upscanRef", downloadUrl = "url", uploadTimestamp = "String",
      checksum = "sum", fileName = "filename", fileMimeType = "mimeType", fileSize = 12, description = "Additional documents")


    val uploadedFilesRequest: FileUploadRequest = FileUploadRequest(id = "id", eori = EORI("eori"), caseNumber = "casenumber",
      applicationName = "appName", declarationId = "MRN", entryNumber = false, uploadedFiles = Seq(uploadedFiles))

    val fileUploadMongo: FileUploadMongo = FileUploadMongo(_id = "id", uploadDocumentsRequest = uploadedFilesRequest,
      processing = false, receivedAt = LocalDateTime.now)

    when(mockDefaultFileUploadCache.nextJob).thenReturn(Future.successful(Some(fileUploadMongo.uploadDocumentsRequest)))
    when(mockDefaultFileUploadCache.deleteJob(ArgumentMatchers.any())).thenReturn(Future.successful(true))
    when(mockCcsService.submitFileToCcs(ArgumentMatchers.any())).thenReturn(Future.successful(true))

  }
}

