/*
 * Copyright 2022 HM Revenue & Customs
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

import domain.{AuthoritiesFound, ErrorResponse, NoAuthoritiesFound}
import domain.acc38.GetCorrespondenceAddressResponse
import domain.acc40.{Account, CashAccount, DutyDefermentAccount, GeneralGuaranteeAccount, RequestCommon, RequestDetail, ResponseDetail}
import models.{AccountNumber, EORI}
import play.api.Application
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient}
import play.api.test.Helpers._
import utils.SpecBase

import scala.concurrent.Future

class Acc40ConnectorSpec extends SpecBase {

  "searchAuthorities" should {
    "return Left no authorities when no authorities returned in the response" in new Setup {
      when[Future[domain.acc40.Response]](mockHttpClient.POST(any, any, any)(any, any, any, any))
        .thenReturn(Future.successful(response(None, Some(0), None, None, None)))

      running(app) {
        val result = await(connector.searchAuthorities(EORI("someEori"), EORI("someEori")))
        result mustBe Left(NoAuthoritiesFound)
      }
    }

    "return Left with error response if the error message present in the response" in new Setup {
      when[Future[domain.acc40.Response]](mockHttpClient.POST(any, any, any)(any, any, any, any))
        .thenReturn(Future.successful(response(Some("error message"), Some(0), None, None, None)))

      running(app) {
        val result = await(connector.searchAuthorities(EORI("someEori"), EORI("someEori")))
        result mustBe Left(ErrorResponse)
      }
    }

    "return Right if a valid response with authorities returned" in new Setup {
      when[Future[domain.acc40.Response]](mockHttpClient.POST(any, any, any)(any, any, any, any))
        .thenReturn(Future.successful(response(None, Some(1), Some(Seq(CashAccount(Account("accountNumber", "accountType", "accountOwner"), Some(10.0)))), None, None)))

      running(app) {
        val result = await(connector.searchAuthorities(EORI("someEori"), EORI("someEori")))
        result mustBe Right(AuthoritiesFound(Some(1), None, None, Some(Seq(CashAccount(Account("accountNumber", "accountType", "accountOwner"), Some(10.0))))))
      }
    }
  }


  trait Setup {
    implicit val hc: HeaderCarrier = HeaderCarrier()
    val mockHttpClient: HttpClient = mock[HttpClient]

    def response(error: Option[String],
                 numberOfAuthorities: Option[Int],
                 cashAccount: Option[Seq[CashAccount]],
                 dutyDefermentAccount: Option[Seq[DutyDefermentAccount]],
                 generalGuaranteeAccount: Option[Seq[GeneralGuaranteeAccount]]
                ): domain.acc40.Response = domain.acc40.Response(
      RequestCommon("date", "MDTP", "reference", "CDS"),
      RequestDetail(EORI("someEORI"), "1", EORI("someOtherEORI")),
      ResponseDetail(
        errorMessage = error,
        numberOfAuthorities = numberOfAuthorities,
        dutyDefermentAccounts = dutyDefermentAccount,
        generalGuaranteeAccounts = generalGuaranteeAccount,
        cdsCashAccounts = cashAccount
      )
    )


    val app: Application = GuiceApplicationBuilder().overrides(
      bind[HttpClient].toInstance(mockHttpClient)
    ).configure(
      "microservice.metrics.enabled" -> false,
      "metrics.enabled" -> false,
      "auditing.enabled" -> false
    ).build()

    val connector: Acc40Connector = app.injector.instanceOf[Acc40Connector]
  }

}
