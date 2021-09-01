/*
 * Copyright 2021 HM Revenue & Customs
 *
 */

package controllers

import connectors.DataStoreConnector
import models.requests.HistoricDocumentRequest
import models.{EORI, FileRole}
import play.api.libs.json.{JsValue, Json, OFormat}
import play.api.{Logger, LoggerLike}
import play.api.mvc.{Action, ControllerComponents}
import services.HistoricDocumentService
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import java.time.LocalDate
import javax.inject.Inject
import scala.concurrent._

class HistoricDocumentRequestController @Inject()(service: HistoricDocumentService,
                                                  dataStoreService: DataStoreConnector,
                                                  authorisedRequest: AuthorisedRequest,
                                                  cc: ControllerComponents)(implicit ec: ExecutionContext) extends BackendController(cc) {

  val log: LoggerLike = Logger(this.getClass)

  def makeRequest( ): Action[JsValue] = authorisedRequest.async(parse.json) { implicit request: RequestWithEori[JsValue] =>

    withJsonBody[RequestForHistoricDocuments] { frontEndRequest =>
      for {
        historicEoris <- dataStoreService.getEoriHistory(request.eori)
        allEoris = historicEoris.toSet + request.eori
        historicDocumentRequests = allEoris.map(frontEndRequest.toHistoricDocumentRequest)
        result <- Future.sequence(historicDocumentRequests.map(service.sendHistoricDocumentRequest))
      } yield {
        log.info(s"Historic Documents requested ${allEoris.size}")
        if (result.contains(false)) ServiceUnavailable else NoContent
      }
    }
  }

}

case class RequestForHistoricDocuments(
                                        documentType: FileRole,
                                        from: LocalDate,
                                        until: LocalDate,
                                        dan: Option[String]
                                      ) {

  def toHistoricDocumentRequest(eori: EORI): HistoricDocumentRequest = {
    new HistoricDocumentRequest(eori, this.documentType, this.from.getYear, this.from.getMonthValue, this.until.getYear, this.until.getMonthValue, dan)
  }
}

object RequestForHistoricDocuments {
  implicit val requestForHistoricDocumentsFormat: OFormat[RequestForHistoricDocuments] = Json.format[RequestForHistoricDocuments]
}

