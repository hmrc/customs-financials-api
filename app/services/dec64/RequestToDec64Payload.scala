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

package services.dec64

import com.google.inject.Inject
import config.MetaConfig.Dec64
import models.dec64._
import utils.RandomUUIDGenerator

class RequestToDec64Payload @Inject()(uuidGenerator: RandomUUIDGenerator) {

  def map(request: FileUploadDetail): String = {
      val xml =
        <mdg:BatchFileInterfaceMetadata
        xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
        xmlns:mdg="http://www.hmrc.gsi.gov.uk/mdg/batchFileInterfaceMetadataSchema"
        xsi:schemaLocation="http://www.hmrc.gsi.gov.uk/mdg/batchFileInterfaceMetadataSchema BatchFileInterfaceMetadata-1.0.7.xsd">
          <mdg:sourceSystem>{Dec64.SOURCE_SYSTEM}</mdg:sourceSystem>
          <mdg:sourceSystemType>{Dec64.SOURCE_SYSTEM_TYPE}</mdg:sourceSystemType>
          <mdg:interfaceName>{Dec64.INTERFACE_NAME}</mdg:interfaceName>
          <mdg:interfaceVersion>{Dec64.INTERFACE_VERSION}</mdg:interfaceVersion>
          <mdg:correlationID>{uuidGenerator.generateUuid}</mdg:correlationID>
          <mdg:batchID>{request.caseNumber}</mdg:batchID>
          <mdg:batchSize>{request.fileCount}</mdg:batchSize>
          <mdg:batchCount>{request.index.toLong + 1}</mdg:batchCount>
          <mdg:extractEndDateTime>{request.file.uploadTimestamp}</mdg:extractEndDateTime>
          <mdg:checksum>{request.file.checksum}</mdg:checksum>
          <mdg:checksumAlgorithm>{Dec64.UPSCAN_CHECKSUM_ALGORITHM}</mdg:checksumAlgorithm>
          <mdg:fileSize>{request.file.fileSize.toLong}</mdg:fileSize>
          <mdg:compressed>false</mdg:compressed>
          <mdg:encrypted>false</mdg:encrypted>
          <mdg:properties>
            <mdg:property>
              <mdg:name>EORI</mdg:name>
              <mdg:value>{request.eori.value}</mdg:value>
            </mdg:property>
            <mdg:property>
              <mdg:name>ApplicationName</mdg:name>
              <mdg:value>{request.applicationName}</mdg:value>
            </mdg:property>
            <mdg:property>
              <mdg:name>CaseReference</mdg:name>
              <mdg:value>{request.caseNumber}</mdg:value>
            </mdg:property>
            <mdg:property>
              <mdg:name>DocumentReceivedDate</mdg:name>
              <mdg:value>{request.file.uploadTimestamp}</mdg:value>
            </mdg:property>
            <mdg:property>
              <mdg:name>DeclarationId</mdg:name>
              <mdg:value>{request.declarationId}</mdg:value>
            </mdg:property>
            <mdg:property>
              <mdg:name>DeclarationType</mdg:name>
              <mdg:value>{request.declarationType}</mdg:value>
            </mdg:property>
            <mdg:property>
              <mdg:name>DocumentType</mdg:name>
              <mdg:value>{request.file.description}</mdg:value>
            </mdg:property>
          </mdg:properties>
          <mdg:sourceLocation>{request.file.downloadUrl}</mdg:sourceLocation>
          <mdg:sourceFileName>{request.file.fileName}</mdg:sourceFileName>
          <mdg:sourceFileMimeType>{request.file.fileMimeType}</mdg:sourceFileMimeType>
          <mdg:destinations>
            <mdg:destination>
              <mdg:destinationSystem>{Dec64.CDFPay}</mdg:destinationSystem>
            </mdg:destination>
          </mdg:destinations>
        </mdg:BatchFileInterfaceMetadata>

      xml.toString()
    }
}
