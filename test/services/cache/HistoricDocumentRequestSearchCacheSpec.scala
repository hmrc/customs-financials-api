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

import config.AppConfig
import models.{HistoricDocumentRequestSearch, Params, SearchRequest}
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.time.{Millis, Seconds, Span}
import org.scalatest.{BeforeAndAfter, BeforeAndAfterAll}
import play.api.Configuration
import uk.gov.hmrc.mongo.MongoComponent
import utils.SpecBase
import utils.Utils.emptyString

import java.util.UUID
import scala.concurrent.ExecutionContext.Implicits.global

class HistoricDocumentRequestSearchCacheSpec extends SpecBase
  with EmbeddedMongoDBSupport
  with BeforeAndAfter
  with BeforeAndAfterAll
  with ScalaFutures {

  override implicit val patienceConfig: PatienceConfig = PatienceConfig(Span(30, Seconds), Span(1, Millis))

  import HistoricDocumentRequestSearchCacheSpec._

  private val externalId = "externalId"
  var historicDocumentRequestCache: HistoricDocumentRequestSearchCache = _

  override def beforeAll(): Unit = {
    when(mockConfig.get[String]("mongodb.historic-document-request-search.name")).thenReturn(
      "historic-document-request-search")
    when(mockAppConfig.mongoHistDocSearchCollectionName).thenReturn("historic-document-request-search")
    when(mockConfig.get[Long]("mongodb.historic-document-request-search.timeToLiveInSeconds")).thenReturn(
      ttlValue)

    when(mockAppConfig.mongoHistDocSearchTtl).thenReturn(ttlValue)

    initMongoDExecutable()
    startMongoD()
    historicDocumentRequestCache = buildFormRepository(mongoHost, mongoPort)
  }

  override def afterAll(): Unit =
    stopMongoD()

  "insertRecord " should {
    "insert the record in Mongo collection when collection is empty" in {
      val documentsInDB = for {
        _ <- historicDocumentRequestCache.collection.drop().toFuture()
        _ <- historicDocumentRequestCache.insertRecord(getHistoricDocumentRequestSearchDoc)
        documentsInDB <- historicDocumentRequestCache.collection.find().toFuture()
      } yield documentsInDB

      whenReady(documentsInDB) {
        documentsInDB =>
          documentsInDB.size mustBe 1
      }
    }

    "insert the record in Mongo collection when collection has one doc already" in {
      val documentsInDB = for {
        _ <- historicDocumentRequestCache.collection.drop().toFuture()
        _ <- historicDocumentRequestCache.insertRecord(getHistoricDocumentRequestSearchDoc)
        _ <- historicDocumentRequestCache.insertRecord(getHistoricDocumentRequestSearchDoc)
        documentsInDB <- historicDocumentRequestCache.collection.find().toFuture()
      } yield documentsInDB

      whenReady(documentsInDB) {
        documentsInDB =>
          documentsInDB.size mustBe 2
      }
    }
  }

  "retrieveRecords" should {
    "retrieve the records correctly" in {
      val documentsInDB = for {
        _ <- historicDocumentRequestCache.collection.drop().toFuture()
        _ <- historicDocumentRequestCache.insertRecord(getHistoricDocumentRequestSearchDoc)
        documentsInDB <- historicDocumentRequestCache.retrieveRecords("test_userId")
      } yield documentsInDB

      whenReady(documentsInDB) { documentsInDB =>
        documentsInDB.nonEmpty mustBe true
      }
    }
  }

  private def getHistoricDocumentRequestSearchDoc: HistoricDocumentRequestSearch = {
    val searchID: UUID = UUID.randomUUID()
    val userId: String = "test_userId"
    val resultsFound: String = "inProcess"
    val searchStatusUpdateDate: String = emptyString
    val currentEori: String = "GB123456789012"
    val params: Params = Params("2", "2021", "4", "2021", "DutyDeferment", "1234567")
    val searchRequests: Set[SearchRequest] = Set(
      SearchRequest(
        "GB123456789012", "5b89895-f0da-4472-af5a-d84d340e7mn5", "inProcess", emptyString, emptyString, 0),
      SearchRequest(
        "GB234567890121", "5c79895-f0da-4472-af5a-d84d340e7mn6", "inProcess", emptyString, emptyString, 0)
    )

    HistoricDocumentRequestSearch(searchID,
      userId,
      resultsFound,
      searchStatusUpdateDate,
      currentEori,
      params,
      searchRequests)
  }
}

object HistoricDocumentRequestSearchCacheSpec extends SpecBase {

  private val mockAppConfig = mock[AppConfig]
  private val mockConfig = mock[Configuration]
  private val ttlValue = 28

  private def buildFormRepository(mongoHost: String,
                                  mongoPort: Int): HistoricDocumentRequestSearchCache = {
    val databaseName = "historic-document-request-search"
    val mongoUri = s"mongodb://$mongoHost:$mongoPort/$databaseName?heartbeatFrequencyMS=1000&rm.failover=default"
    new HistoricDocumentRequestSearchCache(mockAppConfig, MongoComponent(mongoUri))
  }
}
