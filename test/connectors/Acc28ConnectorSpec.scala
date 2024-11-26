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

import models.requests.GuaranteeAccountTransactionsRequest
import models.responses.{GetGGATransactionResponse, GuaranteeTransactionsResponse, ResponseCommon, ResponseDetail}
import models.{AccountNumber, ExceededThresholdErrorException, NoAssociatedDataException}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import play.api.Application
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.Helpers.*
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.http.client.{HttpClientV2, RequestBuilder}
import utils.SpecBase

import java.time.LocalDate
import scala.concurrent.Future

class Acc28ConnectorSpec extends SpecBase {

  "retrieveGuaranteeTransactions" should {
    "return a list of declarations on a successful response" in new Setup {
      when(requestBuilder.withBody(any())(any(), any(), any())).thenReturn(requestBuilder)
      when(requestBuilder.setHeader(any[(String, String)]())).thenReturn(requestBuilder)
      when(mockHttpClient.post(any)(any)).thenReturn(requestBuilder)
      when(requestBuilder.execute(any, any)).thenReturn(Future.successful(response))

      running(app) {
        val result = await(connector.retrieveGuaranteeTransactions(request))
        result mustBe Right(List.empty)
      }
    }

    "return NoAssociatedData error response when responded with no associated data" in new Setup {
      when(requestBuilder.withBody(any())(any(), any(), any())).thenReturn(requestBuilder)
      when(requestBuilder.setHeader(any[(String, String)]())).thenReturn(requestBuilder)
      when(mockHttpClient.post(any)(any)).thenReturn(requestBuilder)
      when(requestBuilder.execute(any, any)).thenReturn(Future.successful(noDataResponse))

      running(app) {
        val result = await(connector.retrieveGuaranteeTransactions(request))
        result mustBe Left(NoAssociatedDataException)
      }
    }

    "return ExceededThreshold error response when responded with exceeded threshold" in new Setup {
      when(requestBuilder.withBody(any())(any(), any(), any())).thenReturn(requestBuilder)
      when(requestBuilder.setHeader(any[(String, String)]())).thenReturn(requestBuilder)
      when(mockHttpClient.post(any)(any)).thenReturn(requestBuilder)
      when(requestBuilder.execute(any, any)).thenReturn(Future.successful(tooMuchDataRequestedResponse))

      running(app) {
        val result = await(connector.retrieveGuaranteeTransactions(request))
        result mustBe Left(ExceededThresholdErrorException)
      }
    }
  }

  trait Setup {
    implicit val hc: HeaderCarrier = HeaderCarrier()
    val mockHttpClient: HttpClientV2 = mock[HttpClientV2]
    val requestBuilder: RequestBuilder = mock[RequestBuilder]

    val request: GuaranteeAccountTransactionsRequest = GuaranteeAccountTransactionsRequest(
      AccountNumber("GAN"),
      openItems = Some(false),
      None
    )

    val response: GuaranteeTransactionsResponse = GuaranteeTransactionsResponse(
      GetGGATransactionResponse(
        ResponseCommon("OK", None, LocalDate.now().toString),
        Some(ResponseDetail(openItems = true, Seq.empty))
      )
    )

    val noDataResponse: GuaranteeTransactionsResponse = GuaranteeTransactionsResponse(
      GetGGATransactionResponse(
        ResponseCommon("OK", Some("025-No associated data found"), LocalDate.now().toString),
        Some(ResponseDetail(openItems = true, Seq.empty))
      )
    )

    val tooMuchDataRequestedResponse: GuaranteeTransactionsResponse = GuaranteeTransactionsResponse(
      GetGGATransactionResponse(
        ResponseCommon("OK",
          Some("091-The query has exceeded the threshold, please refine the search"),
          LocalDate.now().toString),
        Some(ResponseDetail(openItems = true, Seq.empty))
      )
    )

    val app: Application = GuiceApplicationBuilder().overrides(
      bind[HttpClientV2].toInstance(mockHttpClient),
      bind[RequestBuilder].toInstance(requestBuilder)
    ).configure(
      "microservice.metrics.enabled" -> false,
      "metrics.enabled" -> false,
      "auditing.enabled" -> false
    ).build()

    val connector: Acc28Connector = app.injector.instanceOf[Acc28Connector]
  }
}
