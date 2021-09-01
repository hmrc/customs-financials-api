/*
 * Copyright 2021 HM Revenue & Customs
 *
 */

package models

import play.api.libs.json.Format
import play.api.mvc.PathBindable
import utils.JsonFormatUtils

final case class EORI(value: String)

object EORI {
  implicit val format: Format[EORI] = JsonFormatUtils.stringFormat(EORI.apply)(_.value)

  implicit def pathBinder(implicit stringBinder: PathBindable[String]): PathBindable[EORI] = new PathBindable[EORI] {
    override def bind(key: String, value: String): Either[String, EORI] = {
      stringBinder.bind(key, value).right.map(EORI(_))
    }

    override def unbind(key: String, eori: EORI): String =
      eori.value
  }
}
