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

package models

import models.CompanyInformation
import utils.SpecBase

class CompanyInformationSpec extends SpecBase {
  "CompanyInformation" should {
    "Populate correctly" in new Setup {

      val address: AddressInformation = AddressInformation(
        streetAndNumber = "street&Number",
        city = "london",
        postalCode = Option("Post"),
        countryCode = "GB")

      val result: CompanyInformation = CompanyInformation(
        name = "Company Name", consent = "Yes", address = address)

      result mustBe expectedResult
    }
  }

  trait Setup {
    val streetAndNumber: String = "street&Number"
    val city: String = "london"
    val postalCode: Option[String] = Option("Post")
    val countryCode: String = "GB"

    val testAddress: AddressInformation = AddressInformation(
      streetAndNumber = streetAndNumber,
      city = city,
      postalCode = postalCode,
      countryCode = countryCode)

    val name: String = "Company Name"
    val consent: String = "Yes"

    val expectedResult: CompanyInformation = CompanyInformation(
      name = name, consent = consent, address = testAddress)
  }
}

