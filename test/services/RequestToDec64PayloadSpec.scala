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
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.Helpers.running
import play.api.{Application, inject}
import services.ccs.RequestToDec64Payload
import uk.gov.hmrc.http.HeaderCarrier
import utils.{RandomUUIDGenerator, SpecBase}

import scala.concurrent.ExecutionContext

class RequestToDec64PayloadSpec extends SpecBase with ScalaFutures {

  "RequestToDec64Payload" when {

    "calling map" should {
      "return submissionPayloadResponse" in new Setup {
        running(app) {
          val result = requestToDec64Payload.map(uploadedFilesRequest)
          result mustBe ccsSubmissionPayload
        }
      }
    }
  }

  trait Setup {
    implicit val hc: HeaderCarrier = HeaderCarrier()
    implicit val executionContext: ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global
    val mockUUID = mock[RandomUUIDGenerator]

    val app: Application = GuiceApplicationBuilder().overrides(
      inject.bind[RandomUUIDGenerator].toInstance(mockUUID),
    ).configure(
      "microservice.metrics.enabled" -> false,
      "metrics.enabled" -> false,
      "auditing.enabled" -> false
    ).build()

    val requestToDec64Payload: RequestToDec64Payload = app.injector.instanceOf[RequestToDec64Payload]

    val uploadedFiles: UploadedFiles = UploadedFiles(upscanReference = "upscanRef", downloadUrl = "url", uploadTimeStamp = "timeStamp",
      checkSum = "sum", fileName = "filename", fileMimeType = "mimeType", fileSize = "12" , previousUrl = "url")

    val uploadedFileMetaData: UploadedFileMetaData = UploadedFileMetaData(nonce = "nonce", uploadedFiles = Seq(uploadedFiles))

    val uploadedFilesRequest: FileUploadRequest = FileUploadRequest(id = "id", eori = EORI("eori"), caseNumber = "casenumber",
      applicationName = "appName", documentType = "docType", properties = uploadedFileMetaData)

    val batchFileInterfaceMetadata: BatchFileInterfaceMetadata = BatchFileInterfaceMetadata(sourceSystem = "TPI", sourceSystemType = "AWS",
      interfaceName = "DEC64", interfaceVersion = "1.0.0", correlationID = "correlationID", batchID = "casenumber",
      batchSize = 1, batchCount = 1, checksum = "sum", checksumAlgorithm = "SHA-256", fileSize = 12, compressed = false,
      properties = PropertiesType(List(PropertyType("CaseReference", "casenumber"),
        PropertyType("Eori", "eori"), PropertyType("DeclarationId", "MRNNumer"), PropertyType("DeclarationType", "MRN"),
        PropertyType("ApplicationName", "appName"), PropertyType("DocumentType", "docType"),
        PropertyType("DocumentReceivedDate", "timeStamp"))), sourceLocation = "url", sourceFileName = "filename",
      sourceFileMimeType = "mimeType", destinations = Destinations(List(Destination("CDFPay"))))

    val ccsSubmissionPayload = List(Envelope(Body(batchFileInterfaceMetadata)))

    when(mockUUID.generateUuid).thenReturn("correlationID")

  }
}
