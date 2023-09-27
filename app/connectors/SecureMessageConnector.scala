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

package connectors

import config.AppConfig
import domain.SecureMessage
import uk.gov.hmrc.http.HttpReads.Implicits._
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient}
import javax.inject.Inject

import scala.concurrent.{ExecutionContext, Future}
import models.{AccountType, HistoricDocumentRequestSearch}
import java.time.LocalDate

import domain.SecureMessage.{Content, Request, Response}
import play.api.libs.json.{JsValue, Json}
import utils.JSONSchemaValidator

import scala.util.{Failure, Success}

class SecureMessageConnector @Inject()(
  httpClient: HttpClient,
  appConfig: AppConfig,
  jsonSchemaValidator: JSONSchemaValidator,
  mdgHeaders: MdgHeaders
)(implicit executionContext: ExecutionContext) {

  def sendSecureMessage(histDoc: HistoricDocumentRequestSearch): Future[SecureMessage.Response] = {

    val subjectHeader = getSubjectHeader(histDoc.params.accountType)
    val contents = getContents(subjectHeader)
    val request: Request = getRequest(histDoc.currentEori, contents)

    jsonSchemaValidator.validatePayload(requestBody(request),
      jsonSchemaValidator.ssfnSecureMessageRequestSchema) match {
      case Success(_) =>
        httpClient.POST[Request, Response](
          appConfig.secureMessageEndpoint,
          request,
          headers = mdgHeaders.headers(
            appConfig.secureMessageBearerToken,
            appConfig.secureMessageHostHeader)
        )(implicitly, implicitly, HeaderCarrier(), implicitly)
      case Failure(_) =>
        Future(SecureMessage.Response(histDoc.currentEori))
    }
  }

  def getRequest(eori: String, contents: List[Content]): SecureMessage.Request = {

    SecureMessage.Request(
      externalRef = SecureMessage.ExternalReference(eori, "mdtp"),
      recipient = SecureMessage.Recipient("CDS Financials",
        SecureMessage.TaxIdentifier("HMRC-CUS-ORG", eori)),
      params = SecureMessage.Params(LocalDate.now(), LocalDate.now(), "Financials"),
      email = "email@email.com",
      tags = SecureMessage.Tags("CDS Financials"),
      content = contents,
      messageType = "newMessageAlert",
      validForm = LocalDate.now().toString(),
      alertQueue = "DEFAULT"
    )
  }

  def getSubjectHeader(accountType: String): AccountType = {
    accountType match {
      case "DutyDefermentStatement" => AccountType("DutyDefermentStatement")
      case "C79Certificate" => AccountType("C79Certificate")
      case "SecurityStatement" => AccountType("SecurityStatement")
      case "PostponedVATStatement" => AccountType("PostponedVATStatement")
    }
  }

  def getContents(subjectHeader: AccountType): List[SecureMessage.Content] = {
    List(SecureMessage.Content("en", subjectHeader, SecureMessage.SecureMessage.body),
      SecureMessage.Content("cy", subjectHeader, SecureMessage.SecureMessage.body))
  }

  private def requestBody(request: SecureMessage.Request): JsValue = Json.toJson(request)
}
