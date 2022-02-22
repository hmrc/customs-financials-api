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

import javax.inject.Inject
import models.dec64.FileUploadRequest
import play.api.libs.json.Json
import play.api.mvc.{Action, ControllerComponents}
import services.dec64.FileUploadCache
import uk.gov.hmrc.mongo.play.json.Codecs.logger
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import scala.concurrent.ExecutionContext

class FileUploadController @Inject()(cc: ControllerComponents,
                                     fileUploadCache: FileUploadCache
                                    )(implicit ec: ExecutionContext) extends BackendController(cc) {


  def enqueueUploadedFiles(): Action[FileUploadRequest] = Action.async(parse.json[FileUploadRequest]) { implicit request =>
    logger.info(s"enqueueUploadedFiles: file upload request enqueued")
    fileUploadCache.enqueueFileUploadJob(request.body).map {
      case true =>
        Accepted(Json.obj("Status" -> "Ok", "message" -> "Uploaded files successfully queued"))
      case false =>
        BadRequest
    }
  }
}
