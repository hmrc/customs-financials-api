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

import domain.tpi02.Reimbursement
import domain.tpi02.ndrc._
import domain.tpi02.scty.{Goods, SCTYCase}
import models.claims.responses.{NdrcClaimDetails, NdrcClaimItem, SctyClaimDetails, Reimbursement => ReimbursementResponse, Goods => GoodsResponse}
import org.mockito.scalatest.MockitoSugar
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpecLike
import org.scalatest.{BeforeAndAfterEach, OptionValues}
import play.api.test.{DefaultAwaitTimeout, FutureAwaits}

trait SpecBase extends AnyWordSpecLike
  with MockitoSugar
  with Matchers
  with FutureAwaits
  with DefaultAwaitTimeout
  with OptionValues
  with BeforeAndAfterEach {

  val reimbursement: Reimbursement = Reimbursement("date", "10.00", "10.00", "method")
  val reimbursementResponse: ReimbursementResponse = ReimbursementResponse("date", "10.00", "10.00", "method")

  val ndrcCase: NDRCCase = NDRCCase(
    NDRCDetail(
      CDFPayCaseNumber = "CaseNumber",
      declarationID = Some("DeclarationId"),
      claimType = "NDRC",
      caseType = "C285",
      caseStatus = "Resolved-Approved",
      descOfGoods = Some("description of goods"),
      descOfRejectedGoods = Some("description of rejected goods"),
      declarantEORI = "SomeEori",
      importerEORI = "SomeOtherEori",
      claimantEORI = Some("ClaimaintEori"),
      basisOfClaim = Some("basis of claim"),
      claimStartDate = "20221012",
      claimantName = Some("name"),
      claimantEmailAddress = Some("email@email.com"),
      closedDate = Some("20221112"),
      MRNDetails = Some(Seq(
        ProcedureDetail("MRN", true)
      )),
      entryDetails = Some(Seq(
        EntryDetail("entryNumber", true)
      )
      ),
      reimbursement = Some(Seq(reimbursement))
    ),
    NDRCAmounts(
      Some("600000"),
      Some("600000"),
      Some("600000"),
      Some("600000"),
      Some("600000"),
      Some("600000"),
      Some("600000"),
      Some("600000"),
      Some("600000"),
    )
  )
  val sctyCase: SCTYCase = SCTYCase(
    "caseNumber",
    Some("declarationId"),
    "Reason for security",
    "Procedure Code",
    "Resolved-Refund",
    Some(Seq(Goods("itemNumber", Some("description")))),
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
    Some(Seq(reimbursement))
  )

  val ndrcClaimDetails: NdrcClaimDetails = NdrcClaimDetails(
    CDFPayCaseNumber = "CaseNumber",
    declarationID = Some("DeclarationId"),
    claimType = "NDRC",
    caseType = "C285",
    caseStatus = "Closed",
    caseSubStatus = Some("Approved"),
    descOfGoods = Some("description of goods"),
    descOfRejectedGoods = Some("description of rejected goods"),
    totalClaimAmount = Some("600000"),
    declarantEORI = "SomeEori",
    importerEORI = "SomeOtherEori",
    claimantEORI = Some("ClaimaintEori"),
    basisOfClaim = Some("basis of claim"),
    claimStartDate = "20221012",
    claimantName = Some("name"),
    claimantEmailAddress = Some("email@email.com"),
    closedDate = Some("20221112"),
    reimbursements = Some(Seq(reimbursementResponse))
  )

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
    Some(Seq(reimbursementResponse))
  )
}
