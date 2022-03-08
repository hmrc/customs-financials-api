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
import models.dec64._
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.Helpers.running
import services.dec64.DefaultFileUploadCache
import utils.SpecBase

import java.time.LocalDateTime
import scala.concurrent.ExecutionContext.Implicits.global

class FileUploadCacheSpec extends SpecBase {

  "FileUploadCache" should {

    "enqueue file upload to mongo db returns true" in new Setup {
      running(app) {
        await(for {
          enqueueJob <- cache.enqueueFileUploadJob(uploadedFilesRequest)
        } yield {
          enqueueJob mustBe true
        })
      }
    }

    "enqueue file upload & return next file upload job" in new Setup {
      running(app) {
        await(for {
          enqueueJob <- cache.enqueueFileUploadJob(uploadedFilesRequest)
          nextJob <- cache.nextJob
        } yield {
          enqueueJob mustBe true
          nextJob mustBe Some(uploadedFilesRequest)
        })
      }
    }

    "delete file upload from cache should return true" in new Setup {
      running(app) {
        await(for {
          deleteJob <- cache.deleteJob("id")
        } yield {
          deleteJob mustBe true
        })
      }
    }

    "send all file uploads with processing set to false" in new Setup {

      val fileUploadCache: DefaultFileUploadCache = app.injector.instanceOf[DefaultFileUploadCache]
      val fileUploadMongo: FileUploadMongo = FileUploadMongo(_id = "id_1", uploadDocumentsDetail = uploadedFilesRequest,
        processing = false, receivedAt = LocalDateTime.now)
      val fileUploadMongo2: FileUploadMongo = FileUploadMongo(_id = "id_2", uploadDocumentsDetail = uploadedFilesRequest,
        processing = false, receivedAt = LocalDateTime.now)


      running(app) {
        await(for {
          _ <- fileUploadCache.collection.insertMany(Seq(fileUploadMongo, fileUploadMongo2)).toFuture()
          result1 <- fileUploadCache.nextJob
          result2 <- fileUploadCache.nextJob
          result3 <- fileUploadCache.nextJob
          _ <- fileUploadCache.collection.drop().toFuture()
        } yield {
          result1.nonEmpty mustBe true
          result2.nonEmpty mustBe true
          result3.nonEmpty mustBe false
        })
      }
    }
  }

  trait Setup {
    val app: Application = GuiceApplicationBuilder().overrides().configure(
      "microservice.metrics.enabled" -> false,
      "metrics.enabled" -> false,
      "auditing.enabled" -> false
    ).build()

    val cache: DefaultFileUploadCache = app.injector.instanceOf[DefaultFileUploadCache]
    await(cache.collection.drop().toFuture())

    val uploadedFiles: UploadedFile = UploadedFile(upscanReference = "upscanRef", downloadUrl = "url", uploadTimestamp = "String",
      checksum = "sum", fileName = "filename", fileMimeType = "mimeType", fileSize = 12, "Additional documents")

    val uploadedFilesRequest: FileUploadRequest = FileUploadRequest(id = "id", eori = EORI("eori"), caseNumber = "casenumber",
      applicationName = "appName", declarationId = "MRN", entryNumber = false, uploadedFiles = Seq(uploadedFiles))
  }

}
