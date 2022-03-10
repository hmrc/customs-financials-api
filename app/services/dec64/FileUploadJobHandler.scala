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

package services.dec64

import config.AppConfig
import javax.inject.{Inject, Singleton}
import play.api.{Logger, LoggerLike}
import scala.concurrent.{ExecutionContext, Future}


@Singleton
class FileUploadJobHandler @Inject()(fileUploadCache: FileUploadCache,
                                     fileUploadService: FileUploadService,
                                     appConfig: AppConfig)(implicit ec: ExecutionContext) {

  val log: LoggerLike = Logger(this.getClass)

  def processJob(): Future[Unit] = {
    for {
      fileUploadJob <- fileUploadCache.nextJob if fileUploadJob.isDefined
      uploadedFileRequest = fileUploadJob.get
      fileSubmitted <- fileUploadService.submitFileToDec64(uploadedFileRequest.fileUploadDetail)
      id = fileUploadJob.get._id
    } yield {
      val failCount = uploadedFileRequest.failedSubmission
      fileSubmitted match {
        case true =>
          log.info(s"File Submission to CSS was successful delete job starting")
          fileUploadCache.deleteJob(id)
        case false if failCount >= appConfig.fileUploadFailCount =>
          log.info(s"File Submission to CSS failed 5 times deleting job")
          fileUploadCache.deleteJob(id)
        case _ =>
          log.info(s"File Submission to CCS failed count number: $failCount")
          fileUploadCache.resetProcessingFailedUpload(id)
      }
    }
  }

  def houseKeeping(): Unit = fileUploadCache.resetProcessing

}
