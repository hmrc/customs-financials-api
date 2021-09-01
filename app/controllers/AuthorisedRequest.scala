/*
 * Copyright 2021 HM Revenue & Customs
 *
 */

package controllers

import config.AppConfig
import models.EORI
import play.api.mvc._
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.auth.core.authorise.EmptyPredicate
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals
import uk.gov.hmrc.http.{CorePost, HeaderCarrier, HttpClient}
import uk.gov.hmrc.play.http.HeaderCarrierConverter

import javax.inject.Inject
import scala.concurrent.Future.successful
import scala.concurrent.{ExecutionContext, Future}

class AuthorisedRequest @Inject()(override val authConnector: CustomAuthConnector, cc: ControllerComponents)(implicit val executionContext: ExecutionContext)
  extends ActionBuilder[RequestWithEori, AnyContent] with ActionRefiner[Request, RequestWithEori] with AuthorisedFunctions with Results {
  override protected def refine[A](request: Request[A]): Future[Either[Result, RequestWithEori[A]]] = {
    implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromRequest(request)

    val predicates = EmptyPredicate
    val retrievals = Retrievals.allEnrolments

    authConnector.authorise(predicates, retrievals)
      .map(_.getEnrolment("HMRC-CUS-ORG").flatMap(_.getIdentifier("EORINumber")))
      .map {
        case Some(eori) =>
          Right(new RequestWithEori(EORI(eori.value), request))
        case None =>
          Left(Forbidden("Enrolment Identifier EORINumber not found"))
      }
  }

  override def parser: BodyParser[AnyContent] = cc.parsers.defaultBodyParser
}

class RequestWithEori[+A](val eori: EORI, request: Request[A]) extends WrappedRequest[A](request)

class CustomAuthConnector @Inject()(appConfig: AppConfig,
                                    httpPost: HttpClient) extends PlayAuthConnector {
  val serviceUrl: String = appConfig.authUrl

  def http: CorePost = httpPost
}

trait ControllerChecks extends Results {
  def matchingEoriNumber(eori: EORI)(fn: EORI => Future[Result])(implicit request: RequestWithEori[_]): Future[Result] = {
    val eoriRetrievedFromAuth = request.eori.value
    if (eoriRetrievedFromAuth == eori.value) fn(eori) else successful(Forbidden(s"Enrolment Identifier EORINumber $eoriRetrievedFromAuth not matched with ${eori.value}"))
  }
}
