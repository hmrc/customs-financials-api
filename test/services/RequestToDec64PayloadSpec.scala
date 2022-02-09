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

import models.EORI
import models.css._
import org.scalatest.concurrent.ScalaFutures
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.Helpers.running
import uk.gov.hmrc.http.HeaderCarrier
import utils.SpecBase
import scala.concurrent.ExecutionContext

class RequestToDec64PayloadSpec extends SpecBase with ScalaFutures {

  "RequestToDec64Payload" when {

    "calling map" should {
      "return submissionPayloadResponse" in new Setup {
        running(app) {
          val result = requestToDec64Payload.map(uploadDocumentsRequest)
          result mustBe result
        }
      }
    }
  }

  trait Setup {
    implicit val hc: HeaderCarrier = HeaderCarrier()
    implicit val executionContext: ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global

    val app: Application = GuiceApplicationBuilder().configure(
      "microservice.metrics.enabled" -> false,
      "metrics.enabled" -> false,
      "auditing.enabled" -> false
    ).build()

    val uploadDocumentsRequest: UploadedFilesRequest = UploadedFilesRequest("id", EORI("eori"), "casenumber", "", "",
      UploadedFileMetaData("nonce", Seq(UploadedFiles("upscanRef", "downloadUrl", "uploadTimeStamp",
        "checkSum", "fileName", "fileMimeType", "12", "preiousUrl"))))

    val requestToDec64Payload: RequestToDec64Payload = app.injector.instanceOf[RequestToDec64Payload]

    val result = List(Envelope(Body(BatchFileInterfaceMetadata("TPI", "AWS", "DEC64", "1.0.0", "7ffec90e-37c9-49db-8472-6bd50e52ecf2",
      "casenumber", 1, 1, "checkSum", "SHA-256", 2, false, PropertiesType(List(PropertyType("CaseReference","casenumber"),
       PropertyType("Eori","eori"), PropertyType("DeclarationId","TODO"), PropertyType("DeclarationType","MRN"),
       PropertyType("ApplicationName","NDRC"), PropertyType("DocumentType","TODO"), PropertyType("DocumentReceivedDate","uploadTimeStamp"))),
      "downloadUrl","fileName","fileMimeType"))))
  }
}
