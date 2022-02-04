package models.css

import config.MetaConfig.Dec64
import models.css.Namespaces.mdg
import ru.tinkoff.phobos.derivation.semiauto.deriveElementEncoder
import ru.tinkoff.phobos.encoding.ElementEncoder
import ru.tinkoff.phobos.syntax.xmlns

case class BatchFileInterfaceMetadata(
                                       @xmlns(mdg) sourceSystem: String = Dec64.SOURCE_SYSTEM,
                                       @xmlns(mdg) sourceSystemType: String = Dec64.SOURCE_SYSTEM_TYPE,
                                       @xmlns(mdg) interfaceName: String = Dec64.INTERFACE_NAME,
                                       @xmlns(mdg) interfaceVersion: String = Dec64.INTERFACE_VERSION,
                                       @xmlns(mdg) correlationID: String,
                                       @xmlns(mdg) batchID: String,
                                       @xmlns(mdg) batchSize: Long,
                                       @xmlns(mdg) batchCount: Long,
                                       @xmlns(mdg) checksum: String,
                                       @xmlns(mdg) checksumAlgorithm: String = Dec64.UPSCAN_CHECKSUM_ALGORITHM,
                                       @xmlns(mdg) fileSize: Long,
                                       @xmlns(mdg) compressed: Boolean = false,
                                       @xmlns(mdg) properties: PropertiesType,
                                       @xmlns(mdg) sourceLocation: String,
                                       @xmlns(mdg) sourceFileName: String,
                                       @xmlns(mdg) sourceFileMimeType: String
//                                       @xmlns(mdg) destinations: Destinations = Destinations(List(Destination(Dec64.CDFPay)))
                                     )

object BatchFileInterfaceMetadata {
  implicit val batchFileInterfaceMetadataEncoder: ElementEncoder[BatchFileInterfaceMetadata] =
    deriveElementEncoder[BatchFileInterfaceMetadata]
}