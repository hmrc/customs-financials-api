/*
 * Copyright 2021 HM Revenue & Customs
 *
 */

package controllers.definition

import play.api.mvc.{Action, AnyContent, ControllerComponents}
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController
import views.txt

import javax.inject.Inject

class DocumentationController @Inject()(cc: ControllerComponents) extends BackendController(cc) {
  def definition(): Action[AnyContent] = Action {
   Ok(txt.definition()).withHeaders("Content-Type" -> "application/json")
  }
}


