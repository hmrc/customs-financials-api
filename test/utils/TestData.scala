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

package utils

import models.FileRole
import models.requests.EoriRequest
import play.api.libs.json.{JsValue, Json}

import java.time.LocalDate

object TestData {
  val EORI_VALUE                  = "testEORI"
  val EORI_VALUE_1                = "someEORI"
  val EORI_REQUEST: EoriRequest   = EoriRequest("testEoriRequest")
  val EORI_REQUEST_STRING: String = """{"eori": "testEoriRequest"}"""
  val EORI_STRING: String         = "testEoriRequest"
  val EORI_JSON: JsValue          = Json.toJson(EoriRequest(EORI_STRING))

  val TEST_EMAIL   = "test@test.com"
  val TEST_COMPANY = "companyName"

  val FILE_SIZE_1000L               = 1000L
  val FILE_SIZE_1024L               = 1024L
  val FILE_SIZE_75251L              = 75251L
  val FILE_SIZE_2417804L            = 2417804L
  val CURRENT_LOCAL_DATE: LocalDate = LocalDate.now

  val CSV_FILE_NAME                          = "abc.csv"
  val TEST_FILE_NAME                         = "test_file"
  val FILE_ROLE_C79_CERTIFICATE: FileRole    = FileRole("C79Certificate")
  val FILE_ROLE_SECURITY_STATEMENT: FileRole = FileRole("SecurityStatement")

  val YEAR_2019 = 2019
  val YEAR_2020 = 2020
  val YEAR_2021 = 2021
  val YEAR_2023 = 2023

  val MONTH_1  = 1
  val MONTH_2  = 2
  val MONTH_3  = 3
  val MONTH_4  = 4
  val MONTH_6  = 6
  val MONTH_7  = 7
  val MONTH_9  = 9
  val MONTH_10 = 10
  val MONTH_12 = 12

  val DAY_1  = 1
  val DAY_7  = 7
  val DAY_11 = 11
  val DAY_14 = 14
  val DAY_15 = 15
  val DAY_16 = 16

  val HOUR_10 = 10
  val HOUR_11 = 11
  val HOUR_16 = 16

  val MINUTES_5  = 5
  val MINUTES_10 = 10
  val MINUTES_30 = 30

  val SECONDS_35 = 35
  val SECONDS_29 = 29
  val SECONDS_30 = 30

  val MILI_SECONDS_352 = 352

  val NUMBER_5  = 5
  val NUMBER_9  = 9
  val NUMBER_10 = 10

  val OneL      = 1L
  val FiveL     = 5L
  val NineL     = 9L
  val ThirteenL = 13L
  val TwentyL   = 20L

  val COUNTRY_CODE_GB = "GB"

  val REGIME = "cds"

  val ERROR_MSG = "Error occurred"

  val DATE_STRING     = "2024-05-28"
  val PROCESSING_DATE = "2001-12-17T09:30:47Z"

  val PAYMENT_REFERENCE     = "CDSC1234567890"
  val AMOUNT                = 9999.99
  val BANK_ACCOUNT          = "1234567890987"
  val SORT_CODE             = "123456789"
  val CAN                   = "12345678909"
  val INVALID_CAN           = "123456789091234567"
  val EORI_NUMBER           = "GB123456789"
  val DECLARANT_EORI_NUMBER = "GB12345678"
  val EORI_DATA_NAME        = "test"

  val DECLARATION_ID                = "24GB123456789"
  val DECLARANT_REF                 = "1234567890abcdefgh"
  val C18_OR_OVER_PAYMENT_REFERENCE = "RPCSCCCS1"
  val IMPORTERS_EORI_NUMBER         = "GB1234567"

  val ORIGINATING_SYSTEM = "MDTP"
}
