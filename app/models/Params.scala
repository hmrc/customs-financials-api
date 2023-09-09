package models

import play.api.libs.json.{Json, OFormat}

case class Params(periodStartMonth: String,
                  periodStartYear: String,
                  periodEndMonth: String,
                  periodEndYear: String,
                  accountType: String,
                  dan: String)

object Params {
  implicit val paramsFormat: OFormat[Params] = Json.format[Params]
}
