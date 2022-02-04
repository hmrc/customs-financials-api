package models.css

import models.css.Namespaces.mdg
import ru.tinkoff.phobos.derivation.semiauto.deriveElementEncoder
import ru.tinkoff.phobos.encoding.ElementEncoder
import ru.tinkoff.phobos.syntax.xmlns

case class PropertyType(@xmlns(mdg) name: String,
                        @xmlns(mdg) value: String)

object PropertyType {
  implicit val propertyTypeEnc: ElementEncoder[PropertyType] = deriveElementEncoder[PropertyType]
}
