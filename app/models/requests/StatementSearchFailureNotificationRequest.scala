package models.requests

import models.StatementSearchFailureNotificationMetadata
import play.api.libs.json.{Json, Reads}

case class StatementSearchFailureNotificationRequest(StatementSearchFailureNotificationMetadata: StatementSearchFailureNotificationMetadata)

object StatementSearchFailureNotificationRequest {
  implicit val ssfnRequestReads: Reads[StatementSearchFailureNotificationRequest] =
    Json.reads[StatementSearchFailureNotificationRequest]
}
