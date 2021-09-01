/*
 * Copyright 2021 HM Revenue & Customs
 *
 */

package services

import connectors._
import models.requests.HistoricDocumentRequest
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.{Inject, Singleton}
import scala.concurrent.Future

@Singleton
class HistoricDocumentService @Inject()(acc24Connector: Acc24Connector,
                                        auditingService: AuditingService) {
  def sendHistoricDocumentRequest(historicDocumentRequest: HistoricDocumentRequest)(implicit hc: HeaderCarrier): Future[Boolean] = {
    auditingService.auditHistoricStatementRequest(historicDocumentRequest)
    acc24Connector.sendHistoricDocumentRequest(historicDocumentRequest, hc.requestId)
  }
}
