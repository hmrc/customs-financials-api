/*
 * Copyright 2021 HM Revenue & Customs
 *
 */

package domain

import models.{EORI, FileRole}

import java.time.LocalDate

case class Notification (
                          eori: EORI,
                          fileRole: FileRole,
                          fileName: String,
                          fileSize: Long,
                          created: Option[LocalDate],
                          metadata: Map[String, String]) {


  val metadataObfuscated = metadata.map { case (k,v) => if(k.toUpperCase == "DAN") (k,"xxxxxx") else (k,v) }
  override def toString: String = s"Notification(xxxxxxxx, $fileRole, $fileName, $fileSize, $created, $metadataObfuscated"
}

