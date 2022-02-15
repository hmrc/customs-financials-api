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

package services.ccs

import javax.inject.{Inject, Singleton}
import play.api.{Logger, LoggerLike}

import scala.concurrent.{ExecutionContext, Future}


@Singleton
class FileUploadJobHandler @Inject()(fileUploadCache: FileUploadCache,
                                     ccsService: CcsService)(implicit ec: ExecutionContext) {

  val log: LoggerLike = Logger(this.getClass)

  def processJob(): Future[Unit] = {
    for {
      fileUploadJob <- fileUploadCache.nextJob if fileUploadJob.isDefined
      uploadedFileRequest = fileUploadJob.get
      fileSubmitted <- ccsService.submitFileToCcs(uploadedFileRequest)
      id = fileUploadJob.get.id
    } yield {
      if (fileSubmitted) {
        log.info(s"File Submission to CSS was successful delete job starting")
        fileUploadCache.deleteJob(id)
      } else {
        log.info(s"File Submission to CSS failed delete job not ran")
      }
    }
  }

  def houseKeeping(): Unit = fileUploadCache.resetProcessing

}
