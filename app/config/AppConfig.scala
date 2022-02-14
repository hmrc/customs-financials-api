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

package config

import play.api.Configuration
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig
import javax.inject.{Inject, Singleton}

@Singleton
class AppConfig @Inject()(servicesConfig: ServicesConfig, configuration: Configuration) {

  lazy val appName: String = configuration.get[String]("appName")

  private def getConfString(path: String, default: String) = servicesConfig.getConfString(path, default)
  private def baseUrl(path: String) = servicesConfig.baseUrl(path)
  lazy val notificationCacheCollectionName = "notificationStore"
  lazy val fileUploadCacheCollectionName = "fileUploadStore"

  lazy val fileUploadPerInstancePerSecond: Double = configuration.get[Double]("fileUploadPerInstancePerSecond")
  lazy val housekeepingHours: Int = configuration.get[Int]("housekeepingHours")
  lazy val fileUploadMaxAgeMins: Int = configuration.get[Int]("fileUploadMaxAgeMins")

  lazy val hodsEndpoint: String = baseUrl("acc27") + getConfString("acc27.context-base", "/") + getConfString("acc27.endpoint", "/")

  lazy val onlyOpenItems: Boolean = configuration.get[Boolean]("features.onlyOpenItems")

  lazy val bearerToken: String = getConfString("acc27.bearer-token", "test")
  lazy val dataStoreEndpoint: String = baseUrl("customs-data-store") + getConfString("customs-data-store.context-base", "/")
  lazy val sendEmailEndpoint: String = baseUrl("customs-financials-email-throttler") + getConfString("customs-financials-email-throttler.context-base", "/") + "/enqueue-email"
  lazy val authUrl: String = baseUrl("auth")
  lazy val dataStoreServerToken: String = "Bearer " + getConfString("customs-data-store.server-token", "secret-token")

  lazy val acc24HistoricalStatementRetrievalEndpoint: String = baseUrl("acc24") + getConfString("acc24.context-base", "/") + "/accounts/cmdghistoricalstatementretrieval/v1"
  lazy val acc24HostHeader: Option[String] = configuration.getOptional[String]("microservice.services.acc24.host-header")
  lazy val acc24BearerToken: String = getConfString("acc24.bearer-token", "test")

  lazy val acc29GetStandingAuthoritiesEndpoint: String = baseUrl("acc29") + getConfString("acc29.context-base", "/") + "/accounts/getstandingauthoritydetails/v1"
  lazy val acc29HostHeader: Option[String] = configuration.getOptional[String]("microservice.services.acc29.host-header")
  lazy val acc29BearerToken: String = getConfString("acc29.bearer-token", "test")

  lazy val acc30ManageAccountAuthoritiesEndpoint: String = baseUrl("acc30") + getConfString("acc30.context-base", "/") + "/accounts/managestandingauthorities/v1"
  lazy val acc30HostHeader: Option[String] = configuration.getOptional[String]("microservice.services.acc30.host-header")
  lazy val acc30BearerToken: String = getConfString("acc30.bearer-token", "test")

  lazy val acc28GetGGATransactionEndpoint: String = baseUrl("acc28") + getConfString("acc28.context-base", "/") + "/accounts/getggatransactionlisting/v1"
  lazy val acc28HostHeader: Option[String] = configuration.getOptional[String]("microservice.services.acc28.host-header")
  lazy val acc28BearerToken: String = getConfString("acc28.bearer-token", "test")

  lazy val acc31GetCashAccountTransactionListingEndpoint: String = baseUrl("acc31") + getConfString("acc31.context-base", "/") + "/accounts/getcashaccounttransactionlisting/v1"
  lazy val acc31HostHeader: Option[String] = configuration.getOptional[String]("microservice.services.acc31.host-header")
  lazy val acc31BearerToken: String = getConfString("acc31.bearer-token", "test")

  lazy val acc38DutyDefermentContactDetailsEndpoint: String = baseUrl("acc38") + getConfString("acc38.context-base", "/") + "/accounts/getcorrespondenceaddress/v1"
  lazy val acc38HostHeader: Option[String] = configuration.getOptional[String]("microservice.services.acc38.host-header")
  lazy val acc38BearerToken: String = getConfString("acc38.bearer-token", "test")

  lazy val acc37UpdateAccountContactDetailsEndpoint: String = baseUrl("acc37") + getConfString("acc37.context-base", "/") + "/accounts/amendcorrespondenceaddress/v1"
  lazy val acc37HostHeader: Option[String] = configuration.getOptional[String]("microservice.services.acc37.host-header")
  lazy val acc37BearerToken: String = getConfString("acc37.bearer-token", "test")

  lazy val sub21CheckEORIValidEndpoint: String = baseUrl("sub21") + getConfString("sub21.context-base", "/") + getConfString("sub21.historicEoriEndpoint", "/")
  lazy val sub21BearerToken: String = getConfString("sub21.bearer-token", "test")

  lazy val sub09GetSubscriptionsEndpoint: String = baseUrl("sub09") + getConfString("sub09.context-base", "/") + getConfString("sub09.subscriptionEndpoint", "/")
  lazy val sub09HostHeader: Option[String] = configuration.getOptional[String]("microservice.services.acc37.host-header")
  lazy val sub09BearerToken: String = getConfString("sub09.bearer-token", "test")
  lazy val dbTimeToLiveInSeconds: Int = configuration.getOptional[Int]("mongodb.timeToLiveInSeconds").getOrElse(30 * 24 * 60 * 60)

  lazy val tpi01GetReimbursementClaimsEndpoint: String = baseUrl("tpi01") + getConfString("tpi01.context-base", "/") + "/tpi/getreimbursementclaims/v1"
  lazy val tpi01HostHeader: Option[String] = configuration.getOptional[String]("microservice.services.tpi01.host-header")
  lazy val tpi01BearerToken: String = getConfString("tpi01.bearer-token", "test")

  lazy val tpi02GetReimbursementClaimsEndpoint: String = baseUrl("tpi02") + getConfString("tpi02.context-base", "/") + "/tpi/getspecificclaim/v1"
  lazy val tpi02HostHeader: Option[String] = configuration.getOptional[String]("microservice.services.tpi02.host-header")
  lazy val tpi02BearerToken: String = getConfString("tpi02.bearer-token", "test")

  lazy val ccsFileUploadEndpoint: String = baseUrl("dec64") + getConfString("dec64.context-base", "/") + "/filetransfer/init/v1"

}
