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

package utils
import play.api.Configuration
import uk.gov.hmrc.http.test.WireMockSupport
import org.scalatest.Suite
import com.github.tomakehurst.wiremock.client.WireMock.{
  deleteRequestedFor, getRequestedFor, postRequestedFor, put, putRequestedFor, urlPathMatching
}
import com.github.tomakehurst.wiremock.http.RequestMethod
import com.github.tomakehurst.wiremock.http.RequestMethod.{DELETE, GET, POST, PUT}

trait WireMockSupportProvider extends WireMockSupport {
  this: Suite =>

  val X_FORWARDED_HOST = "X-Forwarded-Host"
  val CONTENT_TYPE     = "Content-Type"
  val ACCEPT           = "Accept"
  val AUTHORIZATION    = "Authorization"

  def config: Configuration

  protected def verifyEndPointUrlHit(urlToVerify: String, methodType: RequestMethod = GET): Unit =
    wireMockServer.verify(
      methodType match {
        case GET    => getRequestedFor(urlPathMatching(urlToVerify))
        case POST   => postRequestedFor(urlPathMatching(urlToVerify))
        case PUT    => putRequestedFor(urlPathMatching(urlToVerify))
        case DELETE => deleteRequestedFor(urlPathMatching(urlToVerify))
        case _      => throw new RuntimeException("Invalid method type")
      }
    )
}
