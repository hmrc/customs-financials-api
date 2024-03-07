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

import com.codahale.metrics.MetricRegistry
import models.claims.responses.{SctyClaimDetails, Goods => GoodsResponse, Reimbursement => ReimbursementResponse}
import org.mockito.scalatest.MockitoSugar
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpecLike
import org.scalatest.{BeforeAndAfterEach, OptionValues}
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.{DefaultAwaitTimeout, FakeRequest, FutureAwaits}
import play.api.inject.bind
import uk.gov.hmrc.play.bootstrap.metrics.Metrics

trait SpecBase
  extends AnyWordSpecLike
    with MockitoSugar
    with Matchers
    with FutureAwaits
    with DefaultAwaitTimeout
    with OptionValues
    with BeforeAndAfterEach {

  val reimbursementResponse: ReimbursementResponse = ReimbursementResponse("date", "10.00", "10.00", "method")

  val sctyClaimDetails: SctyClaimDetails = SctyClaimDetails(
    "caseNumber",
    Some("declarationId"),
    "Reason for security",
    "Procedure Code",
    "Closed",
    Some("Resolved-Refund"),
    Some(Seq(GoodsResponse("itemNumber", Some("description")))),
    "someEori",
    "someOtherEori",
    Some("claimaintEori"),
    Some("600000"),
    Some("600000"),
    Some("600000"),
    Some("600000"),
    "20221210",
    Some("name"),
    Some("email@email.com"),
    Some("20221012"),
    Some(Seq(reimbursementResponse)))

  def application(): GuiceApplicationBuilder = new GuiceApplicationBuilder().overrides(
    bind[Metrics].toInstance(new FakeMetrics)
  ).configure(
    "play.filters.csp.nonce.enabled" -> false,
    "auditing.enabled" -> "false",
    "microservice.metrics.graphite.enabled" -> "false",
    "metrics.enabled" -> "false")

  def requestWithoutHeaders[A](request: FakeRequest[A], keys: String*): FakeRequest[A] = {
    val incompleteHeaders = request.headers.remove(keys: _*)
    request.withHeaders(incompleteHeaders)
  }

  class FakeMetrics extends Metrics {
    override val defaultRegistry: MetricRegistry = new MetricRegistry
  }
}
