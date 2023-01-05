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

import domain.{Acc41ErrorResponse, AuthoritiesCsvGenerationResponse}
import domain.acc41._
import models.EORI
import play.api.Application
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.Helpers._
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient}
import utils.SpecBase

import scala.concurrent.Future

class Acc41ConnectorSpec extends SpecBase {

  "initiateAuthoritiesCSV" should {
    "return Left Acc41ErrorResponse when request returns error message" in new Setup {
      when[Future[domain.acc41.StandingAuthoritiesForEORIResponse]](mockHttpClient.POST(any, any, any)(any, any, any, any))
        .thenReturn(Future.successful(StandingAuthoritiesForEORIResponse(response(Some("Request failed"), None))))

      running(app) {
        val result = await(connector.initiateAuthoritiesCSV(EORI("someEori")))
        result mustBe Left(Acc41ErrorResponse)
      }
    }

    "return Right AuthoritiesCsvGeneration when successful response containing a requestAcceptedDate" in new Setup {
      when[Future[domain.acc41.StandingAuthoritiesForEORIResponse]](mockHttpClient.POST(any, any, any)(any, any, any, any))
        .thenReturn(Future.successful(StandingAuthoritiesForEORIResponse(response(None, Some("020-06-09T21:59:56Z")))))

      running(app) {
        val result = await(connector.initiateAuthoritiesCSV(EORI("someEori")))
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
      RequestDetail(EORI("someEORI")),
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
  }
}
