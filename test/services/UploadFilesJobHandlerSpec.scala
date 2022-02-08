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
import java.util.UUID

import akka.http.scaladsl.model.HttpHeader.ParsingResult.Ok
import config.AppConfig
import domain.FileUploadMongo
import models.EORI
import models.css.{UploadedFileMetaData, UploadedFiles, UploadedFilesRequest}
import org.mockito.ArgumentMatchers
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{mock, verify, when}
import play.api.{Application, Configuration}
import play.api.inject.ApplicationLifecycle
import play.api.inject.guice.GuiceApplicationBuilder
import uk.gov.hmrc.mongo.play.PlayMongoComponent
import utils.SpecBase

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class UploadFilesJobHandlerSpec extends SpecBase {

  "FileUploadJobHandlerSpec" should {

    "process job" should {

      "fetch job from email queue" in new Setup {

        when(mockDefaultFileUploadCache.nextJob).thenReturn(Future.successful(Some(fileUploadMongo.uploadDocumentsRequest)))

        await(service.processJob())

        verify(mockDefaultFileUploadCache).nextJob
      }

      "ask email notification service to send email" in new Setup {

        await(service.processJob())

        verify(mockCcsService).submitFileToCcs(ArgumentMatchers.any())
      }

      "ask email queue to delete completed job" in new Setup {

        await(service.processJob())

        verify(mockDefaultFileUploadCache).deleteJob(ArgumentMatchers.any())
      }

      "housekeeping " in {
        val mockCcsService: CcsService = mock[CcsService]
        val mockDefaultFileUploadCache: DefaultFileUploadCache = mock[DefaultFileUploadCache]
        val service = new UploadFilesJobHandler(mockDefaultFileUploadCache, mockCcsService)

        service.houseKeeping()

        verify(mockDefaultFileUploadCache).resetProcessing
      }

//        "integration" in new Setup {
//          val mockConfiguration = mock[Configuration]
//          val mockApplicationLifeCycle = mock[ApplicationLifecycle]
////          when(mockConfiguration.get(ArgumentMatchers.eq("mongodb.uri"))(any)).thenReturn("mongodb://127.0.0.1:27017/test-customs-email-throttler")
//
//          val reactiveMongoComponent: PlayMongoComponent = new PlayMongoComponent(mockConfiguration, lifecycle = mockApplicationLifeCycle)
//
//          val metricsReporter = mock[MetricsReporterService]
//          val mockDateTimeService = mock[DateTimeService]
////          val defaultFileUploadCache = new DefaultFileUploadCache(reactiveMongoComponent, appConfig)
//
//          val fileUploadRequests = Seq(
//            UploadedFilesRequest("id_1", EORI("eori"), "casenumber",
//              UploadedFileMetaData("nonce", Seq(UploadedFiles("upscanRef", "downloadUrl", "uploadTimeStamp",
//                "checkSum", "fileName", "fileMimeType", "fileSize", "preiousUrl")))),
//            UploadedFilesRequest("id_2", EORI("eori"), "casenumber",
//              UploadedFileMetaData("nonce", Seq(UploadedFiles("upscanRef", "downloadUrl", "uploadTimeStamp",
//                "checkSum", "fileName", "fileMimeType", "fileSize", "preiousUrl")))),
//            UploadedFilesRequest("id_3", EORI("eori"), "casenumber",
//              UploadedFileMetaData("nonce", Seq(UploadedFiles("upscanRef", "downloadUrl", "uploadTimeStamp",
//                "checkSum", "fileName", "fileMimeType", "fileSize", "preiousUrl"))))
//          )
//          fileUploadRequests.foreach(request => await(mockDefaultFileUploadCache.enqueueFileUploadJob(request)))
//
//          when(mockCcsService.submitFileToCcs(ArgumentMatchers.any())).thenReturn(Future.successful(true))
//          override val service = new UploadFilesJobHandler(mockDefaultFileUploadCache, mockCcsService)
//
//          await(service.processJob())
//          await(service.processJob())
//
//          reactiveMongoComponent.client.close()
//        }
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
    val service = new UploadFilesJobHandler(mockDefaultFileUploadCache, mockCcsService)

    val uploadDocumentsRequest = UploadedFilesRequest("id", EORI("eori"), "casenumber",
      UploadedFileMetaData("nonce", Seq(UploadedFiles("upscanRef", "downloadUrl", "uploadTimeStamp",
        "checkSum", "fileName", "fileMimeType", "fileSize", "preiousUrl"))))

    val fileUploadMongo: FileUploadMongo = FileUploadMongo(
      UUID.randomUUID().toString,
      UploadedFilesRequest("id", EORI("eori"), "casenumber",
        UploadedFileMetaData("nonce", Seq(UploadedFiles("upscanRef", "downloadUrl", "uploadTimeStamp",
          "checkSum", "fileName", "fileMimeType", "fileSize", "preiousUrl")))),
      processing = true,
      LocalDateTime.of(2019, 10, 8, 15, 1, 0, 0)
    )

    when(mockDefaultFileUploadCache.nextJob).thenReturn(Future.successful(Some(fileUploadMongo.uploadDocumentsRequest)))
    when(mockDefaultFileUploadCache.deleteJob(ArgumentMatchers.any())).thenReturn(Future.successful(true))
    when(mockCcsService.submitFileToCcs(ArgumentMatchers.any())).thenReturn(Future.successful(true))

  }
}
