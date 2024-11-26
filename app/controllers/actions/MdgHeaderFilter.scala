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

package controllers.actions

import _root_.config.AppConfig
import play.api.mvc.*
import play.api.mvc.Results.{BadRequest, Unauthorized}
import utils.Utils.{comma, emptyString, iso8601DateFormatter}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

class MdgHeaderDefaultFilter @Inject()(val parser: BodyParsers.Default,
                                       appConfig: AppConfig)
                                      (implicit val executionContext: ExecutionContext) extends MdgHeaderFilter {

  private val logger = play.api.Logger(getClass)

  private val dateHeader = "Date"
  private val correlationIdHeader = "X-Correlation-ID"
  private val forwardHostHeader = "X-Forwarded-Host"
  private val contentTypeHeader = "Content-Type"
  private val acceptHeader = "Accept"
  private val authorizationHeader = "Authorization"

  override protected def refine[A](request: Request[A]): Future[Either[Result, Request[A]]] = {
    Future.successful(
      for {
        requestWithMandatoryHeaders <- checkForMissingHeaders(request)
        requestWithValidAccept <- validateAcceptHeader(requestWithMandatoryHeaders)
        requestWithValidContentType <- validateContentTypeHeader(requestWithValidAccept)
        requestWithValidDate <- validateRequestDate(requestWithValidContentType)
        requestWithValidCorrelationId <- validateCorrelationId(requestWithValidDate)
        requestWithValidForwardHost <- validateForwardHost(requestWithValidCorrelationId)
      } yield requestWithValidForwardHost
    )
  }

  private def checkForMissingHeaders[A](request: Request[A]): Either[Result, Request[A]] = {
    val mandatoryHeaders =
      List(dateHeader, correlationIdHeader, forwardHostHeader, contentTypeHeader, acceptHeader, authorizationHeader)

    val missingHeaders = mandatoryHeaders.filterNot(request.headers.get(_).isDefined)

    missingHeaders match {
      case Nil => Right(request)
      case _ =>
        logger.error(s"Missing header(s): ${missingHeaders.mkString(comma)}")
        Left(BadRequest)
    }
  }

  private def validateAcceptHeader[A](request: Request[A]): Either[Result, Request[A]] = {
    request.headers.get(acceptHeader).map(_.toLowerCase == "application/json") match {
      case Some(false) =>
        logger.error("Accept header must be application/json")
        Left(BadRequest)
      case _ => Right(request)
    }
  }

  private def validateContentTypeHeader[A](request: Request[A]): Either[Result, Request[A]] = {
    request.headers.get(contentTypeHeader).map(_.toLowerCase == "application/json") match {
      case Some(false) =>
        logger.error("Content-Type header must be application/json")
        Left(BadRequest)
      case _ => Right(request)
    }
  }

  private def validateCorrelationId[A](request: Request[A]): Either[Result, Request[A]] = {
    val MAX_CORRELATION_ID_LENGTH = 36
    request.headers.get(correlationIdHeader).map(_.length) match {
      case Some(length) if length > MAX_CORRELATION_ID_LENGTH =>
        logger.error("header.*X-Correlation-ID exceeds 36 characters")
        Left(BadRequest)
      case _ => Right(request)
    }
  }

  private def validateRequestDate[A](request: Request[A]): Either[Result, Request[A]] = {
    Try(request.headers.get(dateHeader).map(iso8601DateFormatter.parse(_))).toOption.flatten match {
      case Some(_) => Right(request)
      case None =>
        logger.error("Date header has invalid format")
        Left(BadRequest)
    }
  }

  private def validateForwardHost[A](request: Request[A]): Either[Result, Request[A]] = {
    request.headers.get(forwardHostHeader).map(_.toLowerCase == appConfig.ssfnForwardedHost.getOrElse(
      emptyString).toLowerCase) match {
      case Some(false) =>
        logger.error(s"$forwardHostHeader has invalid value")
        Left(Unauthorized)
      case _ => Right(request)
    }
  }
}

trait MdgHeaderFilter
  extends ActionBuilder[Request, AnyContent]
    with ActionRefiner[Request, Request]
