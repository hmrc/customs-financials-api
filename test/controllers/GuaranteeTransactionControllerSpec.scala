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

import domain.GuaranteeTransaction
import models.{EORI, ExceededThresholdErrorException, NoAssociatedDataException}
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json
import play.api.mvc.AnyContentAsJson
import play.api.test.Helpers._
import play.api.test._
import play.api.{Application, inject}
import services.GuaranteeTransactionsService
import utils.SpecBase

import scala.concurrent.Future

class GuaranteeTransactionControllerSpec extends SpecBase {
  "GuaranteeTransactionsController" should {
    "delegate to the service and return a list of open guarantee transactions with a 200 status code" in new Setup {
      val guaranteeTransaction = Seq(GuaranteeTransaction("someDate", "mrn", Some("100.00"), Some("UCR"), EORI("Declarant EORI"), EORI("Consignee EORI"), "200.00", Some("300.00"), None, None, Nil))
      when(mockService.retrieveGuaranteeTransactionsSummary(any))
        .thenReturn(Future.successful(Right(guaranteeTransaction)))

      running(app) {
        val result = route(app, summaryRequest).value
        status(result) mustBe OK
        contentAsJson(result) mustBe Json.toJson(guaranteeTransaction)
      }
    }
  }

  "return an error when no gan found in request" in new Setup {
    val request: FakeRequest[AnyContentAsJson] = FakeRequest("POST",
      controllers.routes.GuaranteeTransactionsController.retrieveOpenGuaranteeTransactionsSummary().url)

      .withJsonBody(Json.parse("""{"invalid":"invalid"}"""))

    running(app) {
      val result = route(app, request).value
      status(result) mustBe BAD_REQUEST
      contentAsString(result) mustBe "Invalid GuaranteeAccountTransactionsRequest payload: List((/gan,List(JsonValidationError(List(error.path.missing),ArraySeq()))))"
    }
  }

  "return 413" when {
    "the requested dates request too much data" in new Setup {
      when(mockService.retrieveGuaranteeTransactionsSummary(any))
        .thenReturn(Future.successful(Left(ExceededThresholdErrorException)))

      running(app) {
        val result = route(app, summaryRequest).value
        status(result) mustBe REQUEST_ENTITY_TOO_LARGE
      }
    }
  }

  "return NOT_FOUND" when {
    "the requested dates returns no content" in new Setup {
      when(mockService.retrieveGuaranteeTransactionsSummary(any))
        .thenReturn(Future.successful(Left(NoAssociatedDataException)))

      running(app) {
        val result = route(app, summaryRequest).value
        status(result) mustBe NOT_FOUND
      }
    }
  }

  "return a list of open guarantee transactions details" in new Setup {

    import domain._

    val amountTaxType: Amounts = Amounts(openAmount = Some("600.00"), totalAmount = "800.00", clearedAmount = Some("200.00"), updateDate = "2020-08-01")
    val taxType: TaxType = TaxType(taxType = "taxType1", amounts = amountTaxType)
    val amountTaxTypeGroup: Amounts = Amounts(openAmount = Some("400.00"), totalAmount = "700.00", clearedAmount = Some("300.00"), updateDate = "2020-08-02")
    val taxTypeGroup: TaxTypeGroup = TaxTypeGroup(taxTypeGroup = "B", amountTaxTypeGroup, taxType = taxType)
    val amountsDueDates: Amounts = Amounts(openAmount = Some("450.00"), totalAmount = "600.00", clearedAmount = Some("150.00"), updateDate = "2020-08-03")

    val guaranteeTransactionDetail = Seq(GuaranteeTransaction(
      date = "someDate2",
      movementReferenceNumber = "mrn",
      balance = Some("balance 1"),
      uniqueConsignmentReference = Some("UCR"),
      declarantEori = EORI("Declarant EORI"),
      consigneeEori = EORI("Consignee EORI"),
      originalCharge = "charge 1",
      dischargedAmount = Some("5.12"),
      interestCharge = Some("interest rate 1"),
      c18Reference = Some("C18-Ref1"),
      dueDates = Seq(
        DueDate("dueDate", Some("reason1"), amountsDueDates, Seq(taxTypeGroup))
      )
    ))

    when(mockService.retrieveGuaranteeTransactionsDetail(any))
      .thenReturn(Future.successful(Right(guaranteeTransactionDetail)))

    running(app) {
      val result = route(app, detailRequest).value
      status(result) mustBe OK
      contentAsJson(result) mustBe Json.toJson(guaranteeTransactionDetail)
    }
  }

  "return 413 when the date range selected requests too much data" in new Setup {
    when(mockService.retrieveGuaranteeTransactionsDetail(any))
      .thenReturn(Future.successful(Left(ExceededThresholdErrorException)))

    running(app) {
      val result = route(app, detailRequest).value
      status(result) mustBe REQUEST_ENTITY_TOO_LARGE
    }
  }

  "return NOT_FOUND when the date range selected returns no content" in new Setup {
    when(mockService.retrieveGuaranteeTransactionsDetail(any))
      .thenReturn(Future.successful(Left(NoAssociatedDataException)))

    running(app) {
      val result = route(app, detailRequest).value
      status(result) mustBe NOT_FOUND
    }
  }

  "return an error from retrieveOpenGuaranteeTransactionsDetail when no gan found in request" in new Setup {
    val request: FakeRequest[AnyContentAsJson] = FakeRequest(POST, controllers.routes.GuaranteeTransactionsController.retrieveOpenGuaranteeTransactionsDetail().url)
      .withJsonBody(Json.parse("""{"invalid":"invalid"}"""))

    running(app) {
      val result = route(app, request).value
      status(result) mustBe BAD_REQUEST
      contentAsString(result) mustBe "Invalid GuaranteeAccountTransactionsRequest payload: List((/gan,List(JsonValidationError(List(error.path.missing),ArraySeq()))))"
    }
  }

  trait Setup {
    val mockAuthConnector: CustomAuthConnector = mock[CustomAuthConnector]
    val mockService: GuaranteeTransactionsService = mock[GuaranteeTransactionsService]

    val detailRequest: FakeRequest[AnyContentAsJson] =
      FakeRequest("POST", controllers.routes.GuaranteeTransactionsController.retrieveOpenGuaranteeTransactionsDetail().url)
        .withJsonBody(Json.parse("""{"gan":"gan1"}"""))

    val summaryRequest: FakeRequest[AnyContentAsJson] =
      FakeRequest("POST", controllers.routes.GuaranteeTransactionsController.retrieveOpenGuaranteeTransactionsSummary().url)
        .withJsonBody(Json.parse("""{"gan":"gan1"}"""))

    val app: Application = GuiceApplicationBuilder().overrides(
      inject.bind[GuaranteeTransactionsService].toInstance(mockService)
    ).configure(
      "microservice.metrics.enabled" -> false,
      "metrics.enabled" -> false,
      "auditing.enabled" -> false
    ).build()
  }
}
