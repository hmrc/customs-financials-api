package models.css

import org.joda.time.DateTime
import play.api.libs.json.{Format, Json, OFormat}
import uk.gov.hmrc.mongo.play.json.formats.MongoJodaFormats

case class FileUploadCache(ccsSubmissionPayload: CcsSubmissionPayload, receivedAt: DateTime = DateTime.now())

  object FileUploadCache {
    implicit lazy val jodaTimeFormat: Format[DateTime] = MongoJodaFormats.dateTimeFormat
    implicit lazy val format: OFormat[FileUploadCache] = Json.format[FileUploadCache]

}
