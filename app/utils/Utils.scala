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

  val iso8601DateTimeRegEx = "[0-9]{4}-[0-9]{2}-[0-9]{2}T[0-9]{2}:[0-9]{2}:[0-9]{2}Z"
  val englishLangKey = "en"
  val welshLangKey = "cy"
  val singleSpace = " "

  def dateTimeAsIso8601(dateTime: LocalDateTime): String =
    s"${DateTimeFormatter.ISO_DATE_TIME.format(dateTime.truncatedTo(ChronoUnit.SECONDS))}Z"

  def isDateTimeStringInIso8601(isoDate: String): Boolean = isoDate.trim.matches(iso8601DateTimeRegEx)

  /**
   * Returns the value with zero padding
   * ex - input - 2  returns 02
   * input  - 10 returns 10
   */
  def zeroPad(value: Int): String = "%02d".format(value)

  /**
   * Returns dateTime string in "Thu, 14 Sep 2023 16:30:30 GMT" format
   */
  def currentDateTimeAsRFC7231(dateTime: LocalDateTime): String = httpDateFormatter.format(dateTime)

  /** Returns base64 encoded string or string on failure */
  def encodeToUTF8Charsets(msg: String): String =
    if (msg.nonEmpty)
      BaseEncoding.base64().encode(msg.trim.getBytes(Charsets.UTF_8))
    else
      msg

  implicit def writable[T](implicit writes: Writes[T]): Writeable[T] = {
    implicit val contentType: ContentTypeOf[T] = ContentTypeOf[T](Some(ContentTypes.JSON))
    Writeable(Writeable.writeableOf_JsValue.transform.compose(writes.writes))
  }

  /**
   * Converts the month integer value to Full month name string
   * ex - input - 01
   *      output - January
   */
  def convertMonthIntegerToFullMonthName(intPaddedValue: String,
                                         lang: String = englishLangKey): String =
    monthValueToNameMap(lang).getOrElse(intPaddedValue, emptyString)

  //TODO: Welsh translation for the month name should be updated once available
  def monthValueToNameMap(lang: String): Map[String, String] =
    Map(
      "01" -> (if (lang == welshLangKey) "January" else "January"),
      "02" -> (if (lang == welshLangKey) "February" else "February"),
      "03" -> (if (lang == welshLangKey) "March" else "March"),
      "04" -> (if (lang == welshLangKey) "April" else "April"),
      "05" -> (if (lang == welshLangKey) "May" else "May"),
      "06" -> (if (lang == welshLangKey) "June" else "June"),
      "07" -> (if (lang == welshLangKey) "July" else "July"),
      "08" -> (if (lang == welshLangKey) "August" else "August"),
      "09" -> (if (lang == welshLangKey) "September" else "September"),
      "10" -> (if (lang == welshLangKey) "October" else "October"),
      "11" -> (if (lang == welshLangKey) "November" else "November"),
      "12" -> (if (lang == welshLangKey) "December" else "December")
    )

}
