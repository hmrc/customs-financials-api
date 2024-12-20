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

import com.github.fge.jackson.JsonLoader
import com.github.fge.jsonschema.core.report.ListProcessingReport
import com.github.fge.jsonschema.main.JsonSchemaFactory
import play.api.libs.json.*
import uk.gov.hmrc.http.BadRequestException
import utils.Utils.threeColons

import scala.util.{Failure, Success, Try}

case class ErrorReport(instance: String, errors: String) {
  override def toString: String = s"($instance: $errors)"
}

class JSONSchemaValidator {
  private type ValidationReport = Either[List[ErrorReport], Unit]
  private val basePath = System.getProperty("user.dir")

  val ssfnRequestSchema              = "/schemas/statement-search-failure-notification-request-schema.json"
  val ssfnErrorResponseSchema        = "/schemas/statement-search-failure-notification-error-response-schema.json"
  val ssfnSecureMessageRequestSchema = "/schemas/secure-message-request-schema.json"
  val acc44RequestSchema             = "/schemas/acc44-fe-request-schema-v1.2.json"
  val acc44ResponseSchema            = "/schemas/acc44-fe-response-schema-success-v1.2.json"
  val acc45RequestSchema             = "/schemas/acc45-fe-request-schema-v1.1.json"

  def validatePayload(data: JsValue, apiSchemaPath: String): Try[Unit] =
    validateJsonPayload(apiSchemaPath, data) match {
      case Right(())    => Success(())
      case Left(errors) =>
        val allErrorsAsString = errors.mkString(threeColons)
        Failure(new BadRequestException(allErrorsAsString))
    }

  private def validateJsonPayload(jsonSchemaPath: String, data: JsValue): ValidationReport = {
    val deepValidationCheck = true
    val factory             = JsonSchemaFactory.byDefault()
    val schemaPath          = JsonLoader.fromPath(s"$basePath/conf/$jsonSchemaPath")
    val schema              = factory.getJsonSchema(schemaPath)
    val jsonDataAsString    = JsonLoader.fromString(data.toString())
    val doValidation        = schema.validate(jsonDataAsString, deepValidationCheck)
    val isSuccess           = doValidation.isSuccess

    if (!isSuccess) {
      val jsArray =
        Json.parse(doValidation.asInstanceOf[ListProcessingReport].asJson().toString).asInstanceOf[JsArray].value

      val jsArrayErrors = jsArray.map { error =>
        ((error \ "instance" \ "pointer").asOpt[String], (error \ "message").asOpt[String]) match {
          case Tuple2(Some(instanceOfError), Some(messageOfError)) => ErrorReport(instanceOfError, messageOfError)
          case _                                                   => throw new RuntimeException(s"Error: $jsArray")
        }
      }
      Left(jsArrayErrors.toList)
    } else {
      Right(())
    }
  }
}
