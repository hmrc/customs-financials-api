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

import java.time.LocalDate

object TestData {

  val EORI_VALUE = "testEORI"
  val FILE_SIZE_1000L = 1000L
  val CURRENT_LOCAL_DATE: LocalDate = LocalDate.now
  val CSV_FILE_NAME = "abc.csv"
  val FILE_ROLE_C79_CERTIFICATE: FileRole = FileRole("C79Certificate")

  val YEAR_2019 = 2019
  val YEAR_2020 = 2020
  val YEAR_2021 = 2021
  val YEAR_2023 = 2023

  val MONTH_1 = 1
  val MONTH_2 = 2
  val MONTH_3 = 3
  val MONTH_4 = 4
  val MONTH_9 = 9
  val MONTH_10 = 10
  val MONTH_12 = 12

  val DAY_11 = 11
  val DAY_14 = 14
  val DAY_15 = 15
  val DAY_16 = 16

  val HOUR_11 = 11
  val HOUR_16 = 16

  val MINUTES_10 = 10
  val MINUTES_30 = 30

  val SECONDS_35 = 35
  val SECONDS_30 = 30

  val NUMBER_5 = 5
  val NUMBER_9 = 9
  val NUMBER_10 = 10
}
