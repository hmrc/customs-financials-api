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

package models.responses

import play.api.libs.json.Json
import utils.{SpecBase, Utils}

import java.time.LocalDateTime

class StatementSearchFailureNotificationErrorResponseSpec extends SpecBase {

  "ssfnErrorResponseWrites" should {
    "generate correct output" in new Setup {
      Json.toJson(ssfnNotErrorResOb1) mustBe Json.parse(ssfnNotErrorResJsValue1)
      Json.toJson(ssfnNotErrorResOb2) mustBe Json.parse(ssfnNotErrorResJsValue2)
    }
  }

  "apply" should {
    "create the object correctly" in {
      val correlationId = "3jh1f6b3-f8b1-4f3c-973a-05b4720e"
      val sourceFaultDetail = SourceFaultDetail(Seq("BadRequest"))

      val errorDetail = ErrorDetail(Utils.currentDateTimeAsRFC7231(LocalDateTime.now()),
        correlationId,
        "400",
        "Bad request received",
        "CDS Financials",
      sourceFaultDetail)

      val expectedSSFNErrorResOb = StatementSearchFailureNotificationErrorResponse(errorDetail)
      val actualOb = StatementSearchFailureNotificationErrorResponse(new RuntimeException(), correlationId)

      actualOb.errorDetail.errorCode mustBe expectedSSFNErrorResOb.errorDetail.errorCode
      actualOb.errorDetail.correlationId mustBe expectedSSFNErrorResOb.errorDetail.correlationId
      actualOb.errorDetail.source mustBe expectedSSFNErrorResOb.errorDetail.source
      actualOb.errorDetail.errorMessage mustBe expectedSSFNErrorResOb.errorDetail.errorMessage
      actualOb.errorDetail.sourceFaultDetail mustBe expectedSSFNErrorResOb.errorDetail.sourceFaultDetail
    }
  }

  trait Setup {
    val ssfnNotErrorResJsValue1: String =
      """{
        | "errorDetail": {
        | "timestamp": "2019-08-16T18:15:41Z",
        | "correlationId": "3jh1f6b3-f8b1-4f3c-973a-05b4720e",
        | "errorCode": "400",
        | "errorMessage": "Bad request received",
        | "source": "CDS Financials",
        | "sourceFaultDetail": {
        | "detail": [
        |  "Invalid value supplied for field statementRequestId: 32 | Invalid value supplied for field failureReason: NoDocs"
        |  ]
        |  }
        |  }
        |  }""".stripMargin

    val ssfnNotErrorResJsValue2: String =
      """{
        | "errorDetail": {
        | "timestamp": "2019-08-16T18:15:41Z",
        | "correlationId": "3jh1f6b3-f8b1-4f3c-973a-05b4720e",
        | "errorCode": "400",
        | "errorMessage": "Bad request received",
        | "source": "CDS Financials",
        | "sourceFaultDetail": {
        | "detail": [
        |  "Invalid value supplied for field statementRequestId: 32 | Invalid value supplied for field failureReason: NoDocs",
        |  "Invalid value supplied for field statementRequestId: 32"
        |  ]
        |  }
        |  }
        |  }""".stripMargin


    val sourceFaultDetail1: SourceFaultDetail = SourceFaultDetail(
      Seq("Invalid value supplied for field statementRequestId: 32 | " +
        "Invalid value supplied for field failureReason: NoDocs"))

    val sourceFaultDetail2: SourceFaultDetail = SourceFaultDetail(
      Seq("Invalid value supplied for field statementRequestId: 32 | " +
        "Invalid value supplied for field failureReason: NoDocs",
        "Invalid value supplied for field statementRequestId: 32"))

    val errorDetail1: ErrorDetail = ErrorDetail("2019-08-16T18:15:41Z",
      "3jh1f6b3-f8b1-4f3c-973a-05b4720e",
      "400",
      "Bad request received",
      "CDS Financials",
      sourceFaultDetail1)

    val errorDetail2: ErrorDetail = ErrorDetail("2019-08-16T18:15:41Z",
      "3jh1f6b3-f8b1-4f3c-973a-05b4720e",
      "400",
      "Bad request received",
      "CDS Financials",
      sourceFaultDetail2)

    val ssfnNotErrorResOb1: StatementSearchFailureNotificationErrorResponse =
      StatementSearchFailureNotificationErrorResponse(errorDetail1)

    val ssfnNotErrorResOb2: StatementSearchFailureNotificationErrorResponse =
      StatementSearchFailureNotificationErrorResponse(errorDetail2)

  }

}
