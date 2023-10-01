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

import connectors.SecureMessageConnector
import controllers.actions.{AuthorizationHeaderFilter, MdgHeaderFilter}
import models.requests.HistoricDocumentRequest
import models.requests.StatementSearchFailureNotificationRequest.ssfnRequestFormat
import models.responses._
import models.{HistoricDocumentRequestSearch, SearchResultStatus}
import play.api.libs.json.{JsError, JsSuccess, JsValue, Json}
import play.api.mvc._
import services.HistoricDocumentService
import services.cache.HistoricDocumentRequestSearchCacheService
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController
import utils.JSONSchemaValidator
import utils.Utils.{currentDateTimeAsRFC7231, emptyString, writable}

import java.time.LocalDateTime
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

class StatementSearchFailureNotificationController @Inject()(
                                                              cc: ControllerComponents,
                                                              jsonSchemaValidator: JSONSchemaValidator,
                                                              authorizationHeaderFilter: AuthorizationHeaderFilter,
                                                              mdgHeaderFilter: MdgHeaderFilter,
                                                              cacheService: HistoricDocumentRequestSearchCacheService,
                                                              histDocumentService: HistoricDocumentService,
                                                              smc: SecureMessageConnector
                                                            )(implicit execution: ExecutionContext)
  extends BackendController(cc) {

  private val logger = play.api.Logger(getClass)

  def processNotification(): Action[AnyContent] = (authorizationHeaderFilter andThen mdgHeaderFilter).async {
    request =>
      jsonSchemaValidator.validatePayload(requestBody(request), jsonSchemaValidator.ssfnRequestSchema) match {
        case Success(_) =>
          processStatementReqId(requestBody(request), correlationId(request))(hc(request))

        case Failure(errors) =>
          import StatementSearchFailureNotificationErrorResponse.ssfnErrorResponseFormat
          Future(BadRequest(buildErrorResponse(Option(errors), ErrorCode.code400, correlationId(request))))
      }
  }

  private def processStatementReqId(request: JsValue,
                                    correlationId: String)(implicit hc: HeaderCarrier): Future[Result] = {
    Json.fromJson(request) match {
      case JsSuccess(value, _) =>
        val statementRequestID = value.StatementSearchFailureNotificationMetadata.statementRequestID
        val failureReasonCode = value.StatementSearchFailureNotificationMetadata.reason

        logger.info(s"Request has been successfully validated for statementRequestID" +
          s" ::: $statementRequestID with reason :: $failureReasonCode")

        checkStatementReqIdInDBAndProcess(statementRequestID, correlationId, failureReasonCode)

      case JsError(_) =>
        logger.error("Request is not properly formed and failing in parsing")
        Future(BadRequest)
    }
  }

  private def buildErrorResponse(errors: Option[Throwable] = None,
                                 errorCode: String = ErrorCode.code400,
                                 correlationId: String,
                                 statementReqId: Option[String] = None) = {
    val errorResponse = StatementSearchFailureNotificationErrorResponse(errors, errorCode, correlationId, statementReqId)

    jsonSchemaValidator.validatePayload(Json.toJson(errorResponse), jsonSchemaValidator.ssfnErrorResponseSchema) match {
      case Success(_) => errorResponse
      case _ =>
        StatementSearchFailureNotificationErrorResponse(ErrorDetail(
          currentDateTimeAsRFC7231(LocalDateTime.now()),
          correlationId,
          ErrorCode.code400,
          ErrorMessage.badRequestReceived,
          ErrorSource.cdsFinancials,
          SourceFaultDetail(Seq(ErrorMessage.badRequestReceived))))
    }
  }

  /**
   * Checks whether request's statementRequestID is present in the DB
   * Process statementRequestId if found in the DB
   * otherwise reply with BAD_REQUEST error response
   */
  private def checkStatementReqIdInDBAndProcess(statementRequestID: String,
                                                correlationId: String,
                                                failureReasonCode: String)(implicit hc: HeaderCarrier): Future[Result] = {
    for {
      optHistDocReq <- cacheService.retrieveHistDocRequestSearchDocForStatementReqId(statementRequestID)
      result: Result <- if (optHistDocReq.isEmpty) {
        Future(BadRequest(buildErrorResponse(
          errors = None,
          errorCode = ErrorCode.code400,
          correlationId = correlationId,
          Option(statementRequestID))))
      } else {
        if (failureReasonCode != "NoDocumentsFound")
          Future(updateRetryCountAndSendRequest(correlationId, statementRequestID, failureReasonCode, optHistDocReq.get))
        else
          updateHistoricDocumentRequestSearchForStatReqId(
            correlationId, statementRequestID, failureReasonCode, optHistDocReq.get)
      }
    } yield {
      result
    }
  }

  private def updateHistoricDocumentRequestSearchForStatReqId(correlationId: String,
                                                              statementRequestID: String,
                                                              failureReasonCode: String,
                                                              optHistDocReqSearchDoc: HistoricDocumentRequestSearch)(
                                                               implicit hc: HeaderCarrier): Future[Result] = {
    for {
      updatedHistDoc <- updateSearchRequestIfInProcess(statementRequestID,
        failureReasonCode, optHistDocReqSearchDoc)
    } yield {
      updatedHistDoc match {
        case Some(histDoc) =>
          histDoc.resultsFound match {
            case SearchResultStatus.inProcess => {
              updateDocStatusToNoIfEligibleAndSendSecureMessage(histDoc)
              NoContent
            }
            case _ =>
              logger.info("Document status in not inProcess hence no further processing required")
              NoContent
          }
        case _ => NoContent
      }
    }
  }

  /**
   * Updates the resultsFound status to no if eligible and sends secure message
   */
  private def updateDocStatusToNoIfEligibleAndSendSecureMessage(histDoc: HistoricDocumentRequestSearch): Future[Unit] = {
    cacheService.updateResultsFoundStatusToNoIfEligible(histDoc).map {
      case Some(updatedDoc) =>
        if (updatedDoc.resultsFound == SearchResultStatus.no) {
          smc.sendSecureMessage(updatedDoc).recover {
            case exception =>
              logger.error(s"secure message could not be sent due to error::: ${exception.getMessage}")
          }
          logger.info("secure message has been triggered")
        } else {
          logger.info("Not eligible to send secure message")
        }
      case _ =>
        logger.info("Not eligible to send secure message")
    }
  }

  /**
   * Updates the SearchRequest for given statementRequestID
   * if it is inProcess (searchRequests.statementRequestID field in the Mongo document)
   */
  private def updateSearchRequestIfInProcess(statementRequestID: String,
                                             failureReasonCode: String,
                                             optHistDocReqSearchDoc: HistoricDocumentRequestSearch)
  : Future[Option[HistoricDocumentRequestSearch]] =
    if (isSearchRequestIsInProcess(optHistDocReqSearchDoc, statementRequestID))
      cacheService.updateSearchRequestForStatementRequestId(
        optHistDocReqSearchDoc,
        statementRequestID,
        failureReasonCode).map {
        updatedDoc => logErrorMessageIfUpdateFails(statementRequestID, failureReasonCode, updatedDoc)
      }
    else
      Future(None)

  private def updateRetryCountAndSendRequest(correlationId: String,
                                             statementRequestID: String,
                                             failureReasonCode: String,
                                             histDocReqSearchDoc: HistoricDocumentRequestSearch
                                            )(implicit hc: HeaderCarrier) = {
    val isReqRetryCountBelowMax = histDocReqSearchDoc.searchRequests.find(
      sReq => sReq.statementRequestId == statementRequestID).fold(false)(sr => sr.failureRetryCount < 5)

    if (isReqRetryCountBelowMax) {
      for {
        optHistDoc <- cacheService.updateSearchRequestRetryCount(
          statementRequestID,
          failureReasonCode,
          histDocReqSearchDoc.searchID.toString,
          histDocReqSearchDoc.searchRequests)
      } yield {
        histDocumentService.sendHistoricDocumentRequest(
          HistoricDocumentRequest(statementRequestID, optHistDoc.get))
      }
      NoContent
    }
    else InternalServerError(buildInternalServerErrorResponse(correlationId, statementRequestID))
  }

  private def buildInternalServerErrorResponse(correlationId: String,
                                               statementRequestID: String) =
    buildErrorResponse(
      errorCode = ErrorCode.code500,
      correlationId = correlationId,
      statementReqId = Some(statementRequestID))

  private def isSearchRequestIsInProcess(optHistDocReqSearchDoc: HistoricDocumentRequestSearch,
                                         statementRequestID: String): Boolean =
    optHistDocReqSearchDoc.searchRequests.find(
      sr => sr.statementRequestId == statementRequestID).fold(false)(
      serReq => serReq.searchSuccessful == SearchResultStatus.inProcess)

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

  private def requestBody(request: Request[AnyContent]): JsValue =
    request.body.asJson.getOrElse(Json.toJson(emptyString))

  private def correlationId(request: Request[AnyContent]): String =
    request.headers.get("X-Correlation-ID").getOrElse("Correlation-ID is missing")
}
