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

package services.ccs

import com.google.inject.Inject
import config.MetaConfig.Dec64
import models.css.Namespaces.mdg
import models.css._
import org.joda.time.DateTime
import ru.tinkoff.phobos.syntax.xmlns
import utils.RandomUUIDGenerator

class RequestToDec64Payload @Inject()(uuidGenerator: RandomUUIDGenerator) {

  def map(request: FileUploadRequest): Seq[String] =
    request.uploadedFiles.zipWithIndex.map { case (uploadedFile, index) =>

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
          <mdg:batchSize>{request.uploadedFiles.length}</mdg:batchSize>
          <mdg:batchCount>{index.toLong + 1}</mdg:batchCount>
          <mdg:extractEndDateTime>{uploadedFile.uploadTimestamp}</mdg:extractEndDateTime>
          <mdg:checksum>{uploadedFile.checksum}</mdg:checksum>
          <mdg:checksumAlgorithm>{Dec64.UPSCAN_CHECKSUM_ALGORITHM}</mdg:checksumAlgorithm>
          <mdg:fileSize>{uploadedFile.fileSize.toLong}</mdg:fileSize>
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
              <mdg:value>{uploadedFile.uploadTimestamp}</mdg:value>
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
              <mdg:value>{"documentType NEEDED"}</mdg:value>
            </mdg:property>
          </mdg:properties>
          <mdg:sourceLocation>{uploadedFile.downloadUrl}</mdg:sourceLocation>
          <mdg:sourceFileName>{uploadedFile.fileName}</mdg:sourceFileName>
          <mdg:sourceFileMimeType>{uploadedFile.fileMimeType}</mdg:sourceFileMimeType>
          <mdg:destinations>
            <mdg:destination>
              <mdg:destinationSystem>{Dec64.CDFPay}</mdg:destinationSystem>
            </mdg:destination>
          </mdg:destinations>
        </mdg:BatchFileInterfaceMetadata>

      xml.toString()
    }
}
