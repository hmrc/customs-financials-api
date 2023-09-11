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

package controllers

import connectors.DataStoreConnector
import models.requests.HistoricDocumentRequest
import models.{EORI, FileRole, HistoricDocumentRequestSearch}
import play.api.libs.json.{JsValue, Json, OFormat}
import play.api.mvc.{Action, ControllerComponents, Result}
import play.api.{Logger, LoggerLike}
import services.HistoricDocumentService
import services.cache.HistoricDocumentRequestSearchCacheService
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import java.time.LocalDate
import javax.inject.Inject
import scala.concurrent._

class HistoricDocumentRequestController @Inject()(service: HistoricDocumentService,
                                                  dataStoreService: DataStoreConnector,
                                                  authorisedRequest: AuthorisedRequest,
                                                  histDocRequestCacheService: HistoricDocumentRequestSearchCacheService,
                                                  cc: ControllerComponents)(implicit ec: ExecutionContext)
  extends BackendController(cc) {

  val log: LoggerLike = Logger(this.getClass)

  def makeRequest(): Action[JsValue] = authorisedRequest.async(parse.json) {
    implicit request: RequestWithEori[JsValue] =>

      withJsonBody[RequestForHistoricDocuments] { frontEndRequest =>
        for {
          historicEoris <- dataStoreService.getEoriHistory(request.eori)
          //userId <- internalId
          allEoris = historicEoris.toSet + request.eori
          historicDocumentRequests: Set[HistoricDocumentRequest] = allEoris.map(frontEndRequest.toHistoricDocumentRequest)
          result <- Future.sequence(historicDocumentRequests.map(service.sendHistoricDocumentRequest))
        } yield {
          log.info(s"Historic Documents requested ${allEoris.size}")
          if (result.contains(false))
            ServiceUnavailable
          else saveHistoricDocRequestsAndReturnNoContent(request, historicDocumentRequests)
        }
      }
  }

  private def saveHistoricDocRequestsAndReturnNoContent(request: RequestWithEori[JsValue],
                                                        historicDocRequests: Set[HistoricDocumentRequest]): Result = {
    saveHistoricDocRequests(
      historicDocRequests,
      request.eori.value,
      histDocRequestCacheService).map(identity)

    NoContent
  }

  private def saveHistoricDocRequests(historicDocumentRequests: Set[HistoricDocumentRequest],
                                      requestEori: String,
                                      histDocRequestCacheService: HistoricDocumentRequestSearchCacheService):
  Future[Boolean] = {
    val histDocRequestSearch = HistoricDocumentRequestSearch.from(historicDocumentRequests, requestEori)
    histDocRequestCacheService.saveHistoricDocumentRequestSearch(histDocRequestSearch)
  }

  /*private def internalId()(implicit hc: HeaderCarrier): Future[String] =
    authorisedRequest.authorised().retrieve(Retrievals.internalId) {
      case Some(internalId) => Future.successful(internalId)
      case _ => Future.successful(emptyString)
    }
}*/
}

case class RequestForHistoricDocuments(
                                        documentType: FileRole,
                                        from: LocalDate,
                                        until: LocalDate,
                                        dan: Option[String]
                                      ) {

  def toHistoricDocumentRequest(eori: EORI): HistoricDocumentRequest = {
    new HistoricDocumentRequest(
      eori,
      this.documentType,
      this.from.getYear,
      this.from.getMonthValue,
      this.until.getYear,
      this.until.getMonthValue,
      dan)
  }
}

object RequestForHistoricDocuments {
  implicit val requestForHistoricDocumentsFormat: OFormat[RequestForHistoricDocuments] =
    Json.format[RequestForHistoricDocuments]
}

