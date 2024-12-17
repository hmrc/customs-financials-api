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
import models.SearchResultStatus.*
import models.{HistoricDocumentRequestSearch, Params, SearchRequest, SearchResultStatus}
import org.mockito.Mockito.when
import org.mongodb.scala.{ObservableFuture, SingleObservableFuture}
import org.mongodb.scala.model.{Filters, Updates}
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.time.{Millis, Seconds, Span}
import org.scalatest.{BeforeAndAfter, BeforeAndAfterAll, Ignore}
import play.api.Configuration
import uk.gov.hmrc.mongo.MongoComponent
import utils.Utils.emptyString
import utils.{SpecBase, Utils}

import java.time.LocalDateTime
import java.util.UUID
import scala.concurrent.ExecutionContext.Implicits.global

//TODO its only temporarily since EmbeddedMongoDB Support is failing in pipeline, will remove once its fixed.
// Also set branch coverage to 90%
@Ignore
class HistoricDocumentRequestSearchCacheSpec
    extends SpecBase
    with EmbeddedMongoDBSupport
    with BeforeAndAfter
    with BeforeAndAfterAll
    with ScalaFutures {

  val spanLength = 30

  override implicit val patienceConfig: PatienceConfig = PatienceConfig(Span(spanLength, Seconds), Span(1, Millis))

  import HistoricDocumentRequestSearchCacheSpec.*

  var historicDocumentRequestCache: HistoricDocumentRequestSearchCache = _

  override def beforeAll(): Unit = {
    when(mockConfig.get[String]("mongodb.historic-document-request-search.name"))
      .thenReturn("historic-document-request-search")
    when(mockAppConfig.mongoHistDocSearchCollectionName).thenReturn("historic-document-request-search")
    when(mockConfig.get[Long]("mongodb.historic-document-request-search.timeToLiveInSeconds")).thenReturn(ttlValue)

    when(mockAppConfig.mongoHistDocSearchTtl).thenReturn(ttlValue)

    initMongoDExecutable()
    startMongoD()
    historicDocumentRequestCache = buildFormRepository(mongoHost, mongoPort)
  }

  override def afterAll(): Unit =
    stopMongoD()

  "insertDocument " should {
    "insert the document in Mongo collection when collection is empty" in {
      val documentsInDB = for {
        _             <- historicDocumentRequestCache.collection.drop().toFuture()
        _             <- historicDocumentRequestCache.insertDocument(getHistoricDocumentRequestSearchDoc)
        documentsInDB <- historicDocumentRequestCache.collection.find().toFuture()
      } yield documentsInDB

      whenReady(documentsInDB) { documentsInDB =>
        documentsInDB.size mustBe 1
      }
    }

    "insert the document in Mongo collection when collection has one doc already" in {
      val documentsInDB = for {
        _             <- historicDocumentRequestCache.collection.drop().toFuture()
        _             <- historicDocumentRequestCache.insertDocument(getHistoricDocumentRequestSearchDoc)
        _             <- historicDocumentRequestCache.insertDocument(getHistoricDocumentRequestSearchDoc)
        documentsInDB <- historicDocumentRequestCache.collection.find().toFuture()
      } yield documentsInDB

      whenReady(documentsInDB) { documentsInDB =>
        documentsInDB.size mustBe 2
      }
    }
  }

  "retrieveDocumentForStatementRequestID" should {
    "retrieve the document for the given statementRequestID" in {
      val documentsInDB = for {
        _            <- historicDocumentRequestCache.collection.drop().toFuture()
        _            <- historicDocumentRequestCache.insertDocument(getHistoricDocumentRequestSearchDoc)
        retrievedDoc <-
          historicDocumentRequestCache.retrieveDocumentForStatementRequestID("5b89895-f0da-4472-af5a-d84d340e7mn5")
      } yield retrievedDoc

      whenReady(documentsInDB) { documentsInDB =>
        documentsInDB.get.currentEori mustBe "GB123456789012"
      }
    }

    "return None if there is no document for the provided value" in {
      val documentsInDB = for {
        _            <- historicDocumentRequestCache.collection.drop().toFuture()
        retrievedDoc <-
          historicDocumentRequestCache.retrieveDocumentForStatementRequestID("5b89895-f0da-4472-af5a-d84d340e7mn5")
      } yield retrievedDoc

      whenReady(documentsInDB) { documentsInDB =>
        documentsInDB mustBe empty
      }
    }
  }

  "retrieveDocumentsForCurrentEori" should {
    "retrieve the documents correctly" in {
      val documentsInDB = for {
        _             <- historicDocumentRequestCache.collection.drop().toFuture()
        _             <- historicDocumentRequestCache.insertDocument(getHistoricDocumentRequestSearchDoc)
        documentsInDB <- historicDocumentRequestCache.retrieveDocumentsForCurrentEori("GB123456789012")
      } yield documentsInDB

      whenReady(documentsInDB) { documentsInDB =>
        documentsInDB.nonEmpty mustBe true
      }
    }

    "return None if there is no document for the provided value" in {
      val documentsInDB = for {
        _            <- historicDocumentRequestCache.collection.drop().toFuture()
        retrievedDoc <- historicDocumentRequestCache.retrieveDocumentsForCurrentEori("GB123456789012")
      } yield retrievedDoc

      whenReady(documentsInDB) { documentsInDB =>
        documentsInDB mustBe Seq()
      }
    }
  }

  "updateDocumentForQueryFilter" should {
    "update the document correctly for the given queryFilter and updates" in {
      val docToBeInserted = getHistoricDocumentRequestSearchDoc
      val searchId        = docToBeInserted.searchID.toString
      val queryFilter     = Filters.equal("searchID", searchId)
      val updates         = Updates.set("resultsFound", SearchResultStatus.no.toString)

      val documentsInDB = for {
        _        <- historicDocumentRequestCache.collection.drop().toFuture()
        _        <- historicDocumentRequestCache.insertDocument(docToBeInserted)
        _        <- historicDocumentRequestCache.updateDocumentForQueryFilter(queryFilter, updates)
        finalDoc <-
          historicDocumentRequestCache.retrieveDocumentForStatementRequestID("5b89895-f0da-4472-af5a-d84d340e7mn5")
      } yield finalDoc

      whenReady(documentsInDB) { documentsInDB =>
        val docInDB = documentsInDB.get

        docInDB.currentEori mustBe "GB123456789012"
        docInDB.searchID.toString mustBe searchId
        docInDB.resultsFound mustBe SearchResultStatus.no
      }
    }

    "return None when there is no document to update" in {
      val docToBeInserted = getHistoricDocumentRequestSearchDoc
      val searchId        = docToBeInserted.searchID.toString
      val queryFilter     = Filters.equal("searchID", searchId)
      val updates         = Updates.set("resultsFound", SearchResultStatus.no.toString)

      val documentsInDB = for {
        _        <- historicDocumentRequestCache.collection.drop().toFuture()
        _        <- historicDocumentRequestCache.updateDocumentForQueryFilter(queryFilter, updates)
        finalDoc <-
          historicDocumentRequestCache.retrieveDocumentForStatementRequestID("5b89895-f0da-4472-af5a-d84d340e7mn5")
      } yield finalDoc

      whenReady(documentsInDB) { documentsInDB =>
        documentsInDB mustBe empty
      }
    }
  }

  "updateSearchRequestForStatementRequestId" should {
    "update the document with correct fields for the given statementRequestID" in {
      val docToBeInserted = getHistoricDocumentRequestSearchDoc
      val updatedDateTime = Utils.dateTimeAsIso8601(LocalDateTime.now)

      val updatedSearchRequests = Set(
        SearchRequest(
          "GB123456789012",
          "5b89895-f0da-4472-af5a-d84d340e7mn5",
          SearchResultStatus.no,
          updatedDateTime,
          "AWSUnreachable",
          0
        ),
        SearchRequest("GB234567890121", "5c79895-f0da-4472-af5a-d84d340e7mn6", inProcess, emptyString, emptyString, 0)
      )

      val documentsInDB = for {
        _        <- historicDocumentRequestCache.collection.drop().toFuture()
        _        <- historicDocumentRequestCache.insertDocument(docToBeInserted)
        _        <- historicDocumentRequestCache.updateSearchRequestForStatementRequestId(
                      updatedSearchRequests,
                      docToBeInserted.searchID.toString
                    )
        finalDoc <-
          historicDocumentRequestCache.retrieveDocumentForStatementRequestID("5b89895-f0da-4472-af5a-d84d340e7mn5")
      } yield finalDoc

      whenReady(documentsInDB) { documentsInDB =>
        documentsInDB.get.currentEori mustBe "GB123456789012"

        val searchRequestAfterUpdate = documentsInDB.get.searchRequests
          .find(sr => sr.statementRequestId == "5b89895-f0da-4472-af5a-d84d340e7mn5")
          .get

        searchRequestAfterUpdate.searchSuccessful mustBe SearchResultStatus.no
        searchRequestAfterUpdate.searchDateTime must not be empty
      }
    }

    "return None when there is no document to update" in {
      val histSearchDoc         = getHistoricDocumentRequestSearchDoc
      val updatedDateTime       = Utils.dateTimeAsIso8601(LocalDateTime.now)
      val updatedSearchRequests = Set(
        SearchRequest(
          "GB123456789012",
          "5b89895-f0da-4472-af5a-d84d340e7mn5",
          SearchResultStatus.no,
          updatedDateTime,
          "AWSUnreachable",
          0
        ),
        SearchRequest("GB234567890121", "5c79895-f0da-4472-af5a-d84d340e7mn6", inProcess, emptyString, emptyString, 0)
      )

      val documentsInDB = for {
        _        <- historicDocumentRequestCache.collection.drop().toFuture()
        _        <- historicDocumentRequestCache.updateSearchRequestForStatementRequestId(
                      updatedSearchRequests,
                      histSearchDoc.searchID.toString
                    )
        finalDoc <-
          historicDocumentRequestCache.retrieveDocumentForStatementRequestID("5b89895-f0da-4472-af5a-d84d340e7mn5")
      } yield finalDoc

      whenReady(documentsInDB) { documentsInDB =>
        documentsInDB mustBe None
      }
    }
  }

  "updateResultsFoundStatus" should {
    "update the document correctly" in {
      val updatedDateTime = Utils.dateTimeAsIso8601(LocalDateTime.now)

      val updatedSearchRequests = Set(
        SearchRequest(
          "GB123456789012",
          "5b89895-f0da-4472-af5a-d84d340e7mn5",
          SearchResultStatus.no,
          updatedDateTime,
          "AWSUnreachable",
          0
        ),
        SearchRequest(
          "GB234567890121",
          "5c79895-f0da-4472-af5a-d84d340e7mn6",
          SearchResultStatus.no,
          updatedDateTime,
          "NoDocumentsFound",
          0
        )
      )

      val docToBeInserted = getHistoricDocumentRequestSearchDoc.copy(searchRequests = updatedSearchRequests)

      val documentsInDB = for {
        _          <- historicDocumentRequestCache.collection.drop().toFuture()
        _          <- historicDocumentRequestCache.insertDocument(docToBeInserted)
        updatedDoc <- historicDocumentRequestCache.updateResultsFoundStatus(
                        docToBeInserted.searchID.toString,
                        SearchResultStatus.no
                      )
      } yield updatedDoc

      whenReady(documentsInDB) { documentsInDB =>
        val updateDoc = documentsInDB.get
        documentsInDB.get.currentEori mustBe "GB123456789012"

        val searchRequestAfterUpdate = documentsInDB.get.searchRequests
          .find(sr => sr.statementRequestId == "5b89895-f0da-4472-af5a-d84d340e7mn5")
          .get

        searchRequestAfterUpdate.searchSuccessful mustBe SearchResultStatus.no
        searchRequestAfterUpdate.searchDateTime must not be empty

        updateDoc.resultsFound mustBe SearchResultStatus.no
        updateDoc.searchStatusUpdateDate must not be empty
      }
    }
  }

  "updateSearchReqsAndResultsFoundStatus" should {
    "update the document correctly" in {
      val updatedDateTime = Utils.dateTimeAsIso8601(LocalDateTime.now)

      val updatedSearchRequests = Set(
        SearchRequest(
          "GB123456789012",
          "5b89895-f0da-4472-af5a-d84d340e7mn5",
          SearchResultStatus.yes,
          updatedDateTime,
          "AWSUnreachable",
          0
        ),
        SearchRequest(
          "GB234567890121",
          "5c79895-f0da-4472-af5a-d84d340e7mn6",
          SearchResultStatus.no,
          updatedDateTime,
          "NoDocumentsFound",
          0
        )
      )

      val docToBeInserted = getHistoricDocumentRequestSearchDoc

      val documentsInDB = for {
        _          <- historicDocumentRequestCache.collection.drop().toFuture()
        _          <- historicDocumentRequestCache.insertDocument(docToBeInserted)
        updatedDoc <- historicDocumentRequestCache.updateSearchReqsAndResultsFoundStatus(
                        docToBeInserted.searchID.toString,
                        updatedSearchRequests,
                        SearchResultStatus.yes
                      )
      } yield updatedDoc

      whenReady(documentsInDB) { documentsInDB =>
        val updateDoc = documentsInDB.get
        documentsInDB.get.currentEori mustBe "GB123456789012"

        val searchRequestAfterUpdate = documentsInDB.get.searchRequests
          .find(sr => sr.statementRequestId == "5b89895-f0da-4472-af5a-d84d340e7mn5")
          .get

        searchRequestAfterUpdate.searchSuccessful mustBe SearchResultStatus.yes
        searchRequestAfterUpdate.searchDateTime must not be empty

        updateDoc.resultsFound mustBe SearchResultStatus.yes
        updateDoc.searchStatusUpdateDate must not be empty
      }
    }
  }

  private def getHistoricDocumentRequestSearchDoc: HistoricDocumentRequestSearch = {
    val searchID: UUID                     = UUID.randomUUID()
    val resultsFound                       = SearchResultStatus.inProcess
    val searchStatusUpdateDate: String     = emptyString
    val currentEori: String                = "GB123456789012"
    val params: Params                     = Params("2", "2021", "4", "2021", "DutyDefermentStatement", "1234567")
    val searchRequests: Set[SearchRequest] = Set(
      SearchRequest("GB123456789012", "5b89895-f0da-4472-af5a-d84d340e7mn5", inProcess, emptyString, emptyString, 0),
      SearchRequest("GB234567890121", "5c79895-f0da-4472-af5a-d84d340e7mn6", inProcess, emptyString, emptyString, 0)
    )

    HistoricDocumentRequestSearch(searchID, resultsFound, searchStatusUpdateDate, currentEori, params, searchRequests)
  }
}

object HistoricDocumentRequestSearchCacheSpec extends SpecBase {

  private val mockAppConfig = mock[AppConfig]
  private val mockConfig    = mock[Configuration]
  private val ttlValue      = 28

  private def buildFormRepository(mongoHost: String, mongoPort: Int): HistoricDocumentRequestSearchCache = {
    val databaseName = "historic-document-request-search"
    val mongoUri     = s"mongodb://$mongoHost:$mongoPort/$databaseName?heartbeatFrequencyMS=1000&rm.failover=default"
    new HistoricDocumentRequestSearchCache(mockAppConfig, MongoComponent(mongoUri))
  }
}
