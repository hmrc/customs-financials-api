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

import com.google.common.base.Charsets
import com.google.common.io.BaseEncoding
import play.api.http.{ContentTypeOf, ContentTypes, Writeable}
import play.api.libs.json.Writes

import java.time.LocalDateTime
import java.time.format.{DateTimeFormatter, DateTimeFormatterBuilder}
import java.time.temporal.{ChronoField, ChronoUnit}
import java.util.Locale
import scala.jdk.CollectionConverters.*

object Utils {
  val emptyString            = ""
  val threeColons            = ":::"
  val rfc7231DateTimePattern = "EEE, dd MMM yyyy HH:mm:ss 'GMT'"

  val abbreviatedMonth = Map(
    1L  -> "Jan",
    2L  -> "Feb",
    3L  -> "Mar",
    4L  -> "Apr",
    5L  -> "May",
    6L  -> "Jun",
    7L  -> "Jul",
    8L  -> "Aug",
    9L  -> "Sep",
    10L -> "Oct",
    11L -> "Nov",
    12L -> "Dec"
  ).map { case (k, v) => (k: java.lang.Long) -> v }.asJava

  val httpDateFormatter: DateTimeFormatter = new DateTimeFormatterBuilder()
    .appendPattern("EEE, dd ")
    .appendText(ChronoField.MONTH_OF_YEAR, abbreviatedMonth)
    .appendPattern(" yyyy HH:mm:ss 'GMT'")
    .toFormatter(Locale.ENGLISH)

  val iso8601DateFormatter: DateTimeFormatter = DateTimeFormatter.ISO_DATE_TIME

  val iso8601DateTimeRegEx = "[0-9]{4}-[0-9]{2}-[0-9]{2}T[0-9]{2}:[0-9]{2}:[0-9]{2}Z"
  val englishLangKey       = "en"
  val welshLangKey         = "cy"
  val singleSpace          = " "
  val hyphen               = "-"
  val comma                = ","
  val gbEoriPrefix         = "GB"
  val xIEoriPrefix         = "XI"
  val euEoriRegex          = "^[A-Z]{2}[0-9A-Z]{1,15}$".r

  val UTC_TIME_ZONE = "UTC"

  val EXCISE_STT_TYPE: String = "Excise"
  val DD1920_STT_TYPE: String = "DD1920"
  val DD1720_STT_TYPE: String = "DD1720"

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

  def convertMonthValueToFullMonthName(intPaddedValue: String, lang: String = englishLangKey): String =
    monthValueToNameMap(lang).getOrElse(intPaddedValue, emptyString)

  def monthValueToNameMap(lang: String): Map[String, String] =
    Map(
      "01" -> msgForKey(lang, welshStr = "Ionawr", engStr = "January"),
      "02" -> msgForKey(lang, welshStr = "Chwefror", engStr = "February"),
      "03" -> msgForKey(lang, welshStr = "Mawrth", engStr = "March"),
      "04" -> msgForKey(lang, welshStr = "Ebrill", engStr = "April"),
      "05" -> msgForKey(lang, welshStr = "Mai", engStr = "May"),
      "06" -> msgForKey(lang, welshStr = "Mehefin", engStr = "June"),
      "07" -> msgForKey(lang, welshStr = "Gorffennaf", engStr = "July"),
      "08" -> msgForKey(lang, welshStr = "Awst", engStr = "August"),
      "09" -> msgForKey(lang, welshStr = "Medi", engStr = "September"),
      "10" -> msgForKey(lang, welshStr = "Hydref", engStr = "October"),
      "11" -> msgForKey(lang, welshStr = "Tachwedd", engStr = "November"),
      "12" -> msgForKey(lang, welshStr = "Rhagfyr", engStr = "December")
    )

  def createHyperLink(text: String, link: String, styleClass: String = "govuk-link"): String = {
    val doubleQuotes = "\""

    s"<a class=$doubleQuotes$styleClass$doubleQuotes href=$doubleQuotes$link$doubleQuotes>$text</a>"
  }

  private def msgForKey(lang: String, welshStr: String, engStr: String): String =
    if (lang == welshLangKey) welshStr else engStr
}
