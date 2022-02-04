package models.css

import models.css.Namespaces.mdg
import ru.tinkoff.phobos.derivation.semiauto.deriveElementEncoder
import ru.tinkoff.phobos.encoding.ElementEncoder
import ru.tinkoff.phobos.syntax.xmlns

case class PropertiesType(@xmlns(mdg) property: Seq[PropertyType] = Nil)

object PropertiesType {
  implicit val propertiesTypeEnc: ElementEncoder[PropertiesType] = deriveElementEncoder[PropertiesType]
}
