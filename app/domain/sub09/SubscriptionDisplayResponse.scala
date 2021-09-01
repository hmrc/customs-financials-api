/*
 * Copyright 2021 HM Revenue & Customs
 *
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
                            establishmentInTheCustomsTerritoryOfTheUnion: Option[String],
                            typeOfLegalEntity: Option[String],
                            contactInformation: Option[ContactInformation],
                            VATIDs: Option[Array[VatIDs]],
                            thirdCountryUniqueIdentificationNumber: Option[Array[String]],
                            consentToDisclosureOfPersonalData: Option[String],
                            shortName: Option[String],
                            dateOfEstablishment: Option[String],
                            typeOfPerson: Option[String],
                            principalEconomicActivity: Option[String],
                            ETMP_Master_Indicator: Boolean
                          )

case class ReturnParameters (
                              paramName: String,
                              paramValue: String
                            )

case class VatIDs (
                    countryCode: String,
                    VATID: String
                  )

object SubscriptionResponse {
  implicit val vatIDsFormat = Json.format[VatIDs]
  implicit val returnParametersFormat = Json.format[ReturnParameters]
  implicit val contactInformationFormat = Json.format[ContactInformation]
  implicit val cdsEstablishmentAddressFormat = Json.format[CdsEstablishmentAddress]
  implicit val responseDetailFormat = Json.format[ResponseDetail]
  implicit val responseCommonFormat = Json.format[ResponseCommon]
  implicit val subscriptionDisplayResponseFormat = Json.format[SubscriptionDisplayResponse]
  implicit val responseSubscriptionFormat = Json.format[SubscriptionResponse]
}
