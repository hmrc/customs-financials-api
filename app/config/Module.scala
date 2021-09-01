/*
 * Copyright 2021 HM Revenue & Customs
 *
 */

package config

import com.google.inject.AbstractModule
import services.{DefaultNotificationCache, NotificationCache}

class Module extends AbstractModule {

  override def configure(): Unit = {
    bind(classOf[NotificationCache]).to(classOf[DefaultNotificationCache]).asEagerSingleton()
  }
}
