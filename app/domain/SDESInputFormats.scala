/*
 * Copyright 2021 HM Revenue & Customs
 *
 */

package domain

import models.{EORI, FileRole}
import play.api.libs.functional.syntax._
import play.api.libs.json.Reads._
import play.api.libs.json._

import java.time.LocalDate

object SDESInputFormats {

  case class InputMetadata(metadata: String, value: String) {
    def toKeyValue: (String, String) = (metadata, value)
  }

  implicit val inputMetadataReader = Json.format[InputMetadata]

  implicit val notificationReads: Reads[Notification] = {
    (
      (JsPath \ 'eori).read[EORI] and
        (JsPath \ 'fileName).read[String] and
        (JsPath \ 'fileSize).read[Long] and
        (JsPath \ 'metadata).read[Seq[InputMetadata]]
      ) { (eoriWithOptionalDan, fileName, fileSize, rawMetadata) =>
      val (metadata, fileRoleSeq) = rawMetadata.partition(_.metadata != "FileRole")
      val fileRole = fileRoleSeq.headOption.map(_.value).getOrElse("UnknownFileRole")
      val metadataToSave = metadata.map(_.toKeyValue).toMap
      val eori = eoriWithOptionalDan.value.split('-').head
      Notification(EORI(eori), FileRole(fileRole), fileName, fileSize, Some(LocalDate.now),metadataToSave) }
  }
}
