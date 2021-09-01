/*
 * Copyright 2021 HM Revenue & Customs
 *
 */

package connectors

import config.AppConfig
import domain.acc38
import domain.acc38.GetCorrespondenceAddressRequest
import models.{AccountNumber, AccountType, EORI}
import services.DateTimeService
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient, RequestId}
import uk.gov.hmrc.http.HttpReads.Implicits._

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class Acc38Connector @Inject()(httpClient: HttpClient,
                               appConfig: AppConfig,
                               dateTimeService: DateTimeService,
                               mdgHeaders: MdgHeaders)(implicit executionContext: ExecutionContext) {

  def getAccountContactDetails(dan: AccountNumber, eori: EORI, requestId: Option[RequestId]): Future[acc38.Response] = {

    val commonRequest = acc38.RequestCommon(
      receiptDate = dateTimeService.currentDateTimeAsIso8601,
      acknowledgementReference = mdgHeaders.acknowledgementReference(requestId),
      originatingSystem = "Digital"
    )

    val request = acc38.Request(
      GetCorrespondenceAddressRequest(
        commonRequest,
        acc38.RequestDetail(
          eori,
          acc38.AccountDetails(AccountType("DutyDeferment"), dan),
          None
        )
      )
    )

    httpClient.POST[acc38.Request, acc38.Response](
      appConfig.acc38DutyDefermentContactDetailsEndpoint,
      request,
      headers = mdgHeaders.headers(appConfig.acc38BearerToken, requestId, appConfig.acc38HostHeader)
    )(implicitly, implicitly, HeaderCarrier(), implicitly)
  }

}
