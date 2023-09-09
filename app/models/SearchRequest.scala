package models

import play.api.libs.json.{Json, OFormat}
import utils.Utils.emptyString

case class SearchRequest(eoriNumber: String,
                         statementRequestId: String,
                         searchSuccessful: String,
                         searchDateTime: String = emptyString,
                         searchFailureReasonCode: String,
                         failureRetryCount: Int) {
  require(List("yes", "no", "inProcess").contains(searchSuccessful), "invalid value for searchSuccessful")
  require(failureRetryCount >= 0 && failureRetryCount < 6, "invalid value for failureRetryCount")
}

object SearchRequest {
  implicit val searchRequestFormat: OFormat[SearchRequest] = Json.format[SearchRequest]
}
