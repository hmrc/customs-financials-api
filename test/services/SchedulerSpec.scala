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

package services

import akka.actor.ActorSystem
import config.AppConfig
import services.dec64.{FileUploadJobHandler, Scheduler}
import utils.SpecBase

import scala.concurrent.ExecutionContext
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.FiniteDuration

class SchedulerSpec extends SpecBase {

  "Scheduler" should {
    "schedule file upload submissions" in {
      val mockAppConfig = mock[AppConfig]
      val mockFileUploadJobHandler = mock[FileUploadJobHandler]
      val mockActorSystem = mock[ActorSystem]
      val mockScheduler = mock[akka.actor.Scheduler]
      when(mockAppConfig.housekeepingHours).thenReturn(12)
      when(mockAppConfig.fileUploadPerInstancePerSecond).thenReturn(0.2)
      when(mockActorSystem.scheduler).thenReturn(mockScheduler)
      when(mockScheduler.scheduleWithFixedDelay(any[FiniteDuration], any[FiniteDuration])(any[Runnable])(any[ExecutionContext]))
        .thenReturn(null)
      new Scheduler(mockAppConfig, mockFileUploadJobHandler, mockActorSystem)
      verify(mockActorSystem, times(2)).scheduler
    }
  }
}
