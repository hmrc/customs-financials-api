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

import config.AppConfig
import play.api.mvc.*
import play.api.mvc.Results.Unauthorized

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class DefaultAuthorizationHeaderFilter @Inject() (val parser: BodyParsers.Default, appConfig: AppConfig)(implicit
  val executionContext: ExecutionContext
) extends AuthorizationHeaderFilter {

  private val logger = play.api.Logger(getClass)

  override protected def refine[A](request: Request[A]): Future[Either[Result, Request[A]]] =
    Future.successful(for {
      requestWithAuthorizationHeader <- validateAuthorization(request)
    } yield requestWithAuthorizationHeader)

  private def validateAuthorization[A](request: Request[A]): Either[Result, Request[A]] =
    request.headers.get("Authorization") match {
      case Some(s"${appConfig.bearerTokenValuePrefix} ${appConfig.ssfnBearerToken}") => Right(request)
      case _                                                                         =>
        logger.error("Invalid Authorization token")
        Left(Unauthorized)
    }
}

trait AuthorizationHeaderFilter extends ActionBuilder[Request, AnyContent] with ActionRefiner[Request, Request]
