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
import play.api.libs.json.{JsValue, Json}
import utils.JSONSchemaValidator
import scala.util.Success

class SecureMessageConnector @Inject()(
  httpClient: HttpClient,
  appConfig: AppConfig,
  jsonSchemaValidator: JSONSchemaValidator,
  mdgHeaders: MdgHeaders
)(implicit executionContext: ExecutionContext) {

  def sendSecureMessage(histDoc: HistoricDocumentRequestSearch): Future[SecureMessage.Response] = {

    val subjectHeader: AccountType = histDoc.params.accountType match {
      case "DutyDefermentStatement" => AccountType("DutyDefermentStatement")
      case "C79Certificate" => AccountType("C79Certificate")
      case "SecurityStatement" => AccountType("SecurityStatement")
      case "PostponedVATStatement" => AccountType("PostponedVATStatement")
    }

    val contents: List[SecureMessage.Content] = List(
      SecureMessage.Content("en", subjectHeader, SecureMessage.SecureMessage.body),
      SecureMessage.Content("cy", subjectHeader, SecureMessage.SecureMessage.body)
    )

    val commonRequest = SecureMessage.RequestCommon(
      externalRef = SecureMessage.ExternalReference(histDoc.currentEori, "mdtp"),
      recipient = SecureMessage.Recipient("CDS Financials",
        SecureMessage.TaxIdentifier("HMRC-CUS-ORG", histDoc.currentEori)),
      params = SecureMessage.Params(LocalDate.now(), LocalDate.now(), "Financials"),
      email = "email@email.com",
      tags = SecureMessage.Tags("CDS Financials"),
      content = contents,
      messageType = "newMEssageAlert",
      validForm = LocalDate.now().toString(),
      alertQueue = "DEFAULT"
    )

    val requestDetail = SecureMessage.RequestDetail(EORI(histDoc.currentEori), Option(EORI("")))
    val request = SecureMessage.Request(commonRequest, requestDetail)

    jsonSchemaValidator.validatePayload(requestBody(request), jsonSchemaValidator.ssfnRequestSchema) match {
      case Success(_) =>
        httpClient.POST[SecureMessage.Request, SecureMessage.Response](
          appConfig.secureMessageEndpoint,
          request,
          headers = mdgHeaders.headers(appConfig.secureMessageBearerToken,
            appConfig.secureMessageHostHeader)
        )(implicitly, implicitly, HeaderCarrier(), implicitly)

    }
  }

  private def requestBody(request: SecureMessage.Request): JsValue = Json.toJson(request)

}
