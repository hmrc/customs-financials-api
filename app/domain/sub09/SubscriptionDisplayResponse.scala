/*
 * Copyright 2022 HM Revenue & Customs
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

package domain.sub09

import models.{EORI, EmailAddress}
import play.api.libs.json.Json

case class SubscriptionResponse(subscriptionDisplayResponse: SubscriptionDisplayResponse)

case class SubscriptionDisplayResponse (
                                         responseCommon: ResponseCommon,
                                         responseDetail: ResponseDetail
                                       )

case class CdsEstablishmentAddress (
                                     streetAndNumber: String,
                                     city: String,
                                     postalCode: Option[String],
                                     countryCode: String
                                   )

case class ContactInformation (
                                personOfContact: Option[String],
                                sepCorrAddrIndicator: Option[Boolean],
                                streetAndNumber: Option[String],
                                city: Option[String],
                                postalCode: Option[String],
                                countryCode: Option[String],
                                telephoneNumber: Option[String],
                                faxNumber: Option[String],
                                emailAddress: Option[EmailAddress],
                                emailVerificationTimestamp: Option[String]
                              )

case class ResponseCommon (
                            status: String,
                            statusText: Option[String],
                            processingDate: String,
                            returnParameters: Option[Array[ReturnParameters]]
                          )

case class ResponseDetail (
                            EORINo: Option[EORI],
                            EORIStartDate: Option[String],
                            EORIEndDate: Option[String],
                            CDSFullName: String,
                            CDSEstablishmentAddress: CdsEstablishmentAddress,
                            typeOfLegalEntity: Option[String],
                            contactInformation: Option[ContactInformation],
                            thirdCountryUniqueIdentificationNumber: Option[Array[String]],
                            consentToDisclosureOfPersonalData: Option[String],
                            shortName: Option[String],
                            dateOfEstablishment: Option[String],
                            typeOfPerson: Option[String],
                            principalEconomicActivity: Option[String],
                            ETMP_Master_Indicator: Boolean,
                            XI_Subscription: Option[XiSubscription]
                          )

case class ReturnParameters(paramName: String,
                            paramValue: String)

case class XiSubscription(XI_EORINo: String,
                          PBEAddress: Option[PbeAddress],
                          XI_VATNumber: Option[String],
                          EU_VATNumber: Option[EUVATNumber],
                          XI_ConsentToDisclose: Option[String],
                          XI_SICCode: Option[String]
                  )

case class PbeAddress(streetNumber1: String,
                      streetNumber2: Option[String],
                      city: Option[String],
                      postalCode: Option[String],
                      countryCode: Option[String])

case class EUVATNumber(countryCode: Option[String],
                       VATId: Option[String])

object SubscriptionResponse {
  implicit val pbeAddressFormat = Json.format[PbeAddress]
  implicit val euVatFormat = Json.format[EUVATNumber]
  implicit val xiSubscriptionFormat = Json.format[XiSubscription]
  implicit val returnParametersFormat = Json.format[ReturnParameters]
  implicit val contactInformationFormat = Json.format[ContactInformation]
  implicit val cdsEstablishmentAddressFormat = Json.format[CdsEstablishmentAddress]
  implicit val responseDetailFormat = Json.format[ResponseDetail]
  implicit val responseCommonFormat = Json.format[ResponseCommon]
  implicit val subscriptionDisplayResponseFormat = Json.format[SubscriptionDisplayResponse]
  implicit val responseSubscriptionFormat = Json.format[SubscriptionResponse]
}
