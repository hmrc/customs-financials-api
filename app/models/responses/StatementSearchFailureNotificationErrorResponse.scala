package models.responses

import play.api.libs.json.{Json, OWrites}

case class StatementSearchFailureNotificationErrorResponse(errorDetail: ErrorDetail)

object StatementSearchFailureNotificationErrorResponse {
  implicit val ssfnErrorResponseWrites: OWrites[StatementSearchFailureNotificationErrorResponse] =
    Json.writes[StatementSearchFailureNotificationErrorResponse]
}

case class ErrorDetail(timestamp: String,
                       correlationId: String,
                       errorCode: String,
                       errorMessage: String,
                       source: String = "CDS Financials",
                       sourceFaultDetail: SourceFaultDetail)

object ErrorDetail {
  implicit val errorDetailsWrites: OWrites[ErrorDetail] = Json.writes[ErrorDetail]
}

case class SourceFaultDetail(detail: Seq[String])

object SourceFaultDetail {
  implicit val errorDetailsWrites: OWrites[SourceFaultDetail] = Json.writes[SourceFaultDetail]
}
