/*
 * Copyright 2021 HM Revenue & Customs
 *
 */

package services

import connectors.Acc28Connector
import domain.{Amounts, GuaranteeTransaction}
import models.requests.GuaranteeAccountTransactionsRequest
import models.responses.{DefAmounts, GuaranteeTransactionDeclaration, TaxType, TaxTypeGroup}
import models.{AccountNumber, EORI}
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.Helpers._
import play.api.{Application, inject}
import uk.gov.hmrc.http.HeaderCarrier
import utils.SpecBase

import scala.concurrent.{ExecutionContext, Future}

class GuaranteeTransactionsServiceSpec extends SpecBase {

  "retrieveGuaranteeTransactionsSummary" should {
    "return domain summary for a successful acc28 response" in new Setup {
      when(mockAcc28Connector.retrieveGuaranteeTransactions(request, None))
        .thenReturn(Future.successful(Right(Seq(declaration))))

      running(app) {
        val result = await(service.retrieveGuaranteeTransactionsSummary(request))
        result mustBe Right(List(GuaranteeTransaction("date", "id", None, None, EORI("someEori"), EORI("someOtherEori"), "10000", None, None, None, List.empty)))
      }
    }
  }

  "retrieveGuaranteeTransactionsDetail" should {
    "return domain detail for a successful acc28 response" in new Setup {

      when(mockAcc28Connector.retrieveGuaranteeTransactions(request, None))
        .thenReturn(Future.successful(Right(Seq(declaration))))

      running(app) {
        val result = await(service.retrieveGuaranteeTransactionsDetail(request))
        result mustBe Right(List(GuaranteeTransaction("date", "id", None, None, EORI("someEori"), EORI("someOtherEori"), "10000", None, None, None,
          List(domain.DueDate("date", None, Amounts(None, "10000", Some("9000"), "date"),
            List(domain.TaxTypeGroup("a1", Amounts(None, "10000", None, "date"),
              domain.TaxType("type", Amounts(None, "10000", None, "date")))))))))
      }

    }
  }

  trait Setup {
    implicit val hc: HeaderCarrier = HeaderCarrier()
    implicit val executionContext: ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global

    val mockAcc28Connector: Acc28Connector = mock[Acc28Connector]

    val declaration: GuaranteeTransactionDeclaration = GuaranteeTransactionDeclaration(
      "id",
      "date",
      None,
      EORI("someEori"),
      EORI("someOtherEori"),
      DefAmounts(None, "10000", None, "date"),
      None,
      None,
      Seq(models.responses.DueDate("date", None, DefAmounts(None, "10000", Some("9000"), "date"),
        Seq(TaxTypeGroup("a1", DefAmounts(None, "10000", None, "date"), Seq(TaxType("type", DefAmounts(None, "10000", None, "date")))))))
    )

    val request: GuaranteeAccountTransactionsRequest = GuaranteeAccountTransactionsRequest(AccountNumber("gan"), None, None)

    val app: Application = GuiceApplicationBuilder().overrides(
      inject.bind[Acc28Connector].toInstance(mockAcc28Connector)
    ).configure(
      "microservice.metrics.enabled" -> false,
      "metrics.enabled" -> false,
      "auditing.enabled" -> false
    ).build()

    val service: GuaranteeTransactionsService = app.injector.instanceOf[GuaranteeTransactionsService]
  }
}
