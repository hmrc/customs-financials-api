/*
 * Copyright 2021 HM Revenue & Customs
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

import models.responses.{CashTransactionsResponse, CashTransactionsResponseCommon, CashTransactionsResponseDetail, GetCashAccountTransactionListingResponse}
import models.{ExceededThresholdErrorException, NoAssociatedDataException}
import play.api.Application
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.Helpers._
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient}
import utils.SpecBase

import java.time.LocalDate
import scala.concurrent.Future

class Acc31ConnectorSpec extends SpecBase {

  "retrieveCashTransactions" should {
    "return a list of declarations on a successful response" in new Setup {
      when[Future[CashTransactionsResponse]](mockHttpClient.POST(any, any, any)(any, any, any, any))
        .thenReturn(Future.successful(response))

      running(app) {
        val result = await(connector.retrieveCashTransactions("can", LocalDate.now(), LocalDate.now()))
        result mustBe Right(Some(CashTransactionsResponseDetail(None, None)))
      }
    }

    "return NoAssociatedData error response when responded with no associated data" in new Setup {
      when[Future[CashTransactionsResponse]](mockHttpClient.POST(any, any, any)(any, any, any, any))
        .thenReturn(Future.successful(noDataResponse))

      running(app) {
        val result = await(connector.retrieveCashTransactions("can", LocalDate.now(), LocalDate.now()))
        result mustBe Left(NoAssociatedDataException)
      }
    }

    "return ExceededThreshold error response when responded with exceeded threshold" in new Setup {
      when[Future[CashTransactionsResponse]](mockHttpClient.POST(any, any, any)(any, any, any, any))
        .thenReturn(Future.successful(tooMuchDataRequestedResponse))

      running(app) {
        val result = await(connector.retrieveCashTransactions("can", LocalDate.now(), LocalDate.now()))
        result mustBe Left(ExceededThresholdErrorException)
      }
    }
  }

  trait Setup {
    implicit val hc: HeaderCarrier = HeaderCarrier()
    val mockHttpClient: HttpClient = mock[HttpClient]
    val noAssociatedDataMessage = "025-No associated data found"
    val exceedsThresholdMessage = "091-The query has exceeded the threshold, please refine the search"

    val response: CashTransactionsResponse = CashTransactionsResponse(
      GetCashAccountTransactionListingResponse(
        CashTransactionsResponseCommon("OK", None, LocalDate.now().toString),
        Some(CashTransactionsResponseDetail(None, None))
      )
    )

    val noDataResponse: CashTransactionsResponse = CashTransactionsResponse(
      GetCashAccountTransactionListingResponse(
        CashTransactionsResponseCommon("OK", Some(noAssociatedDataMessage), LocalDate.now().toString),
        Some(CashTransactionsResponseDetail(None, None))
      )
    )

    val tooMuchDataRequestedResponse: CashTransactionsResponse = CashTransactionsResponse(
      GetCashAccountTransactionListingResponse(
        CashTransactionsResponseCommon("OK", Some(exceedsThresholdMessage), LocalDate.now().toString),
        Some(CashTransactionsResponseDetail(None, None))
      )
    )

    val app: Application = GuiceApplicationBuilder().overrides(
      bind[HttpClient].toInstance(mockHttpClient)
    ).configure(
      "microservice.metrics.enabled" -> false,
      "metrics.enabled" -> false,
      "auditing.enabled" -> false
    ).build()

    val connector: Acc31Connector = app.injector.instanceOf[Acc31Connector]
  }
}
