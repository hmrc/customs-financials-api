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
import domain.FileUploadMongo
import models.EORI
import models.css._
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.Helpers.running
import utils.SpecBase
import scala.concurrent.ExecutionContext.Implicits.global

class FileUploadCacheSpec extends SpecBase {

  "FileUploadCache" should {

    "enqueue file upload to mongo db returns true" in new Setup {
      running(app) {
        await(for {
          enqueueJob <- cache.enqueueFileUploadJob(uploadDocumentsRequest)
        } yield {
          enqueueJob mustBe true
        })
      }
    }
    "enqueue file upload & return next file upload job" in new Setup {
      running(app) {
        await(for {
          enqueueJob <- cache.enqueueFileUploadJob(uploadDocumentsRequest)
          nextJob <- cache.nextJob
        } yield {
          enqueueJob mustBe true
          nextJob mustBe Some(uploadDocumentsRequest)
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
  }

  trait Setup {

    val app: Application = GuiceApplicationBuilder().overrides().configure(
      "microservice.metrics.enabled" -> false,
      "metrics.enabled" -> false,
      "auditing.enabled" -> false
    ).build()

    val cache: DefaultFileUploadCache = app.injector.instanceOf[DefaultFileUploadCache]
    await(cache.collection.drop().toFuture())

    val fileUploadMongo = FileUploadMongo("id", UploadedFilesRequest("id", EORI("eori"), "casenumber", "", "",
      UploadedFileMetaData("nonce", Seq(UploadedFiles("upscanRef", "downloadUrl", "uploadTimeStamp",
        "checkSum", "fileName", "fileMimeType", "fileSize", "preiousUrl")))), false, LocalDateTime.now)

    val uploadDocumentsRequest = UploadedFilesRequest("id", EORI("eori"), "casenumber", "", "",
      UploadedFileMetaData("nonce", Seq(UploadedFiles("upscanRef", "downloadUrl", "uploadTimeStamp",
        "checkSum", "fileName", "fileMimeType", "fileSize", "preiousUrl"))))
  }

}
