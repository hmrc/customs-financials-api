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

import play.api.mvc.Results.BadRequest
import play.api.mvc._

import java.time.format.DateTimeFormatter
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

class MdgHeaderDefaultFilter @Inject()(
                                        val parser: BodyParsers.Default,
                                      )(implicit val executionContext: ExecutionContext)
  extends MdgHeaderFilter {
  override protected def refine[A](request: Request[A]): Future[Either[Result, Request[A]]] = {
    Future.successful(
      for {
        requestWithMandatoryHeaders <- checkForMissingHeaders(request)
        requestWithValidAccept <- validateAcceptHeader(requestWithMandatoryHeaders)
        requestWithValidContentType <- validateContentTypeHeader(requestWithValidAccept)
        requestWithValidDate <- validateRequestDate(requestWithValidContentType)
        requestWithValidCorrelationId <- validateCorrelationId(requestWithValidDate)
      } yield requestWithValidCorrelationId
    )
  }

  // Play 2.6 updates Content-Type key to lower case which means we need to do a case insensitive check
  private def checkForMissingHeaders[A](request: Request[A]): Either[Result, Request[A]] = {
    val mandatoryHeaders = List("Date", "X-Correlation-ID", "X-Forwarded-Host", "Content-Type", "Accept")
    val missingHeaders = mandatoryHeaders.filterNot(request.headers.get(_).isDefined)

    missingHeaders match {
      case Nil => Right(request)
      case _ => Left(BadRequest(s"Missing header(s): ${missingHeaders.mkString(",")}"))
    }
  }

  // HTTP Date format from https://tools.ietf.org/html/rfc7231#section-7.1.1.1
  private val httpDateFormatter = DateTimeFormatter.ofPattern("EEE, dd MMM yyyy HH:mm:ss 'GMT'")

  private def validateAcceptHeader[A](request: Request[A]): Either[Result, Request[A]] = {
    request.headers.get("Accept").map(_.toLowerCase == "application/json") match {
      case Some(false) => Left(BadRequest("Accept header must be application/json"))
      case _ => Right(request)
    }
  }

  private def validateContentTypeHeader[A](request: Request[A]): Either[Result, Request[A]] = {
    request.headers.get("Content-Type").map(_.toLowerCase == "application/json") match {
      case Some(false) => Left(BadRequest("Content-Type header must be application/json"))
      case _ => Right(request)
    }
  }

  private def validateCorrelationId[A](request: Request[A]): Either[Result, Request[A]] = {
    val MAX_CORRELATION_ID_LENGTH = 36
    request.headers.get("X-Correlation-ID").map(_.length) match {
      case Some(length) if length > MAX_CORRELATION_ID_LENGTH => Left(
        BadRequest("header.*X-Correlation-ID exceeds 36 characters"))
      case _ => Right(request)
    }
  }

  private def validateRequestDate[A](request: Request[A]): Either[Result, Request[A]] = {
    Try(request.headers.get("Date").map(httpDateFormatter.parse(_))).toOption.flatten match {
      case Some(_) => Right(request)
      case None => Left(BadRequest("Date header has invalid format"))
    }
  }
}

trait MdgHeaderFilter
  extends ActionBuilder[Request, AnyContent]
    with ActionRefiner[Request, Request]
