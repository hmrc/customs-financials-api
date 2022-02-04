package models.css

import ru.tinkoff.phobos

object Namespaces {
  case object soap
  implicit val soapNamespace: phobos.Namespace[soap.type] =
    phobos.Namespace.mkInstance("http://schemas.xmlsoap.org/soap/envelope/")

  case object xs
  implicit val xsNamespace: phobos.Namespace[xs.type] =
    phobos.Namespace.mkInstance("http://www.w3.org/2001/XMLSchema")

  case object vc
  implicit val vcNamespace: phobos.Namespace[vc.type] =
    phobos.Namespace.mkInstance("http://www.w3.org/2007/XMLSchema-versioning")

  case object mdg
  implicit val mdgNamespace: phobos.Namespace[mdg.type] =
    phobos.Namespace.mkInstance("http://www.hmrc.gsi.gov.uk/mdg/batchFileInterfaceMetadataSchema")
}
