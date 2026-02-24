/*
 * Copyright 2023 HM Revenue & Customs
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

import models.EmailAddress
import utils.SpecBase
import utils.TestData.{COUNTRY_CODE_GB, COUNTRY_NAME, TEST_CITY, TEST_EMAIL, TEST_EORI, TEST_STATUS, XI_EORI_NUMBER}
import play.api.libs.json.{JsResultException, JsSuccess, Json}

class SubscriptionDisplayResponseSpec extends SpecBase {

  "pbeAddressFormat" should {
    import SubscriptionResponse.pbeAddressFormat

    "generate correct output for Json Reads" in new Setup {
      Json.fromJson(Json.parse(pbeAddressObJsString)) mustBe JsSuccess(pbeAddressOb)
    }

    "generate correct output for Json Writes" in new Setup {
      Json.toJson(pbeAddressOb) mustBe Json.parse(pbeAddressObJsString)
    }

    "throw exception for invalid Json" in {
      val invalidJson = "{ \"pbeAddress\": \"London Street\", \"totalAmount\": \"600\" }"

      intercept[JsResultException] {
        Json.parse(invalidJson).as[PbeAddress]
      }
    }
  }

  "euVatFormat" should {
    import SubscriptionResponse.euVatFormat

    "generate correct output for Json Reads" in new Setup {
      Json.fromJson(Json.parse(euvatNumberObJsString)) mustBe JsSuccess(euvatNumberOb)
    }

    "generate correct output for Json Writes" in new Setup {
      Json.toJson(euvatNumberOb) mustBe Json.parse(euvatNumberObJsString)
    }
  }

  "xiSubscriptionFormat" should {
    import SubscriptionResponse.xiSubscriptionFormat

    "generate correct output for Json Reads" in new Setup {
      Json.fromJson(Json.parse(xiSubscriptionObJsString)) mustBe JsSuccess(xiSubscriptionOb)
    }

    "generate correct output for Json Writes" in new Setup {
      Json.toJson(xiSubscriptionOb) mustBe Json.parse(xiSubscriptionObJsString)
    }

    "throw exception for invalid Json" in {
      val invalidJson = "{ \"xiEori\": \"XI123456\", \"totalAmount\": \"600\" }"

      intercept[JsResultException] {
        Json.parse(invalidJson).as[XiSubscription]
      }
    }
  }

  "returnParametersFormat" should {
    import SubscriptionResponse.returnParametersFormat

    "generate correct output for Json Reads" in new Setup {
      Json.fromJson(Json.parse(returnParamJsString)) mustBe JsSuccess(returnParamOb)
    }

    "generate correct output for Json Writes" in new Setup {
      Json.toJson(returnParamOb) mustBe Json.parse(returnParamJsString)
    }

    "throw exception for invalid Json" in {
      val invalidJson = "{ \"status\": \"pending\", \"eventId1\": \"test_event\" }"

      intercept[JsResultException] {
        Json.parse(invalidJson).as[ReturnParameters]
      }
    }
  }

  "vatIDFormat" should {
    import SubscriptionResponse.vatIDFormat

    "generate correct output for Json Reads" in new Setup {
      Json.fromJson(Json.parse(vatIdObJsString)) mustBe JsSuccess(vatIdOb)
    }

    "generate correct output for Json Writes" in new Setup {
      Json.toJson(vatIdOb) mustBe Json.parse(vatIdObJsString)
    }
  }

  "contactInformationFormat" should {
    import SubscriptionResponse.contactInformationFormat

    "generate correct output for Json Reads" in new Setup {
      Json.fromJson(Json.parse(contactInformationObJsString)) mustBe JsSuccess(contactInformationOb)
    }

    "generate correct output for Json Writes" in new Setup {
      Json.toJson(contactInformationOb) mustBe Json.parse(contactInformationObJsString)
    }
  }

  "cdsEstablishmentAddressFormat" should {
    import SubscriptionResponse.cdsEstablishmentAddressFormat

    "generate correct output for Json Reads" in new Setup {
      Json.fromJson(Json.parse(cdsEstablishmentAddressObJsString)) mustBe JsSuccess(cdsEstablishmentAddressOb)
    }

    "generate correct output for Json Writes" in new Setup {
      Json.toJson(cdsEstablishmentAddressOb) mustBe Json.parse(cdsEstablishmentAddressObJsString)
    }

    "throw exception for invalid Json" in {
      val invalidJson = "{ \"street\": \"Street 1\", \"city1\": \"Bristol\" }"

      intercept[JsResultException] {
        Json.parse(invalidJson).as[CdsEstablishmentAddress]
      }
    }
  }

  "responseCommonFormat" should {
    import SubscriptionResponse.responseCommonFormat

    "generate correct output for Json Reads" in new Setup {
      Json.fromJson(Json.parse(resCommonJsString)) mustBe JsSuccess(responseCommonOb)
    }

    "Invalid JSON" should {
      "fail" in {
        val invalidJson = "{ \"status1\": \"pending\" }"

        intercept[JsResultException] {
          Json.parse(invalidJson).as[ResponseCommon]
        }
      }
    }

    "generate correct output for Json Writes" in new Setup {
      Json.toJson(responseCommonOb) mustBe Json.parse(resCommonJsString)
    }
  }

  "responseDetailFormat" should {
    import SubscriptionResponse.responseDetailFormat

    "generate correct output for Json Reads" in new Setup {
      Json.fromJson(Json.parse(responseDetailsObJsString)) mustBe JsSuccess(responseDetailsOb)
    }

    "Invalid JSON" should {
      "fail" in {
        val invalidJson = "{ \"status1\": \"pending\", \"eventId1\": \"test_event\" }"

        intercept[JsResultException] {
          Json.parse(invalidJson).as[ResponseDetail]
        }
      }
    }

    "generate correct output for Json Writes" in new Setup {
      Json.toJson(responseDetailsOb) mustBe Json.parse(responseDetailsObJsString)
    }
  }

  "subscriptionDisplayResponseFormat" should {
    import SubscriptionResponse.subscriptionDisplayResponseFormat

    "generate correct output for Json Reads" in new Setup {
      Json.fromJson(Json.parse(subscriptionDisplayResponseObJsString)) mustBe JsSuccess(subscriptionDisplayResponseOb)
    }

    "Invalid JSON" should {
      "fail" in {
        val invalidJson = "{ \"resCommon\": \"pending\", \"resDetails\": \"test_event\" }"

        intercept[JsResultException] {
          Json.parse(invalidJson).as[SubscriptionDisplayResponse]
        }
      }
    }

    "generate correct output for Json Writes" in new Setup {
      Json.toJson(subscriptionDisplayResponseOb) mustBe Json.parse(subscriptionDisplayResponseObJsString)
    }
  }

  "SubscriptionResponse.responseSubscriptionFormat" should {
    import SubscriptionResponse.responseSubscriptionFormat

    "generate correct output for Json Reads" in new Setup {
      Json.fromJson(Json.parse(subscriptionResponseObJsString)) mustBe JsSuccess(subscriptionResponseOb)
    }

    "Invalid JSON" should {
      "fail" in {
        val invalidJson = "{ \"status1\": \"pending\", \"eventId1\": \"test_event\" }"

        intercept[JsResultException] {
          Json.parse(invalidJson).as[SubscriptionResponse]
        }
      }
    }

    "generate correct output for Json Writes" in new Setup {
      Json.toJson(subscriptionResponseOb) mustBe Json.parse(subscriptionResponseObJsString)
    }
  }

  trait Setup {
    val pbeAddressOb: PbeAddress = PbeAddress(
      pbeAddressLine1 = "66 London Road",
      pbeAddressLine2 = Some("block 21"),
      pbeAddressLine3 = None,
      pbeAddressLine4 = None,
      pbePostCode = Some("RG18FV")
    )

    val euvatNumberOb: EUVATNumber = EUVATNumber(Some(COUNTRY_CODE_GB), Some("test_id"))

    val xiSubscriptionOb: XiSubscription = XiSubscription(
      XI_EORINo = XI_EORI_NUMBER,
      PBEAddress = Some(pbeAddressOb),
      establishmentInTheCustomsTerritoryOfTheUnion = None,
      XI_VATNumber = Some("Test_number"),
      EU_VATNumber = None,
      XI_ConsentToDisclose = "1",
      XI_SICCode = None
    )

    val vatIdOb: VatId = VatId(Some(COUNTRY_CODE_GB), Some("test_id"))

    val contactInformationOb: ContactInformation = ContactInformation(
      personOfContact = None,
      sepCorrAddrIndicator = Some(true),
      streetAndNumber = None,
      city = Some(TEST_CITY),
      postalCode = None,
      countryCode = None,
      telephoneNumber = None,
      faxNumber = None,
      emailAddress = Some(EmailAddress(TEST_EMAIL)),
      emailVerificationTimestamp = None
    )

    val cdsEstablishmentAddressOb: CdsEstablishmentAddress =
      CdsEstablishmentAddress(
        streetAndNumber = "10",
        city = TEST_CITY,
        postalCode = Some("RG18NB"),
        countryCode = COUNTRY_CODE_GB
      )

    val returnParamOb: ReturnParameters = ReturnParameters("test_param", "test_param_value")

    val responseCommonOb: ResponseCommon = ResponseCommon(
      status = TEST_STATUS,
      statusText = Some("test_status"),
      processingDate = "test_data",
      returnParameters = None
    )

    val responseDetailsOb: ResponseDetail = ResponseDetail(
      Some(TEST_EORI),
      None,
      None,
      "CDSFullName",
      cdsEstablishmentAddressOb,
      Some("0"),
      None,
      None,
      None,
      None,
      None,
      None,
      None,
      None,
      None,
      ETMP_Master_Indicator = true,
      Some(xiSubscriptionOb)
    )

    val subscriptionDisplayResponseOb: SubscriptionDisplayResponse =
      SubscriptionDisplayResponse(responseCommonOb, responseDetailsOb)

    val subscriptionResponseOb: SubscriptionResponse = SubscriptionResponse(subscriptionDisplayResponseOb)

    val pbeAddressObJsString: String =
      """{"pbeAddressLine1":"66 London Road","pbeAddressLine2":"block 21","pbePostCode":"RG18FV"}""".stripMargin

    val euvatNumberObJsString: String    = """{"countryCode":"GB","VATId":"test_id"}""".stripMargin
    val xiSubscriptionObJsString: String =
      """{"XI_EORINo":"XI12345678",
        |"PBEAddress":{"pbeAddressLine1":"66 London Road","pbeAddressLine2":"block 21","pbePostCode":"RG18FV"},
        |"XI_VATNumber":"Test_number","XI_ConsentToDisclose":"1"
        |}""".stripMargin

    val vatIdObJsString: String              = """{"countryCode":"GB","VATID":"test_id"}""".stripMargin
    val contactInformationObJsString: String =
      """{"sepCorrAddrIndicator":true,"city":"London","emailAddress":"test@test.com"}""".stripMargin

    val cdsEstablishmentAddressObJsString: String =
      """{"streetAndNumber":"10","city":"London","postalCode":"RG18NB","countryCode":"GB"}""".stripMargin

    val returnParamJsString: String = """{"paramName":"test_param","paramValue":"test_param_value"}""".stripMargin

    val resCommonJsString: String =
      """{"status":"pending","statusText":"test_status","processingDate":"test_data"}""".stripMargin

    val responseDetailsObJsString: String =
      """{"EORINo":"testEORI",
        |"CDSFullName":"CDSFullName",
        |"CDSEstablishmentAddress":{"streetAndNumber":"10","city":"London","postalCode":"RG18NB","countryCode":"GB"},
        |"establishmentInTheCustomsTerritoryOfTheUnion":"0",
        |"ETMP_Master_Indicator":true,
        |"XI_Subscription":{"XI_EORINo":"XI12345678",
        |"PBEAddress":{"pbeAddressLine1":"66 London Road","pbeAddressLine2":"block 21","pbePostCode":"RG18FV"},
        |"XI_VATNumber":"Test_number",
        |"XI_ConsentToDisclose":"1"
        |}
        |}""".stripMargin

    val subscriptionDisplayResponseObJsString: String =
      """{"responseCommon":{"status":"pending","statusText":"test_status","processingDate":"test_data"},
        |"responseDetail":{"EORINo":"testEORI",
        |"CDSFullName":"CDSFullName",
        |"CDSEstablishmentAddress":{"streetAndNumber":"10","city":"London","postalCode":"RG18NB","countryCode":"GB"},
        |"establishmentInTheCustomsTerritoryOfTheUnion":"0",
        |"ETMP_Master_Indicator":true,
        |"XI_Subscription":{"XI_EORINo":"XI12345678",
        |"PBEAddress":{"pbeAddressLine1":"66 London Road","pbeAddressLine2":"block 21","pbePostCode":"RG18FV"},
        |"XI_VATNumber":"Test_number",
        |"XI_ConsentToDisclose":"1"
        |}}}""".stripMargin

    val subscriptionResponseObJsString: String =
      """{"subscriptionDisplayResponse":{
        |"responseCommon":{"status":"pending","statusText":"test_status","processingDate":"test_data"},
        |"responseDetail":{"EORINo":"testEORI",
        |"CDSFullName":"CDSFullName",
        |"CDSEstablishmentAddress":{"streetAndNumber":"10","city":"London","postalCode":"RG18NB","countryCode":"GB"},
        |"establishmentInTheCustomsTerritoryOfTheUnion":"0",
        |"ETMP_Master_Indicator":true,
        |"XI_Subscription":{"XI_EORINo":"XI12345678",
        |"PBEAddress":{"pbeAddressLine1":"66 London Road","pbeAddressLine2":"block 21","pbePostCode":"RG18FV"},
        |"XI_VATNumber":"Test_number",
        |"XI_ConsentToDisclose":"1"
        |}
        |}
        |}}""".stripMargin
  }
}
