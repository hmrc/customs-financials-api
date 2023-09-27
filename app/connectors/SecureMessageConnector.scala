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
import models.{AccountType, EORI, HistoricDocumentRequestSearch}
import java.time.LocalDate

import domain.SecureMessage.{Content, Request, Response}
import services.SubscriptionService
import play.api.{Logger, LoggerLike}
import play.api.libs.json.{JsValue, Json}
import utils.JSONSchemaValidator

import scala.util.{Failure, Success}

class SecureMessageConnector @Inject()(
  httpClient: HttpClient,
  appConfig: AppConfig,
  jsonSchemaValidator: JSONSchemaValidator,
  mdgHeaders: MdgHeaders,
  service: SubscriptionService
)(implicit executionContext: ExecutionContext) {

  def sendSecureMessage(histDoc: HistoricDocumentRequestSearch): Future[SecureMessage.Response] = {

    val log: LoggerLike = Logger(this.getClass)

    val subjectHeader = getSubjectHeader(histDoc.params.accountType)
    val contents = getContents(subjectHeader)
    val request: Request = getRequest(histDoc, contents)

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
        log.error(s"Json Schema Failed Validation for SendSecureMessage")
        Future(SecureMessage.Response(histDoc.currentEori))
    }
  }

  def getRequest(hisDoc: HistoricDocumentRequestSearch, contents: List[Content]): SecureMessage.Request = {

    SecureMessage.Request(
      externalRef = SecureMessage.ExternalReference(hisDoc.searchID.toString, "mdtp"),
      recipient = SecureMessage.Recipient("cds",
        SecureMessage.TaxIdentifier("HMRC-CUS-ORG", hisDoc.currentEori),
        params = SecureMessage.Params(hisDoc.params.periodStartMonth, hisDoc.params.periodStartYear,
        hisDoc.params.periodEndMonth,hisDoc.params.periodEndYear, "Financials"),
      email = "test@test.com"),
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
