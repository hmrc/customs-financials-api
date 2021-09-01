/*
 * Copyright 2021 HM Revenue & Customs
 *
 */

package services

import connectors.{Acc37Connector, Acc38Connector}
import domain.acc37.{AmendCorrespondenceAddressResponse, ContactDetails, Response, ResponseCommon}
import domain.acc38.GetCorrespondenceAddressResponse
import models.{AccountNumber, EORI, EmailAddress}
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.Helpers._
import play.api.{Application, inject}
import uk.gov.hmrc.http.HeaderCarrier
import utils.SpecBase

import scala.concurrent.{ExecutionContext, Future}

class AccountContactDetailsServiceSpec extends SpecBase {

  "getAccountContactDetails" should {
    "return the response from the connector" in new Setup {
      when(mockAcc38Connector.getAccountContactDetails(AccountNumber("dan"), EORI("someEori"), None))
        .thenReturn(Future.successful(acc38Response))

      running(app){
        val result = await(service.getAccountContactDetails(AccountNumber("dan"), EORI("someEori")))
        result mustBe acc38Response
      }
    }
  }

  "updateAccountContactDetails" should {
    "return the response from the connector" in new Setup {
      when(mockAcc37Connector.updateAccountContactDetails(AccountNumber("dan"), EORI("someEori"), acc37ContactInfo, None))
        .thenReturn(Future.successful(acc37SuccessResponse))

      running(app){
        val result = await(service.updateAccountContactDetails(AccountNumber("dan"), EORI("someEori"), acc37ContactInfo))
        result mustBe acc37SuccessResponse
      }
    }
  }

  trait Setup {
    implicit val hc: HeaderCarrier = HeaderCarrier()
    implicit val executionContext: ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global

    val mockAcc37Connector: Acc37Connector = mock[Acc37Connector]
    val mockAcc38Connector: Acc38Connector = mock[Acc38Connector]

    val acc37ResponseCommon: ResponseCommon = ResponseCommon("OK", None, "2020-10-05T09:30:47Z", None)
    val acc37SuccessResponse: Response = domain.acc37.Response(AmendCorrespondenceAddressResponse(acc37ResponseCommon))

    val acc37ContactInfo: ContactDetails = domain.acc37.ContactDetails(
      Some("Example Name"),
      "Example Street",
      Some("Example"),
      None,
      Some("Example"),
      Some("A00 00A"),
      "GB",
      Some("011111111111"),
      None,
      Some(EmailAddress("example@email.com"))
    )

    val acc38Response: domain.acc38.Response = domain.acc38.Response(
      GetCorrespondenceAddressResponse(
        domain.acc38.ResponseCommon(
          "OK",
          None,
          "",
          None),
        None
      )
    )

    val app: Application = GuiceApplicationBuilder().overrides(
      inject.bind[Acc37Connector].toInstance(mockAcc37Connector),
      inject.bind[Acc38Connector].toInstance(mockAcc38Connector)
    ).configure(
      "microservice.metrics.enabled" -> false,
      "metrics.enabled" -> false,
      "auditing.enabled" -> false
    ).build()

    val service: AccountContactDetailsService = app.injector.instanceOf[AccountContactDetailsService]

  }
}
