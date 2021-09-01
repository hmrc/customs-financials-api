/*
 * Copyright 2021 HM Revenue & Customs
 *
 */

package domain.acc38

import models.EORI
import play.api.libs.json.{Json, OFormat}

case class Request(getCorrespondenceAddressRequest: GetCorrespondenceAddressRequest)

object Request {
  implicit val format: OFormat[Request] = Json.format[Request]
}

case class GetCorrespondenceAddressRequest(requestCommon: RequestCommon,
                                           requestDetail: RequestDetail)

object GetCorrespondenceAddressRequest {
  implicit val format: OFormat[GetCorrespondenceAddressRequest] = Json.format[GetCorrespondenceAddressRequest]
}

case class RequestCommon(originatingSystem: String,
                         receiptDate: String,
                         acknowledgementReference: String)

object RequestCommon {
  implicit val format: OFormat[RequestCommon] = Json.format[RequestCommon]
}

case class RequestDetail(eori: EORI,
                         accountDetails: AccountDetails,
                         referenceDate: Option[String])

object RequestDetail {
  implicit val format: OFormat[RequestDetail] = Json.format[RequestDetail]
}
