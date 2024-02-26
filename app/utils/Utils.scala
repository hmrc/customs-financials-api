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

package utils

import play.api.http.{ContentTypeOf, ContentTypes, Writeable}
import play.api.libs.json.Writes
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

import com.google.common.base.Charsets
import com.google.common.io.BaseEncoding

object Utils {
  val emptyString = ""
  val threeColons = ":::"
  val rfc7231DateTimePattern = "EEE, dd MMM yyyy HH:mm:ss 'GMT'"
  val httpDateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern(rfc7231DateTimePattern)
  val iso8601DateFormatter: DateTimeFormatter = DateTimeFormatter.ISO_DATE_TIME

  val iso8601DateTimeRegEx = "[0-9]{4}-[0-9]{2}-[0-9]{2}T[0-9]{2}:[0-9]{2}:[0-9]{2}Z"
  val englishLangKey = "en"
  val welshLangKey = "cy"
  val singleSpace = " "

  def dateTimeAsIso8601(dateTime: LocalDateTime): String =
    s"${DateTimeFormatter.ISO_DATE_TIME.format(dateTime.truncatedTo(ChronoUnit.SECONDS))}Z"

  def isDateTimeStringInIso8601(isoDate: String): Boolean = isoDate.trim.matches(iso8601DateTimeRegEx)

  def zeroPad(value: Int): String = "%02d".format(value)

  def currentDateTimeAsRFC7231(dateTime: LocalDateTime): String = httpDateFormatter.format(dateTime)

  def encodeToUTF8Charsets(msg: String): String =
    if (msg.nonEmpty) {
      BaseEncoding.base64().encode(msg.trim.getBytes(Charsets.UTF_8))
    } else {
      msg
    }

  implicit def writable[T](implicit writes: Writes[T]): Writeable[T] = {
    implicit val contentType: ContentTypeOf[T] = ContentTypeOf[T](Some(ContentTypes.JSON))
    Writeable(Writeable.writeableOf_JsValue.transform.compose(writes.writes))
  }

  def convertMonthValueToFullMonthName(intPaddedValue: String,
                                       lang: String = englishLangKey): String =
    monthValueToNameMap(lang).getOrElse(intPaddedValue, emptyString)

  def monthValueToNameMap(lang: String): Map[String, String] =
    Map(
      "01" -> (if (lang == welshLangKey) "Ionawr" else "January"),
      "02" -> (if (lang == welshLangKey) "Chwefror" else "February"),
      "03" -> (if (lang == welshLangKey) "Mawrth" else "March"),
      "04" -> (if (lang == welshLangKey) "Ebrill" else "April"),
      "05" -> (if (lang == welshLangKey) "Mai" else "May"),
      "06" -> (if (lang == welshLangKey) "Mehefin" else "June"),
      "07" -> (if (lang == welshLangKey) "Gorffennaf" else "July"),
      "08" -> (if (lang == welshLangKey) "Awst" else "August"),
      "09" -> (if (lang == welshLangKey) "Medi" else "September"),
      "10" -> (if (lang == welshLangKey) "Hydref" else "October"),
      "11" -> (if (lang == welshLangKey) "Tachwedd" else "November"),
      "12" -> (if (lang == welshLangKey) "Rhagfyr" else "December")
    )

  def createHyperLink(text: String,
                      link: String,
                      styleClass: String = "govuk-link"): String = {
    val doubleQuotes = "\""

    s"<a class=$doubleQuotes$styleClass$doubleQuotes href=$doubleQuotes$link$doubleQuotes>$text</a>"
  }
}
