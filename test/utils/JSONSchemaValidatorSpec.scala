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

import org.scalatest.TryValues
import play.api.Application
import play.api.test.Helpers.running

class JSONSchemaValidatorSpec extends SpecBase with TryValues with JsonFileReader {
  val ssfnRequestSchemaPath = "/schemas/statement-search-failure-notification-request-schema.json"
  val ssfnErrorResponseSchemaPath = "/schemas/statement-search-failure-notification-error-response-schema.json"
  val ssfnValidRequestJsonFilePath = "/ssfn-valid-request.json"
  val ssfnInvalidRequestJsonFilePath = "/ssfn-invalid-request.json"
  val ssfnValidErrorResponseJsonFilePath = "/ssfn-valid-error-response.json"
  val ssfnInvalidErrorResponseJsonFilePath = "/ssfn-invalid-error-response.json"
  val ssfnInvalidMultipleErrorsErrorResponseJsonFilePath = "/ssfn-invalid-multiple-errors-error-response.json"

  "validateJson" must {
    "validate the ssfn valid request" in new Setup {
      running(app) {
        val result = jsonPayloadSchemaValidator.validatePayload(
          readJsonFromFile(ssfnValidRequestJsonFilePath), ssfnRequestSchemaPath)
        result.success.value mustBe()
      }
    }

    "return error for ssfn invalid request" in new Setup {
      running(app) {
        val result = jsonPayloadSchemaValidator.validatePayload(
          readJsonFromFile(ssfnInvalidRequestJsonFilePath), ssfnRequestSchemaPath)

        result.isFailure mustBe true
        result.failure.exception.getMessage must include("Schema validation errors are :-")
        result.failure.exception.getMessage must include("/StatementSearchFailureNotificationMetadata/reason")
      }
    }

    "validate the ssfn valid error response" in new Setup {
      running(app) {
        val result = jsonPayloadSchemaValidator.validatePayload(
          readJsonFromFile(ssfnValidErrorResponseJsonFilePath), ssfnErrorResponseSchemaPath)
        result.success.value mustBe()
      }
    }

    "return error for ssfn invalid error response" in new Setup {
      running(app) {
        val result = jsonPayloadSchemaValidator.validatePayload(
          readJsonFromFile(ssfnInvalidErrorResponseJsonFilePath), ssfnErrorResponseSchemaPath)

        result.isFailure mustBe true
        result.failure.exception.getMessage must include("Schema validation errors are :-")
        result.failure.exception.getMessage must include("/errorDetail/correlationId")
      }
    }

    "return errors for ssfn invalid error response that has multiple incorrect values" in new Setup {
      running(app) {
        val result = jsonPayloadSchemaValidator.validatePayload(
          readJsonFromFile(ssfnInvalidMultipleErrorsErrorResponseJsonFilePath), ssfnErrorResponseSchemaPath)

        result.isFailure mustBe true
        result.failure.exception.getMessage must include("Schema validation errors are :-")
        result.failure.exception.getMessage must include("/errorDetail/correlationId")
        result.failure.exception.getMessage must include("/errorDetail/errorCode")
      }
    }
  }

  trait Setup {
    val app: Application = application().build()
    val jsonPayloadSchemaValidator: JSONSchemaValidator = app.injector.instanceOf[JSONSchemaValidator]
  }
}
