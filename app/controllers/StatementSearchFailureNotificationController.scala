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

import controllers.actions.{AuthorizationHeaderFilter, MdgHeaderFilter}
import models.requests.StatementSearchFailureNotificationRequest.ssfnRequestFormat
import models.responses._
import models.{HistoricDocumentRequestSearch, SearchResultStatus}
import play.api.libs.json.{JsError, JsSuccess, JsValue, Json}
import play.api.mvc.{Action, ControllerComponents, Request}
import services.cache.HistoricDocumentRequestSearchCacheService
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController
import utils.JSONSchemaValidator
import utils.Utils.{currentDateTimeAsRFC7231, writable}

import java.time.LocalDateTime
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

class StatementSearchFailureNotificationController @Inject()(
                                                              cc: ControllerComponents,
                                                              jsonSchemaValidator: JSONSchemaValidator,
                                                              authorizationHeaderFilter: AuthorizationHeaderFilter,
                                                              mdgHeaderFilter: MdgHeaderFilter,
                                                              cacheService: HistoricDocumentRequestSearchCacheService
                                                            )(implicit execution: ExecutionContext)
  extends BackendController(cc) {

  private val logger = play.api.Logger(getClass)

  def processNotification(): Action[JsValue] = (authorizationHeaderFilter andThen mdgHeaderFilter)(parse.json) {
    implicit request =>
      jsonSchemaValidator.validatePayload(request.body, jsonSchemaValidator.ssfnRequestSchema) match {
        case Success(_) =>
          processStatementReqId(request)
          NoContent

        case Failure(errors) =>
          import StatementSearchFailureNotificationErrorResponse.ssfnErrorResponseFormat
          BadRequest(buildErrorResponse(errors, request.headers.get("X-Correlation-ID").getOrElse(
            "Correlation-ID is missing")))
      }
  }

  private def processStatementReqId(request: Request[JsValue]) = {
    Json.fromJson(request.body) match {
      case JsSuccess(value, _) => {
        val statementRequestID = value.StatementSearchFailureNotificationMetadata.statementRequestID
        val failureReasonCode = value.StatementSearchFailureNotificationMetadata.reason

        logger.info(s"Request has been successfully validated for statementRequestID" +
          s" ::: $statementRequestID with reason :: $failureReasonCode")

        updateHistoricDocumentRequestSearchForStatReqId(statementRequestID, failureReasonCode)
      }
      case JsError(_) => logger.error("Request is not properly formed and failing in parsing")
    }
  }

  private def buildErrorResponse(errors: Throwable,
                                 correlationId: String) = {
    val errorResponse = StatementSearchFailureNotificationErrorResponse(errors, correlationId)

    jsonSchemaValidator.validatePayload(Json.toJson(errorResponse), jsonSchemaValidator.ssfnErrorResponseSchema) match {
      case Success(_) => errorResponse
      case _ =>
        StatementSearchFailureNotificationErrorResponse(ErrorDetail(
          currentDateTimeAsRFC7231(LocalDateTime.now()),
          correlationId,
          ErrorCode.code500,
          ErrorMessage.badRequestReceived,
          ErrorSource.cdsFinancials,
          SourceFaultDetail(Seq("JSON validation failed for the request"))))
    }
  }

  private def updateHistoricDocumentRequestSearchForStatReqId(statementRequestID: String,
                                                              failureReasonCode: String): Future[Option[Unit]] =
    for {
      optHistDocReqSearchDoc <- cacheService.retrieveHistDocRequestSearchDocForStatementReqId(statementRequestID)
      updatedHistDoc: Option[HistoricDocumentRequestSearch] <- updateSearchRequestIfInProcess(statementRequestID,
        failureReasonCode, optHistDocReqSearchDoc)
    } yield {
      updatedHistDoc.map {
        histDoc => {
          if (!(histDoc.resultsFound == SearchResultStatus.yes)) cacheService.updateResultsFoundStatus(histDoc)
          else ()
        }
      }
    }

  /**
   * Updates the SearchRequest for given statementRequestID
   * if it is inProcess (searchRequests.statementRequestID field in the Mongo document)
   */
  private def updateSearchRequestIfInProcess(statementRequestID: String,
                                             failureReasonCode: String,
                                             optHistDocReqSearchDoc: Option[HistoricDocumentRequestSearch])
  : Future[Option[HistoricDocumentRequestSearch]] =
    if (isSearchRequestIsInProcess(optHistDocReqSearchDoc, statementRequestID))
      cacheService.updateSearchRequestForStatementRequestId(
        optHistDocReqSearchDoc.get,
        statementRequestID,
        failureReasonCode).map {
        updatedDoc => logErrorMessageIfUpdateFails(statementRequestID, failureReasonCode, updatedDoc)
      }
    else
      Future(None)

  private def isSearchRequestIsInProcess(optHistDocReqSearchDoc: Option[HistoricDocumentRequestSearch],
                                         statementRequestID: String) =
    optHistDocReqSearchDoc.fold(false)(
      histReqSearchDoc => histReqSearchDoc.searchRequests.find(
        sr => sr.statementRequestId == statementRequestID).fold(false)(
        serReq => serReq.searchSuccessful == SearchResultStatus.inProcess))

  private def logErrorMessageIfUpdateFails(statementRequestID: String,
                                           failureReasonCode: String,
                                           updatedDoc: Option[HistoricDocumentRequestSearch]):
  Option[HistoricDocumentRequestSearch] =
    if (updatedDoc.isEmpty) {
      logger.error(s"update failed for statementRequestID :: $statementRequestID" +
        s" and reasonCode :: $failureReasonCode")
      None
    } else {
      updatedDoc
    }
}
