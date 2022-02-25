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
          val expectedResult = responseXmlString.toString
          val result = requestToDec64Payload.map(uploadedFilesRequest)
          result contains expectedResult
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

    val uploadedFiles: UploadedFiles = UploadedFiles(upscanReference = "upscanRef", downloadUrl = "url", uploadTimestamp = "timeStamp",
      checksum = "sum", fileName = "filename", fileMimeType = "mimeType", fileSize = 12, description = "Additional documents")


    val uploadedFilesRequest: FileUploadRequest = FileUploadRequest(id = "id", eori = EORI("eori"), caseNumber = "casenumber",
      applicationName = "appName", declarationId = "MRN", entryNumber = false, uploadedFiles = Seq(uploadedFiles))

    val batchFileInterfaceMetadata: BatchFileInterfaceMetadata = BatchFileInterfaceMetadata(sourceSystem = "TPI", sourceSystemType = "AWS",
      interfaceName = "DEC64", interfaceVersion = "1.0.0", correlationID = "correlationID", batchID = "casenumber",
      batchSize = 1, batchCount = 1, checksum = "sum", checksumAlgorithm = "SHA-256", fileSize = 12, compressed = false,
      properties = PropertiesType(List(PropertyType("CaseReference", "casenumber"),
        PropertyType("Eori", "eori"), PropertyType("DeclarationId", "MRNNumer"), PropertyType("DeclarationType", "MRN"),
        PropertyType("ApplicationName", "appName"), PropertyType("DocumentType", "docType"),
        PropertyType("DocumentReceivedDate", "timeStamp"))), sourceLocation = "url", sourceFileName = "filename",
      sourceFileMimeType = "mimeType", destinations = Destinations(List(Destination("CDFPay"))))

    val ccsSubmissionPayload = List(batchFileInterfaceMetadata.toString)

    when(mockUUID.generateUuid).thenReturn("correlationID")

    val responseXmlString =
      <mdg:BatchFileInterfaceMetadata
      xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
      xmlns:mdg="http://www.hmrc.gsi.gov.uk/mdg/batchFileInterfaceMetadataSchema"
      xsi:schemaLocation="http://www.hmrc.gsi.gov.uk/mdg/batchFileInterfaceMetadataSchema BatchFileInterfaceMetadata-1.0.7.xsd">
        <mdg:sourceSystem>TPI</mdg:sourceSystem>
        <mdg:sourceSystemType>AWS</mdg:sourceSystemType>
        <mdg:interfaceName>DEC64</mdg:interfaceName>
        <mdg:interfaceVersion>1.0.0</mdg:interfaceVersion>
        <mdg:correlationID>correlationID</mdg:correlationID>
        <mdg:batchID>casenumber</mdg:batchID>
        <mdg:batchSize>1</mdg:batchSize>
        <mdg:batchCount>1</mdg:batchCount>
        <mdg:extractEndDateTime>timeStamp</mdg:extractEndDateTime>
        <mdg:checksum>sum</mdg:checksum>
        <mdg:checksumAlgorithm>SHA-256</mdg:checksumAlgorithm>
        <mdg:fileSize>12</mdg:fileSize>
        <mdg:compressed>false</mdg:compressed>
        <mdg:encrypted>false</mdg:encrypted>
        <mdg:properties>
          <mdg:property>
            <mdg:name>EORI</mdg:name>
            <mdg:value>eori</mdg:value>
          </mdg:property>
          <mdg:property>
            <mdg:name>ApplicationName</mdg:name>
            <mdg:value>appName</mdg:value>
          </mdg:property>
          <mdg:property>
            <mdg:name>CaseReference</mdg:name>
            <mdg:value>casenumber</mdg:value>
          </mdg:property>
          <mdg:property>
            <mdg:name>DocumentReceivedDate</mdg:name>
            <mdg:value>timeStamp</mdg:value>
          </mdg:property>
          <mdg:property>
            <mdg:name>DeclarationId</mdg:name>
            <mdg:value>MRNNumber</mdg:value>
          </mdg:property>
          <mdg:property>
            <mdg:name>DeclarationType</mdg:name>
            <mdg:value>MRN</mdg:value>
          </mdg:property>
          <mdg:property>
            <mdg:name>DocumentType</mdg:name>
            <mdg:value>docType</mdg:value>
          </mdg:property>
        </mdg:properties>
        <mdg:sourceLocation>url</mdg:sourceLocation>
        <mdg:sourceFileName>filename</mdg:sourceFileName>
        <mdg:sourceFileMimeType>mimeType</mdg:sourceFileMimeType>
        <mdg:destinations>
          <mdg:destination>
            <mdg:destinationSystem>CDFPay</mdg:destinationSystem>
          </mdg:destination>
        </mdg:destinations>
      </mdg:BatchFileInterfaceMetadata>

  }

}
