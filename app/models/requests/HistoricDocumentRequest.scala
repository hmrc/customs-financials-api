/*
 * Copyright 2021 HM Revenue & Customs
 *
 */

package models.requests

import models.{EORI, FileRole}
import play.api.libs.json._

case class HistoricDocumentRequest(
                                    eori: EORI,
                                    documentType: FileRole,
                                    periodStartYear: Int,
                                    periodStartMonth: Int,
                                    periodEndYear: Int,
                                    periodEndMonth: Int,
                                    dan: Option[String]
                                  )

object HistoricDocumentRequest {
  implicit val historicDocumentRequestFormat: OFormat[HistoricDocumentRequest] = Json.format[HistoricDocumentRequest]
}
