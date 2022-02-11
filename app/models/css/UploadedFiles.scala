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

package models.css

import play.api.libs.json.{Json, Format}

case class UploadedFiles(upscanReference: String,
                         downloadUrl: String,
                         uploadTimeStamp: String,
                         checkSum: String,
                         fileName: String,
                         fileMimeType: String,
                         fileSize: String,
                         previousUrl: String)

object UploadedFiles {
  implicit val format: Format[UploadedFiles] = Json.format[UploadedFiles]
}