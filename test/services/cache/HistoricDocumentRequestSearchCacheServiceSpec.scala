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

package services.cache

import models.{HistoricDocumentRequestSearch, Params, SearchRequest}
import play.api.{Application, inject}
import utils.SpecBase
import utils.Utils.emptyString

import java.util.UUID
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class HistoricDocumentRequestSearchCacheServiceSpec extends SpecBase {

  "saveHistoricDocumentRequestSearch" should {
    "return true when document is insert successfully" in new Setup {
      when(mockHistDocReqSearchCache.insertDocument(any)).thenReturn(Future.successful(true))

      val service: HistoricDocumentRequestSearchCacheService =
        app.injector.instanceOf[HistoricDocumentRequestSearchCacheService]

      service.saveHistoricDocumentRequestSearch(histDocRequestSearch).map {
        result => result mustBe true
      }
    }

    "return false when document is not inserted" in new Setup {
      when(mockHistDocReqSearchCache.insertDocument(any)).thenReturn(Future.successful(false))

      val service: HistoricDocumentRequestSearchCacheService =
        app.injector.instanceOf[HistoricDocumentRequestSearchCacheService]

      service.saveHistoricDocumentRequestSearch(histDocRequestSearch).map {
        result => result mustBe false
      }
    }
  }

  "retrieveHistDocRequestSearchDocsForCurrentEori" should {
    "retrieve the documents when documents are present in the DB" in new Setup {
      when(mockHistDocReqSearchCache.retrieveDocumentsForCurrentEori(any)).thenReturn(Future.successful(
        Seq(histDocRequestSearch, histDocRequestSearch)))

      val service: HistoricDocumentRequestSearchCacheService =
        app.injector.instanceOf[HistoricDocumentRequestSearchCacheService]

      service.retrieveHistDocRequestSearchDocsForCurrentEori("test_eori").map {
        records => records mustBe Seq(histDocRequestSearch, histDocRequestSearch)
      }
    }

    "return the empty documents when documents are not present in the DB" in new Setup {
      when(mockHistDocReqSearchCache.retrieveDocumentsForCurrentEori(any)).thenReturn(Future.successful(
        Seq()))

      val service: HistoricDocumentRequestSearchCacheService =
        app.injector.instanceOf[HistoricDocumentRequestSearchCacheService]

      service.retrieveHistDocRequestSearchDocsForCurrentEori("test_eori").map {
        records => records mustBe Seq()
      }
    }
  }

  trait Setup {
    val mockHistDocReqSearchCache: HistoricDocumentRequestSearchCache = mock[HistoricDocumentRequestSearchCache]

    val app: Application = application().overrides(
      inject.bind[HistoricDocumentRequestSearchCache].toInstance(mockHistDocReqSearchCache)
    ).build()

    val searchID: UUID = UUID.randomUUID()
    val userId: String = "test_userId"
    val resultsFound: String = "inProcess"
    val searchStatusUpdateDate: String = emptyString
    val currentEori: String = "GB123456789012"
    val params: Params = Params("2", "2021", "4", "2021", "DutyDefermentStatement", "1234567")
    val searchRequests: Set[SearchRequest] = Set(
      SearchRequest(
        "GB123456789012", "5b89895-f0da-4472-af5a-d84d340e7mn5", "inProcess", emptyString, emptyString, 0),
      SearchRequest(
        "GB234567890121", "5c79895-f0da-4472-af5a-d84d340e7mn6", "inProcess", emptyString, emptyString, 0)
    )

    val histDocRequestSearch: HistoricDocumentRequestSearch =
      HistoricDocumentRequestSearch(searchID,
        resultsFound,
        searchStatusUpdateDate,
        currentEori,
        params,
        searchRequests)

  }
}
