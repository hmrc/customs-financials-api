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

package controllers

import models.css.FileUploadRequest
import play.api.Application
import play.api.http.Status
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.Result
import play.api.test.Helpers._
import play.api.test.{FakeHeaders, FakeRequest, Helpers}
import services.ccs.FileUploadCache
import utils.SpecBase

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class FileUploadControllerSpec extends SpecBase {

  "enqueueUploadedFiles" should {
    "return 204 status when file upload successful" in new Setup {
      when(mockFileUploadCache.enqueueFileUploadJob(any))
        .thenReturn(Future.successful(true))

      val result: Future[Result] = controller.enqueueUploadedFiles()(fakeRequest)
      status(result) mustBe Status.ACCEPTED
    }

    "return 400 for bad request error" in new Setup {
      when(mockFileUploadCache.enqueueFileUploadJob(any))
        .thenReturn(Future.successful(false))

      val result: Future[Result] = controller.enqueueUploadedFiles()(fakeRequest)
      status(result) mustBe Status.BAD_REQUEST
    }
  }

  trait Setup {

    val requestJson: JsValue = Json.parse(
      """{
        |   "id":"id",
        |   "eori":"eori",
        |   "caseNumber":"casenumber",
        |   "declarationId":"MRN",
        |   "entryNumber": false,
        |   "applicationName":"appName",
        |   "documentType":"docType",
        |   "uploadedFiles":[
        |   {
        |      "upscanReference":"upscanRef",
        |      "downloadUrl":"url",
        |      "uploadTimestamp":"String",
        |      "checksum":"sum",
        |      "fileName":"filename",
        |      "fileMimeType":"mimeType",
        |      "fileSize":12,
        |      "description":"Additional Documents"
        |   }
        |]
        |}""".stripMargin)


    val fakeRequest: FakeRequest[FileUploadRequest] = FakeRequest("POST", "/", FakeHeaders(), requestJson.as[FileUploadRequest])

    val mockFileUploadCache: FileUploadCache = mock[FileUploadCache]

    val controller: FileUploadController = new FileUploadController(Helpers.stubControllerComponents(), mockFileUploadCache)

    val app: Application = GuiceApplicationBuilder().configure(
      "microservice.metrics.enabled" -> false,
      "metrics.enabled" -> false,
      "auditing.enabled" -> false
    ).build()
  }
}
