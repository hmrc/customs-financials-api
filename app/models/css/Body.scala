package models.css

import models.css.Namespaces.mdg
import ru.tinkoff.phobos.derivation.semiauto.deriveElementEncoder
import ru.tinkoff.phobos.encoding.ElementEncoder
import ru.tinkoff.phobos.syntax.xmlns

case class Body(@xmlns(mdg) BatchFileInterfaceMetadata: BatchFileInterfaceMetadata)

object Body {
  implicit val batchFileInterfaceMetadataEncoder: ElementEncoder[Body] = deriveElementEncoder[Body]
}