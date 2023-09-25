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

package connectors

import java.time.LocalDate
import domain.SecureMessage
import models.AccountType
import utils.SpecBase

class SecureMessageConnectorSpec extends SpecBase {

  "SecureMessageConnector" should {
    "Populate RequestCommon" in new Setup {
      
      val commonRequest = SecureMessage.RequestCommon(
        externalRef = SecureMessage.ExternalReference("123456","mdtp"),
        recipient = SecureMessage.Recipient("CDS Financials",
          SecureMessage.TaxIdentifier("HMRC-CUS-ORG", "123123123")),
        params = SecureMessage.Params(LocalDate.now(), LocalDate.now(), "Financials"),
        email = "email@email.com",
        tags = SecureMessage.Tags("CDS Financials"),
        content = contents,
        messageType = "newMEssageAlert",
        validForm = LocalDate.now().toString(),
        alertQueue = "DEFAULT"
      )

      commonRequest mustBe compareRequest

    }
  }

/*  "initiateAuthoritiesCSV" should {
    "return Left Acc41ErrorResponse when request returns error message" in new Setup {
      when[Future[domain.acc41.StandingAuthoritiesForEORIResponse]](mockHttpClient.POST(any, any, any)(any, any, any, any))
        .thenReturn(Future.successful(StandingAuthoritiesForEORIResponse(response(Some("Request failed"), None))))

      running(app) {
        val result = await(connector.initiateAuthoritiesCSV(EORI("someEori"),Some(EORI("someAltEori"))))
        result mustBe Left(Acc41ErrorResponse)
      }
    }

    "return Right AuthoritiesCsvGeneration when no alternateEORI" in new Setup {
      when[Future[domain.acc41.StandingAuthoritiesForEORIResponse]](mockHttpClient.POST(any, any, any)(any, any, any, any))
        .thenReturn(Future.successful(StandingAuthoritiesForEORIResponse(response(None, Some("020-06-09T21:59:56Z")))))

      running(app) {
        val result = await(connector.initiateAuthoritiesCSV(EORI("someEori"),Some(EORI(""))))
        result mustBe Right(AuthoritiesCsvGenerationResponse(Some("020-06-09T21:59:56Z")))
      }
    }


    "return Right AuthoritiesCsvGeneration when successful response containing a requestAcceptedDate" in new Setup {
      when[Future[domain.acc41.StandingAuthoritiesForEORIResponse]](mockHttpClient.POST(any, any, any)(any, any, any, any))
        .thenReturn(Future.successful(StandingAuthoritiesForEORIResponse(response(None, Some("020-06-09T21:59:56Z")))))

      running(app) {
        val result = await(connector.initiateAuthoritiesCSV(EORI("someEori"),Some(EORI("someAltEori"))))
        result mustBe Right(AuthoritiesCsvGenerationResponse(Some("020-06-09T21:59:56Z")))
      }
    }
  }


  trait Setup {
    implicit val hc: HeaderCarrier = HeaderCarrier()
    val mockHttpClient: HttpClient = mock[HttpClient]

    def response(error: Option[String],
                 requestAcceptedDate: Option[String]
                ): domain.acc41.Response = domain.acc41.Response(
      RequestCommon("date", "MDTP", "reference", "CDS"),
      RequestDetail(EORI("someEORI"),Some(EORI("someAltEori"))),
      ResponseDetail(
        errorMessage = error,
        requestAcceptedDate = requestAcceptedDate
      )
    )


    val app: Application = GuiceApplicationBuilder().overrides(
      bind[HttpClient].toInstance(mockHttpClient)
    ).configure(
      "microservice.metrics.enabled" -> false,
      "metrics.enabled" -> false,
      "auditing.enabled" -> false
    ).build()

    val connector: Acc41Connector = app.injector.instanceOf[Acc41Connector]
  }*/

  trait Setup {

    val contents: List[SecureMessage.Content] = List(
      SecureMessage.Content("en", AccountType("asd"), "asd"),
      SecureMessage.Content("cy", AccountType("asd"), "asd")
    )

    val compareRequest = SecureMessage.RequestCommon(
      externalRef = SecureMessage.ExternalReference("123456","mdtp"),
      recipient = SecureMessage.Recipient("CDS Financials",
        SecureMessage.TaxIdentifier("HMRC-CUS-ORG", "123123123")),
      params = SecureMessage.Params(LocalDate.now(), LocalDate.now(), "Financials"),
      email = "email@email.com",
      tags = SecureMessage.Tags("CDS Financials"),
      content = contents,
      messageType = "newMEssageAlert",
      validForm = LocalDate.now().toString(),
      alertQueue = "DEFAULT"
    )
  }
}
