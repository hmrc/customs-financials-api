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

package config

import play.api.Application
import utils.SpecBase

class AppConfigSpec extends SpecBase {

  "euEoriEnabled" should {
    "return the correct value" in new Setup {
      appConfig.isEuEoriEnabled mustBe false
    }
  }

  "mongoHistDocSearchCollectionName" should {
    "return correct name for the collection" in new Setup {
      appConfig.mongoHistDocSearchCollectionName mustBe "historic-document-request-search"
    }
  }

  "mongoHistDocSearchTtl" should {
    "return correct value of 20 days in seconds" in new Setup {
      appConfig.mongoHistDocSearchTtl mustBe 1728000
    }
  }

  "acc24BearerToken" should {
    "return correct value" in new Setup {
      appConfig.ssfnBearerToken mustBe "test1234567"
    }
  }

  "acc45" should {
    "return correct BearerToken value" in new Setup {
      appConfig.acc45BearerToken mustBe "test1234567"
    }

    "return correct endpoint" in new Setup {
      appConfig.acc45CashAccountStatementRequestEndpoint mustBe
        "http://localhost:9753/customs-financials-hods-stub/accounts/cashaccountstatementrequest/v1"
    }

  }

  "bearerTokenValuePrefix" should {
    "return correct value" in new Setup {
      appConfig.bearerTokenValuePrefix mustBe "Bearer"
    }
  }

  "ssfnForwardedHost" should {
    "return correct value" in new Setup {
      appConfig.ssfnForwardedHost mustBe Option("CDDM")
    }
  }

  "secureMessage" should {
    "Endpoint returns correct value" in new Setup {
      appConfig.secureMessageEndpoint mustBe "http://localhost:9051/secure-messaging/v4/message"
    }

    "Host returns correct value" in new Setup {
      appConfig.secureMessageHostHeader mustBe None
    }

    "BearerToken returns correct value" in new Setup {
      appConfig.secureMessageBearerToken mustBe "test1234567"
    }
  }

  "acc44BearerToken" should {
    "return correct value" in new Setup {
      appConfig.acc44BearerToken mustBe "test1234567"
    }
  }

  "acc44CashTransactionSearchEndpoint" should {
    "return correct endpoint url" in new Setup {
      appConfig.acc44CashTransactionSearchEndpoint mustBe
        "http://localhost:9753/customs-financials-hods-stub/accounts/cashaccounttransactionsearch/v1"
    }
  }

  trait Setup {
    val app: Application     = application().build()
    val appConfig: AppConfig = app.injector.instanceOf[AppConfig]
  }
}
