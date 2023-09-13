package models

import play.api.libs.json.{Json, OFormat}

case class StatementSearchFailureNotificationMetadata(statementRequestID: String, reason: String)

object StatementSearchFailureNotificationMetadata {
  implicit val ssfnMetaDataFormats: OFormat[StatementSearchFailureNotificationMetadata] =
    Json.format[StatementSearchFailureNotificationMetadata]
}
