/*
 * Copyright 2021 HM Revenue & Customs
 *
 */

package controllers.definition

import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.FakeRequest
import play.api.test.Helpers._
import utils.SpecBase

class DocumentationControllerSpec extends SpecBase {

  "DocumentController" should {
    "return controllers.definition view" in {

      val app: Application = GuiceApplicationBuilder().configure(
        "microservice.metrics.enabled" -> false,
        "metrics.enabled" -> false,
        "auditing.enabled" -> false
      ).build()

      val view = views.txt.definition().toString

      running(app){
        val result = route(app, FakeRequest(GET, controllers.definition.routes.DocumentationController.definition().url)).value
        contentAsString(result) mustBe view
      }
    }
  }
}
