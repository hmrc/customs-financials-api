/*
 * Copyright 2021 HM Revenue & Customs
 *
 */

package utils

import play.api.libs.json._

object JsonFormatUtils {
  def stringFormat[A](fromString: String => A)(makeString: A => String): Format[A] = new Format[A] {
    def reads(json: JsValue): JsResult[A] = json match {
      case JsString(str) => JsSuccess(fromString(str))
      case _ => JsError(s"Expected JSON string type")
    }

    def writes(o: A): JsValue = JsString(makeString(o))
  }
}
