/*
 * Copyright 2021 HM Revenue & Customs
 *
 */

package connectors

import config.AppConfig
import domain.acc37.{AccountDetails, AmendCorrespondenceAddressRequest}
import models.{AccountNumber, AccountType, EORI}
import services.DateTimeService
import uk.gov.hmrc.http.HttpReads.Implicits._
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient, RequestId}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class Acc37Connector @Inject()(httpClient: HttpClient,
                               appConfig: AppConfig,
                               dateTimeService: DateTimeService,
                               headers: MdgHeaders)(implicit executionContext: ExecutionContext) {

  def updateAccountContactDetails(dan: AccountNumber, eori: EORI, contactInformation: domain.acc37.ContactDetails, requestId: Option[RequestId]): Future[domain.acc37.Response] = {

    val request = domain.acc37.Request(
      AmendCorrespondenceAddressRequest(
        domain.acc37.RequestCommon("Digital", dateTimeService.currentDateTimeAsIso8601, headers.acknowledgementReference(requestId)),
        domain.acc37.RequestDetail(eori, AccountDetails(AccountType("DutyDeferment"), dan), contactInformation, None)
      ))

    httpClient.POST[domain.acc37.Request, domain.acc37.Response](
      appConfig.acc37UpdateAccountContactDetailsEndpoint,
      request,
      headers = headers.headers(appConfig.acc37BearerToken, requestId, appConfig.acc37HostHeader)
    )(implicitly, implicitly, HeaderCarrier(), implicitly)
  }
}
