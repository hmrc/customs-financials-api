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
import models.FailureReason.NO_DOCUMENTS_FOUND
import models.FailureRetryCount.FINAL_RETRY
import models.requests.HistoricDocumentRequest
import models.requests.StatementSearchFailureNotificationRequest.ssfnRequestFormat
import models.responses.*
import models.responses.ErrorMessage.technicalErrorDetail
import models.{HistoricDocumentRequestSearch, SearchResultStatus}
import play.api.libs.json.{JsError, JsSuccess, JsValue, Json}
import play.api.mvc.*
import services.HistoricDocumentService
import services.cache.HistoricDocumentRequestSearchCacheService
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController
import utils.JSONSchemaValidator
import utils.Utils.{currentDateTimeAsRFC7231, emptyString, writable}

import java.time.LocalDateTime
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

class StatementSearchFailureNotificationController @Inject() (
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

  def processNotification(): Action[AnyContent] = (authorizationHeaderFilter andThen mdgHeaderFilter).async { request =>
    jsonSchemaValidator.validatePayload(requestBody(request), jsonSchemaValidator.ssfnRequestSchema) match {
      case Success(_) =>
        processStatementReqId(requestBody(request), correlationId(request))(hc(request))

      case Failure(errors) =>
        import StatementSearchFailureNotificationErrorResponse.ssfnErrorResponseFormat
        Future(BadRequest(buildErrorResponse(Option(errors), ErrorCode.code400, correlationId(request))))
    }
  }

  private def processStatementReqId(request: JsValue, correlationId: String)(implicit
    hc: HeaderCarrier
  ): Future[Result] =
    Json.fromJson(request) match {
      case JsSuccess(value, _) =>
        val statementRequestID = value.StatementSearchFailureNotificationMetadata.statementRequestID
        val failureReasonCode  = value.StatementSearchFailureNotificationMetadata.reason

        logger.info(
          s"Request has been successfully validated for statementRequestID" +
            s" ::: $statementRequestID with reason :: $failureReasonCode"
        )

        handleExceptionAndProcessStatementRequestId(correlationId, statementRequestID, failureReasonCode)

      case JsError(_) =>
        logger.error("Request is not properly formed and failing in parsing")
        Future(BadRequest)
    }

  private def handleExceptionAndProcessStatementRequestId(
    correlationId: String,
    statementRequestID: String,
    failureReasonCode: String
  )(implicit hc: HeaderCarrier): Future[Result] =
    Try {
      checkStatementReqIdInDBAndProcess(statementRequestID, correlationId, failureReasonCode).recoverWith {
        case exception: Exception =>
          logger.error(
            s"Technical error occurred while processing statementRequestID " +
              s"::: $statementRequestID and error is :: ${exception.getMessage}"
          )

          Future(
            InternalServerError(
              buildInternalServerErrorResponse(
                correlationId,
                statementRequestID,
                technicalErrorDetail(statementRequestID)
              )
            )
          )
      }
    } match {
      case Success(value)     => value
      case Failure(exception) =>
        logger.error(
          s"Technical error occurred while processing statementRequestID " +
            s"::: $statementRequestID and error is :: ${exception.getMessage}"
        )

        Future(
          InternalServerError(
            buildInternalServerErrorResponse(
              correlationId,
              statementRequestID,
              technicalErrorDetail(statementRequestID)
            )
          )
        )
    }

  private def checkStatementReqIdInDBAndProcess(
    statementRequestID: String,
    correlationId: String,
    failureReasonCode: String
  )(implicit hc: HeaderCarrier): Future[Result] =
    for {
      optHistDocReq  <- cacheService.retrieveHistDocRequestSearchDocForStatementReqId(statementRequestID)
      result: Result <- processStatReqIdOrSendErrorResponseIfReqIdNotPresent(
                          statementRequestID,
                          correlationId,
                          failureReasonCode,
                          optHistDocReq
                        )
    } yield result

  private def processStatReqIdOrSendErrorResponseIfReqIdNotPresent(
    statementRequestID: String,
    correlationId: String,
    failureReasonCode: String,
    optHistDocReq: Option[HistoricDocumentRequestSearch]
  )(implicit hc: HeaderCarrier): Future[Result] =
    if (optHistDocReq.isEmpty) {
      Future(
        BadRequest(
          buildErrorResponse(
            errors = None,
            errorCode = ErrorCode.code400,
            correlationId = correlationId,
            Option(statementRequestID)
          )
        )
      )
    } else {
      checkFailureReasonAndProcessRequestId(statementRequestID, correlationId, failureReasonCode, optHistDocReq)
    }

  private def checkFailureReasonAndProcessRequestId(
    statementRequestID: String,
    correlationId: String,
    failureReasonCode: String,
    optHistDocReq: Option[HistoricDocumentRequestSearch]
  )(implicit hc: HeaderCarrier): Future[Result] =
    if (failureReasonCode != NO_DOCUMENTS_FOUND) {
      updateRetryCountAndSendRequest(correlationId, statementRequestID, failureReasonCode, optHistDocReq.get)
    } else {
      updateHistoricDocumentRequestSearchForStatReqId(statementRequestID, failureReasonCode, optHistDocReq.get)
    }

  private def updateHistoricDocumentRequestSearchForStatReqId(
    statementRequestID: String,
    failureReasonCode: String,
    optHistDocReqSearchDoc: HistoricDocumentRequestSearch
  ): Future[Result] =
    for {
      updatedHistDoc <- updateSearchRequestIfInProcess(statementRequestID, failureReasonCode, optHistDocReqSearchDoc)
    } yield updatedHistDoc match {
      case Some(histDoc) =>
        updateDocumentStatusIfInProcess(histDoc)
      case _             => NoContent
    }

  private def updateDocStatusToNoIfEligibleAndSendSecureMessage(histDoc: HistoricDocumentRequestSearch): Future[Unit] =
    cacheService.updateResultsFoundStatusToNoIfEligible(histDoc).map {
      case Some(updatedDoc) =>
        if (updatedDoc.resultsFound == SearchResultStatus.no) {
          smc.sendSecureMessage(updatedDoc).recover { case exception =>
            logger.error(s"secure message could not be sent due to error::: ${exception.getMessage}")
            throw exception
          }
          logger.info("secure message has been triggered")
        } else {
          logger.info("Not eligible to send secure message")
        }
      case _                =>
        logger.info("Not eligible to send secure message")
    }

  private def updateDocumentStatusIfInProcess(histDoc: HistoricDocumentRequestSearch): Result = {
    histDoc.resultsFound match {
      case SearchResultStatus.inProcess =>
        updateDocStatusToNoIfEligibleAndSendSecureMessage(histDoc)
      case _                            =>
        logger.info("Document status in not inProcess hence no further processing required")
    }
    NoContent
  }

  private def updateSearchRequestIfInProcess(
    statementRequestID: String,
    failureReasonCode: String,
    optHistDocReqSearchDoc: HistoricDocumentRequestSearch
  ): Future[Option[HistoricDocumentRequestSearch]] =
    if (isSearchRequestIsInProcess(optHistDocReqSearchDoc, statementRequestID)) {
      cacheService
        .updateSearchRequestForStatementRequestId(optHistDocReqSearchDoc, statementRequestID, failureReasonCode)
        .map { updatedDoc =>
          logErrorMessageIfUpdateFails(statementRequestID, failureReasonCode, updatedDoc)
        }
    } else {
      Future(None)
    }

  private def updateRetryCountAndSendRequest(
    correlationId: String,
    statementRequestID: String,
    failureReasonCode: String,
    histDocReqSearchDoc: HistoricDocumentRequestSearch
  )(implicit hc: HeaderCarrier): Future[Result] = {
    val isReqRetryCountBelowMax = histDocReqSearchDoc.searchRequests
      .find(sReq => sReq.statementRequestId == statementRequestID)
      .fold(false)(sr => sr.failureRetryCount < FINAL_RETRY)

    if (isReqRetryCountBelowMax) {
      for {
        optHistDoc <- cacheService.updateSearchRequestRetryCount(
                        statementRequestID,
                        failureReasonCode,
                        histDocReqSearchDoc.searchID.toString,
                        histDocReqSearchDoc.searchRequests
                      )
      } yield histDocumentService
        .sendHistoricDocumentRequest(
          HistoricDocumentRequest(
            statementRequestID,
            optHistDoc.getOrElse(throw new RuntimeException("HistoricDocumentRequestSearch could not be retrieved"))
          )
        )
        .map {
          case true => logger.info("ACC24 request got 204 response")
          case _    => logger.error("ACC24 request did not get 204 response")
        }

      Future(NoContent)
    } else {
      Future(
        InternalServerError(
          buildInternalServerErrorResponse(
            correlationId,
            statementRequestID,
            ErrorMessage.failureRetryCountErrorDetail(statementRequestID)
          )
        )
      )
    }
  }

  private def isSearchRequestIsInProcess(
    optHistDocReqSearchDoc: HistoricDocumentRequestSearch,
    statementRequestID: String
  ): Boolean =
    optHistDocReqSearchDoc.searchRequests
      .find(sr => sr.statementRequestId == statementRequestID)
      .fold(false)(serReq => serReq.searchSuccessful == SearchResultStatus.inProcess)

  private def logErrorMessageIfUpdateFails(
    statementRequestID: String,
    failureReasonCode: String,
    updatedDoc: Option[HistoricDocumentRequestSearch]
  ): Option[HistoricDocumentRequestSearch] =
    if (updatedDoc.isEmpty) {
      logger.error(
        s"update failed for statementRequestID :: $statementRequestID" +
          s" and reasonCode :: $failureReasonCode"
      )
      None
    } else {
      updatedDoc
    }

  private def buildInternalServerErrorResponse(
    correlationId: String,
    statementRequestID: String,
    errorDetailMessage: String
  ) =
    buildErrorResponse(
      errorCode = ErrorCode.code500,
      correlationId = correlationId,
      statementReqId = Some(statementRequestID),
      errorDetailMsg = errorDetailMessage
    )

  private def buildErrorResponse(
    errors: Option[Throwable] = None,
    errorCode: String,
    correlationId: String,
    statementReqId: Option[String] = None,
    errorDetailMsg: String = emptyString
  ) = {
    val errorResponse =
      StatementSearchFailureNotificationErrorResponse(errors, errorCode, correlationId, statementReqId, errorDetailMsg)

    jsonSchemaValidator.validatePayload(Json.toJson(errorResponse), jsonSchemaValidator.ssfnErrorResponseSchema) match {
      case Success(_) => errorResponse
      case _          =>
        StatementSearchFailureNotificationErrorResponse(
          ErrorDetail(
            currentDateTimeAsRFC7231(LocalDateTime.now()),
            correlationId,
            ErrorCode.code400,
            ErrorMessage.badRequestReceived,
            ErrorSource.cdsFinancials,
            SourceFaultDetail(Seq(ErrorMessage.badRequestReceived))
          )
        )
    }
  }

  private def requestBody(request: Request[AnyContent]): JsValue =
    request.body.asJson.getOrElse(Json.toJson(emptyString))

  private def correlationId(request: Request[AnyContent]): String =
    request.headers.get("X-Correlation-ID").getOrElse("Correlation-ID is missing")
}
