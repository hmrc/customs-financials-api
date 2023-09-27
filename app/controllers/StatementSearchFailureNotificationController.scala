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
import connectors.SecureMessageConnector
import models.requests.StatementSearchFailureNotificationRequest.ssfnRequestFormat
import models.responses._
import models.{HistoricDocumentRequestSearch, SearchResultStatus}
import play.api.libs.json.{JsError, JsSuccess, JsValue, Json}
import play.api.mvc._
import services.cache.HistoricDocumentRequestSearchCacheService
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
                                                              smc: SecureMessageConnector
                                                            )(implicit execution: ExecutionContext)
  extends BackendController(cc) {

  private val logger = play.api.Logger(getClass)

  def processNotification(): Action[AnyContent] = (authorizationHeaderFilter andThen mdgHeaderFilter).async {
    request =>
      jsonSchemaValidator.validatePayload(requestBody(request), jsonSchemaValidator.ssfnRequestSchema) match {
        case Success(_) =>
          processStatementReqId(requestBody(request), correlationId(request))

        case Failure(errors) =>
          import StatementSearchFailureNotificationErrorResponse.ssfnErrorResponseFormat
          Future(BadRequest(buildErrorResponse(Option(errors), correlationId(request))))
      }
  }

  private def processStatementReqId(request: JsValue,
                                    correlationId: String): Future[Result] = {
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
                                 correlationId: String,
                                 statementReqId: Option[String] = None) = {
    val errorResponse = StatementSearchFailureNotificationErrorResponse(errors, correlationId, statementReqId)

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
                                            failureReasonCode: String): Future[Result] = {
    cacheService.retrieveHistDocRequestSearchDocForStatementReqId(statementRequestID).map {
      case None => BadRequest(buildErrorResponse(
        errors = None,
        correlationId = correlationId,
        Option(statementRequestID)))

      case optHistDocReqSearchDoc =>
        updateHistoricDocumentRequestSearchForStatReqId(
          statementRequestID, failureReasonCode, optHistDocReqSearchDoc)
        NoContent
    }
  }

  private def updateHistoricDocumentRequestSearchForStatReqId(statementRequestID: String,
    failureReasonCode: String,
    optHistDocReqSearchDoc: Option[
      HistoricDocumentRequestSearch]): Future[Option[Unit]] =
    for {
      updatedHistDoc <- updateSearchRequestIfInProcess(statementRequestID,
        failureReasonCode, optHistDocReqSearchDoc)
    } yield {
      updatedHistDoc.map {
        histDoc => {
          histDoc.resultsFound match {
            case SearchResultStatus.inProcess => updateDocStatusToNoIfEligibleAndSendSecureMessage(histDoc)
            case _ => logger.info("Document status in not inProcess hence no further processing required")
          }
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

  private def requestBody(request: Request[AnyContent]): JsValue =
    request.body.asJson.getOrElse(Json.toJson(emptyString))

  private def correlationId(request: Request[AnyContent]): String =
    request.headers.get("X-Correlation-ID").getOrElse("Correlation-ID is missing")
}
