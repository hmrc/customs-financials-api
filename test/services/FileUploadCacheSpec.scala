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
import domain.FileUploadMongo
import models.EORI
import models.css._
import org.joda.time.DateTime
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.Helpers.running
import utils.SpecBase
import scala.concurrent.ExecutionContext.Implicits.global

class FileUploadCacheSpec extends SpecBase {

  "FileUploadCache" should {

    "set file upload to mongo db" in new Setup {
      running(app) {
        val result = for {
          setResponse <- cache.set(FileUploadMongo(UploadDocumentsRequest(UUID.randomUUID(), EORI("eori"), "casenumber",
            UploadedFileMetaData("nonce", Seq(UploadedFiles("upscanRef", "downloadUrl", "uploadTimeStamp",
              "checkSum", "fileName", "fileMimeType", "fileSize", "preiousUrl")))), DateTime.now))
        } yield setResponse
        await(result) mustBe true
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

    val fileUploadMongo = FileUploadMongo(UploadDocumentsRequest(UUID.randomUUID(), EORI("eori"), "casenumber",
      UploadedFileMetaData("nonce", Seq(UploadedFiles("upscanRef", "downloadUrl", "uploadTimeStamp",
        "checkSum", "fileName", "fileMimeType", "fileSize", "preiousUrl")))), DateTime.now)

//    val receivedAt: DateTime = DateTime.now(DateTimeZone.UTC)
//
//    val uploadDocumentsRequest = UploadDocumentsRequest(UUID.randomUUID(), EORI("eori"), "casenumber",
//      UploadedFileMetaData("nonce", Seq(UploadedFiles("upscanRef", "downloadUrl", "uploadTimeStamp",
//        "checkSum", "fileName", "fileMimeType", "fileSize", "preiousUrl"))))
//
//    val uploadedFile = UploadedFiles("upscanRef", "downloadUrl", "uploadTimeStamp",
//      "checkSum", "fileName", "fileMimeType", "fileSize", "preiousUrl")


    val mapResponse = List(Envelope(Body(BatchFileInterfaceMetadata("TPI","AWS","DEC64", "1.0.0", "5f6c71d4-68de-4a40-816d-20766c291f6d", "casenumber" ,1,1,"checkSum","SHA-256", 12, false,
      PropertiesType(List(PropertyType("CaseReference", "casenumber"), PropertyType("Eori","eori"), PropertyType("DeclarationId", "TODO"),
        PropertyType("DeclarationType", "MRN"), PropertyType("ApplicationName", "NDRC"), PropertyType("DocumentType", "TODO"), PropertyType("DocumentReceivedDate", "uploadTimeStamp"))),
      "downloadUrl", "fileName", "fileMimeType"))))


  }
}
