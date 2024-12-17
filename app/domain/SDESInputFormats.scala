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

package domain

import models.{EORI, FileRole}
import play.api.libs.functional.syntax.*
import play.api.libs.json.*
import play.api.libs.json.Reads.*

import java.time.LocalDate

object SDESInputFormats {

  case class InputMetadata(metadata: String, value: String) {
    def toKeyValue: (String, String) = (metadata, value)
  }

  implicit val inputMetadataReader: OFormat[InputMetadata] = Json.format[InputMetadata]

  implicit val notificationReads: Reads[Notification] =
    (
      (JsPath \ Symbol("eori")).read[EORI] and
        (JsPath \ Symbol("fileName")).read[String] and
        (JsPath \ Symbol("fileSize")).read[Long] and
        (JsPath \ Symbol("metadata")).read[Seq[InputMetadata]]
    ) { (eoriWithOptionalDan, fileName, fileSize, rawMetadata) =>
      val (metadata, fileRoleSeq) = rawMetadata.partition(_.metadata != "FileRole")
      val fileRole                = fileRoleSeq.headOption.map(_.value).getOrElse("UnknownFileRole")
      val metadataToSave          = metadata.map(_.toKeyValue).toMap
      val eori                    = eoriWithOptionalDan.value.split('-').head

      Notification(EORI(eori), FileRole(fileRole), fileName, fileSize, Some(LocalDate.now), metadataToSave)
    }
}
