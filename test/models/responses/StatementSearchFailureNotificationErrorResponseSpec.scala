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
import uk.gov.hmrc.http.BadRequestException
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
    "create the object correctly with errorCode, errorMessage, source and sourceFaultDetail" +
      "when schema validator generates error for missing single field" in {

      val correlationId = "3jh1f6b3-f8b1-4f3c-973a-05b4720e"
      val sourceFaultDetail = SourceFaultDetail(Seq(
        "object has missing required properties ([StatementSearchFailureNotificationMetadata])"))

      val errorDetail = ErrorDetail(Utils.currentDateTimeAsRFC7231(LocalDateTime.now()),
        correlationId,
        errorCode = ErrorCode.code400,
        errorMessage = ErrorMessage.badRequestReceived,
        source = ErrorSource.cdsFinancials,
        sourceFaultDetail = sourceFaultDetail)

      val expectedSSFNErrorResOb = StatementSearchFailureNotificationErrorResponse(errorDetail)

      val schemaErrorMsg = "(: object has missing required properties ([\"StatementSearchFailureNotificationMetadata\"]))"
      val actualOb = StatementSearchFailureNotificationErrorResponse(
        Option(new BadRequestException(schemaErrorMsg)), ErrorCode.code400, correlationId)

      actualOb.errorDetail.errorCode mustBe expectedSSFNErrorResOb.errorDetail.errorCode
      actualOb.errorDetail.correlationId mustBe expectedSSFNErrorResOb.errorDetail.correlationId
      actualOb.errorDetail.source mustBe expectedSSFNErrorResOb.errorDetail.source
      actualOb.errorDetail.errorMessage mustBe expectedSSFNErrorResOb.errorDetail.errorMessage
      actualOb.errorDetail.sourceFaultDetail mustBe expectedSSFNErrorResOb.errorDetail.sourceFaultDetail
    }

    "create the object correctly with errorCode, errorMessage, source and sourceFaultDetail" +
      "when schema validator generates errors for missing both required fields" in {

      val correlationId = "3jh1f6b3-f8b1-4f3c-973a-05b4720e"
      val sourceFaultDetail = SourceFaultDetail(Seq(
        "/StatementSearchFailureNotificationMetadata:" +
          " object has missing required properties ([reason,statementRequestID])"))

      val errorDetail = ErrorDetail(Utils.currentDateTimeAsRFC7231(LocalDateTime.now()),
        correlationId,
        errorCode = ErrorCode.code400,
        errorMessage = ErrorMessage.badRequestReceived,
        source = ErrorSource.cdsFinancials,
        sourceFaultDetail = sourceFaultDetail)

      val expectedSSFNErrorResOb = StatementSearchFailureNotificationErrorResponse(errorDetail)

      val schemaErrorMsg = "(/StatementSearchFailureNotificationMetadata:" +
        " object has missing required properties ([\"reason\",\"statementRequestID\"]))"
      val actualOb = StatementSearchFailureNotificationErrorResponse(
        Option(new BadRequestException(schemaErrorMsg)), ErrorCode.code400, correlationId)

      actualOb.errorDetail.errorCode mustBe expectedSSFNErrorResOb.errorDetail.errorCode
      actualOb.errorDetail.correlationId mustBe expectedSSFNErrorResOb.errorDetail.correlationId
      actualOb.errorDetail.source mustBe expectedSSFNErrorResOb.errorDetail.source
      actualOb.errorDetail.errorMessage mustBe expectedSSFNErrorResOb.errorDetail.errorMessage
      actualOb.errorDetail.sourceFaultDetail mustBe expectedSSFNErrorResOb.errorDetail.sourceFaultDetail
    }

    "create the object correctly with errorCode, errorMessage, source and sourceFaultDetail" +
      "when schema validator generates errors for not matching the statementRequestID reg ex" in {

      val correlationId = "3jh1f6b3-f8b1-4f3c-973a-05b4720e"
      val sourceFaultDetail = SourceFaultDetail(Seq(
        "/StatementSearchFailureNotificationMetadata/statementRequestID:" +
          " ECMA 262 regex ^[A-Fa-f0-9-]{36}$ does not match input string 1641bd46"
      ))

      val errorDetail = ErrorDetail(Utils.currentDateTimeAsRFC7231(LocalDateTime.now()),
        correlationId,
        errorCode = ErrorCode.code400,
        errorMessage = ErrorMessage.badRequestReceived,
        source = ErrorSource.cdsFinancials,
        sourceFaultDetail = sourceFaultDetail)

      val expectedSSFNErrorResOb = StatementSearchFailureNotificationErrorResponse(errorDetail)

      val schemaErrorMsg = "(/StatementSearchFailureNotificationMetadata/statementRequestID:" +
        " ECMA 262 regex \"^[A-Fa-f0-9-]{36}$\" does not match input string \"1641bd46\")"
      val actualOb = StatementSearchFailureNotificationErrorResponse(
        Option(new BadRequestException(schemaErrorMsg)), ErrorCode.code400, correlationId)

      actualOb.errorDetail.errorCode mustBe expectedSSFNErrorResOb.errorDetail.errorCode
      actualOb.errorDetail.correlationId mustBe expectedSSFNErrorResOb.errorDetail.correlationId
      actualOb.errorDetail.source mustBe expectedSSFNErrorResOb.errorDetail.source
      actualOb.errorDetail.errorMessage mustBe expectedSSFNErrorResOb.errorDetail.errorMessage
      actualOb.errorDetail.sourceFaultDetail mustBe expectedSSFNErrorResOb.errorDetail.sourceFaultDetail
    }

    "create the object correctly with errorCode, errorMessage, source and sourceFaultDetail " +
      "when schema validator generates multiple errors" in {

      val correlationId = "3jh1f6b3-f8b1-4f3c-973a-05b4720e"

      val sourceFaultDetail = SourceFaultDetail(Seq(
        "/StatementSearchFailureNotificationMetadata: object has missing required properties ([statementRequestID])",
        "/StatementSearchFailureNotificationMetadata/reason: instance value (Unknown) not found in enum" +
          " (possible values: [NoDocumentsFound,DocumentumUnreachable,DocumentumException,AWSUnreachable," +
          "AWSException,BadRequestReceived,CDDMInternalError])"
      ))

      val errorDetail = ErrorDetail(Utils.currentDateTimeAsRFC7231(LocalDateTime.now()),
        correlationId,
        errorCode = ErrorCode.code400,
        errorMessage = ErrorMessage.badRequestReceived,
        source = ErrorSource.cdsFinancials,
        sourceFaultDetail = sourceFaultDetail)

      val expectedSSFNErrorResOb = StatementSearchFailureNotificationErrorResponse(errorDetail)

      val schemaErrorMsg = "(/StatementSearchFailureNotificationMetadata:" +
        " object has missing required properties ([\"statementRequestID\"])):::" +
        "(/StatementSearchFailureNotificationMetadata/reason: instance value (\"Unknown\")" +
        " not found in enum (possible values: [\"NoDocumentsFound\",\"DocumentumUnreachable\"," +
        "\"DocumentumException\"," +
        "\"AWSUnreachable\",\"AWSException\",\"BadRequestReceived\",\"CDDMInternalError\"]))"

      val actualOb = StatementSearchFailureNotificationErrorResponse(
        Option(new BadRequestException(schemaErrorMsg)), ErrorCode.code400, correlationId)

      actualOb.errorDetail.errorCode mustBe expectedSSFNErrorResOb.errorDetail.errorCode
      actualOb.errorDetail.correlationId mustBe expectedSSFNErrorResOb.errorDetail.correlationId
      actualOb.errorDetail.source mustBe expectedSSFNErrorResOb.errorDetail.source
      actualOb.errorDetail.errorMessage mustBe expectedSSFNErrorResOb.errorDetail.errorMessage
      actualOb.errorDetail.sourceFaultDetail.detail.size mustBe
        expectedSSFNErrorResOb.errorDetail.sourceFaultDetail.detail.size
    }

    "create the object correctly with errorCode, errorMessage, source and sourceFaultDetail " +
      "when statementRequestID is present" in new Setup {
      val correlationId = "3jh1f6b3-f8b1-4f3c-973a-05b4720e"
      val statementReqId = "9041cc6e-9afb-42ad-b4f1-f017d884fc17"

      val sourceFaultDetail = SourceFaultDetail(Seq(ErrorMessage.invalidStatementReqIdDetail(statementReqId)))

      val errorDetail = ErrorDetail(Utils.currentDateTimeAsRFC7231(LocalDateTime.now()),
        correlationId,
        errorCode = ErrorCode.code400,
        errorMessage = ErrorMessage.invalidStatementReqId,
        source = ErrorSource.cdsFinancials,
        sourceFaultDetail = sourceFaultDetail)

      val expectedSSFNErrorResOb = StatementSearchFailureNotificationErrorResponse(errorDetail)

      val schemaErrorMsg = ""

      val actualOb = StatementSearchFailureNotificationErrorResponse(
        Option(new BadRequestException(schemaErrorMsg)), ErrorCode.code400, correlationId, Option(statementReqId))

      actualOb.errorDetail.errorCode mustBe expectedSSFNErrorResOb.errorDetail.errorCode
      actualOb.errorDetail.correlationId mustBe expectedSSFNErrorResOb.errorDetail.correlationId
      actualOb.errorDetail.source mustBe expectedSSFNErrorResOb.errorDetail.source
      actualOb.errorDetail.errorMessage mustBe expectedSSFNErrorResOb.errorDetail.errorMessage
      actualOb.errorDetail.sourceFaultDetail.detail.size mustBe
        expectedSSFNErrorResOb.errorDetail.sourceFaultDetail.detail.size

      actualOb.errorDetail.sourceFaultDetail.detail.head mustBe
        expectedSSFNErrorResOb.errorDetail.sourceFaultDetail.detail.head
    }

    "create the object correctly with errorCode, errorMessage, source and sourceFaultDetail " +
      "for technical error and statementRequestId is present " in new Setup {
      val correlationId = "3jh1f6b3-f8b1-4f3c-973a-05b4720e"
      val statementReqId = "9041cc6e-9afb-42ad-b4f1-f017d884fc17"

      val sourceFaultDetail = SourceFaultDetail(Seq(ErrorMessage.technicalErrorDetail(statementReqId)))

      val errorDetail = ErrorDetail(Utils.currentDateTimeAsRFC7231(LocalDateTime.now()),
        correlationId,
        errorCode = ErrorCode.code500,
        errorMessage = ErrorMessage.technicalError,
        source = ErrorSource.cdsFinancials,
        sourceFaultDetail = sourceFaultDetail)

      val expectedSSFNErrorResOb = StatementSearchFailureNotificationErrorResponse(errorDetail)

      val schemaErrorMsg = ""

      val actualOb = StatementSearchFailureNotificationErrorResponse(
        Option(new BadRequestException(schemaErrorMsg)), ErrorCode.code500, correlationId, Option(statementReqId))

      actualOb.errorDetail.errorCode mustBe expectedSSFNErrorResOb.errorDetail.errorCode
      actualOb.errorDetail.correlationId mustBe expectedSSFNErrorResOb.errorDetail.correlationId
      actualOb.errorDetail.source mustBe expectedSSFNErrorResOb.errorDetail.source
      actualOb.errorDetail.errorMessage mustBe expectedSSFNErrorResOb.errorDetail.errorMessage
      actualOb.errorDetail.sourceFaultDetail.detail.size mustBe
        expectedSSFNErrorResOb.errorDetail.sourceFaultDetail.detail.size

      actualOb.errorDetail.sourceFaultDetail.detail.head mustBe
        expectedSSFNErrorResOb.errorDetail.sourceFaultDetail.detail.head
    }

    "create the object correctly with errorCode, errorMessage, source and sourceFaultDetail " +
      "for technical error when statementRequestId and errorDetailMsg are present " in new Setup {
      val correlationId = "3jh1f6b3-f8b1-4f3c-973a-05b4720e"
      val statementReqId = "9041cc6e-9afb-42ad-b4f1-f017d884fc17"

      val sourceFaultDetail: SourceFaultDetail =
        SourceFaultDetail(Seq(ErrorMessage.failureRetryCountErrorDetail(statementReqId)))

      val errorDetail: ErrorDetail = ErrorDetail(Utils.currentDateTimeAsRFC7231(LocalDateTime.now()),
        correlationId,
        errorCode = ErrorCode.code500,
        errorMessage = ErrorMessage.technicalError,
        source = ErrorSource.cdsFinancials,
        sourceFaultDetail = sourceFaultDetail)

      val expectedSSFNErrorResOb: StatementSearchFailureNotificationErrorResponse =
        StatementSearchFailureNotificationErrorResponse(errorDetail)

      val schemaErrorMsg = ""

      val actualOb: StatementSearchFailureNotificationErrorResponse = StatementSearchFailureNotificationErrorResponse(
        Option(new BadRequestException(schemaErrorMsg)),
        ErrorCode.code500,
        correlationId,
        Option(statementReqId),
        ErrorMessage.failureRetryCountErrorDetail(statementReqId))

      actualOb.errorDetail.errorCode mustBe expectedSSFNErrorResOb.errorDetail.errorCode
      actualOb.errorDetail.correlationId mustBe expectedSSFNErrorResOb.errorDetail.correlationId
      actualOb.errorDetail.source mustBe expectedSSFNErrorResOb.errorDetail.source
      actualOb.errorDetail.errorMessage mustBe expectedSSFNErrorResOb.errorDetail.errorMessage
      actualOb.errorDetail.sourceFaultDetail.detail.size mustBe
        expectedSSFNErrorResOb.errorDetail.sourceFaultDetail.detail.size

      actualOb.errorDetail.sourceFaultDetail.detail.head mustBe
        expectedSSFNErrorResOb.errorDetail.sourceFaultDetail.detail.head
    }
  }

  "ErrorMessage.technicalErrorDetail" should {
    "return correct output" in {
      val statementReqId = "test_id"
      ErrorMessage.technicalErrorDetail(statementReqId) mustBe
        s"Technical error occurred while processing the statementRequestId : $statementReqId"
    }
  }

  "ErrorMessage.failureRetryCountErrorDetail" should {
    "return correct output" in {
      val statementReqId = "test_id"
      ErrorMessage.failureRetryCountErrorDetail(statementReqId) mustBe
        s"Failure retry count has reached its max value for statementRequestId : $statementReqId"
    }
  }

  "ErrorMessage.invalidStatementReqIdDetail" should {
    "return correct output" in {
      val statementReqId = "test_id"
      ErrorMessage.invalidStatementReqIdDetail(statementReqId) mustBe
        s"statementRequestId : $statementReqId is not recognised"
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
