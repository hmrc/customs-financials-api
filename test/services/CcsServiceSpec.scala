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
import models.css._
import org.mockito.ArgumentMatchers
import play.api.{Application, inject}
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.Helpers.running
import utils.SpecBase

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class CcsServiceSpec extends SpecBase {

  "CcsService" should {

    "submit uploaded files to ccs should return true when successful" in new Setup {
      running(app) {
        await(for {
          response <- ccsService.submitFileToCcs(uploadDocumentsRequest)
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

    val fileUploadMongo = FileUploadMongo("id", UploadedFilesRequest("id", EORI("eori"), "casenumber",
      UploadedFileMetaData("nonce", Seq(UploadedFiles("upscanRef", "downloadUrl", "uploadTimeStamp",
        "checkSum", "fileName", "fileMimeType", "fileSize", "preiousUrl")))), false, LocalDateTime.now)

    val uploadDocumentsRequest = UploadedFilesRequest("id", EORI("eori"), "casenumber",
      UploadedFileMetaData("nonce", Seq(UploadedFiles("upscanRef", "downloadUrl", "uploadTimeStamp",
        "checkSum", "fileName", "fileMimeType", "12", "preiousUrl"))))

    val batchFileInterfaceMetadata = BatchFileInterfaceMetadata("", "", "", "", "", "", 75098112, 75098112, "", "", 75098112, true,
      PropertiesType(List(PropertyType("CaseReference", ""), PropertyType("Eori", ""),
        PropertyType("DeclarationId", "TODO"), PropertyType("DeclarationType", "MRN"),
        PropertyType("ApplicationName", "NDRC"), PropertyType("DocumentType", "TODO"),
        PropertyType("DocumentReceivedDate", "timestamp"))), "", "", "")

    when(mockCcsConnector.submitFileUpload(ArgumentMatchers.any())(ArgumentMatchers.any())).thenReturn(Future.successful(true))
    when(mockRequestToDec64Payload.map(ArgumentMatchers.any())).thenReturn(List(Envelope(Body(batchFileInterfaceMetadata))))

  }
}
