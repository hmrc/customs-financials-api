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

package services

import connectors.Acc24Connector
import models.requests.HistoricDocumentRequest
import models.{EORI, FileRole}
import org.mockito.*
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.Helpers.running
import play.api.{Application, inject}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.audit.http.connector.AuditResult
import utils.SpecBase
import utils.TestData.{MONTH_1, MONTH_3, YEAR_2019}

import scala.concurrent.*

class HistoricDocumentServiceSpec extends SpecBase {

  "HistoricDocumentService" when {

    "calling ACC24 (request historic documents)" should {

      List(true, false).foreach { expectedResult =>
        s"propagate the connector's result when $expectedResult" in new Setup {

          running(app) {
            val historicDocumentRequest =
              HistoricDocumentRequest(eori, FileRole("C79Certificate"), YEAR_2019, MONTH_1, YEAR_2019, MONTH_3, None)

            val historicDocumentRequestCaptor: ArgumentCaptor[HistoricDocumentRequest] =
              ArgumentCaptor.forClass(classOf[HistoricDocumentRequest])

            when(mockAcc24Connector.sendHistoricDocumentRequest(historicDocumentRequestCaptor.capture()))
              .thenReturn(Future.successful(expectedResult))

            when(mockAuditingService.auditHistoricStatementRequest(any)(any))
              .thenReturn(Future.successful(AuditResult.Success))

            val actualResult = await(service.sendHistoricDocumentRequest(historicDocumentRequest))

            actualResult mustBe expectedResult
            historicDocumentRequestCaptor.getValue mustBe historicDocumentRequest
          }
        }
      }
    }
  }

  trait Setup {
    implicit val hc: HeaderCarrier                  = HeaderCarrier()
    implicit val executionContext: ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global
    val mockAuditingService: AuditingService        = mock[AuditingService]

    val eori: EORI                         = EORI("testEORI")
    val mockAcc24Connector: Acc24Connector = mock[Acc24Connector]

    val app: Application = GuiceApplicationBuilder()
      .overrides(
        inject.bind[Acc24Connector].toInstance(mockAcc24Connector),
        inject.bind[AuditingService].toInstance(mockAuditingService)
      )
      .configure(
        "microservice.metrics.enabled" -> false,
        "metrics.enabled"              -> false,
        "auditing.enabled"             -> false
      )
      .build()

    val service: HistoricDocumentService = app.injector.instanceOf[HistoricDocumentService]
  }
}
