/*
 * Copyright 2021 HM Revenue & Customs
 *
 */

package connectors

import config.AppConfig
import play.api.libs.json.{JsObject, JsValue}
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient, RequestId}
import uk.gov.hmrc.http.HttpReads.Implicits._

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class Acc27Connector @Inject()(httpClient: HttpClient,
                               appConfig: AppConfig,
                               mdgHeaders: MdgHeaders)(implicit executionContext: ExecutionContext) {

  def getAccounts(requestBody: JsValue, requestId: Option[RequestId]): Future[JsObject] = {
    httpClient.POST[JsValue, JsObject](
      appConfig.hodsEndpoint,
      requestBody,
      headers = mdgHeaders.headers(appConfig.bearerToken, requestId, None)
    )(implicitly, implicitly, HeaderCarrier(), implicitly)
  }
}
