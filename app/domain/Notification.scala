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

import java.time.LocalDate

case class Notification(
  eori: EORI,
  fileRole: FileRole,
  fileName: String,
  fileSize: Long,
  created: Option[LocalDate],
  metadata: Map[String, String]
) {

  private val metadataObfuscated = metadata.map { case (k, v) => if (k.toUpperCase == "DAN") (k, "xxxxxx") else (k, v) }

  override def toString: String =
    s"Notification(xxxxxxxx, $fileRole, $fileName, $fileSize, $created, $metadataObfuscated"
}
