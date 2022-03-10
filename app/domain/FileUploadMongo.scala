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

package domain

import java.time.LocalDateTime

import models.dec64.{Dec64SubmissionPayload, FileUploadDetail}
import play.api.libs.json.{Format, Json, OFormat}
import uk.gov.hmrc.mongo.play.json.formats.MongoJavatimeFormats

case class FileUploadMongo(_id: String, processing: Boolean, receivedAt: LocalDateTime, fileUploadDetail: FileUploadDetail, failedSubmission: Int = 0)

object FileUploadMongo {
  implicit val timeFormat: Format[LocalDateTime] = MongoJavatimeFormats.localDateTimeFormat
  implicit val format: OFormat[FileUploadMongo] = Json.format[FileUploadMongo]
  implicit val documentFormat: OFormat[Dec64SubmissionPayload] = Json.format[Dec64SubmissionPayload]
}
