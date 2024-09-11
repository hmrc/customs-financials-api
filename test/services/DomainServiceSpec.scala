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

package services

import models.EORI
import models.responses._
import utils.SpecBase

class DomainServiceSpec extends SpecBase {

  "DomainService" should {

    "correctly handle .map for DeclarationDetails to domain.Declaration" in new Setup {

      val result: domain.Declaration = domainService.toDomainDetail(populatedDeclarationDetails)

      result.movementReferenceNumber mustBe "DeclarationID"
      result.importerEori mustBe Some(EORI("importerEORI123"))
      result.declarantEori mustBe EORI("declarantEORI123")
      result.declarantReference mustBe Some("reference123")
      result.date mustBe "2024-01-01"
      result.amount mustBe "500"

      result.taxGroups.head.taxGroupDescription mustBe "Customs"
      result.taxGroups.head.amount mustBe twoHundred
      result.taxGroups.head.taxTypes.head.taxTypeID mustBe "a"
      result.taxGroups.head.taxTypes.head.reasonForSecurity mustBe "b"
      result.taxGroups.head.taxTypes.head.amount mustBe hundred
    }

    "correctly handle empty DeclarationDetails" in new Setup {

      val result: domain.Declaration = domainService.toDomainDetail(emptyDeclarationDetails)

      result.movementReferenceNumber mustBe "EmptyDeclarationID"
      result.importerEori mustBe None
      result.declarantEori mustBe EORI("declarantEORI123")
      result.declarantReference mustBe None
      result.date mustBe "2024-01-01"
      result.amount mustBe "0"

      result.taxGroups mustBe empty
    }
  }

  trait Setup {

    val domainService = new DomainService()

    val twoHundred = "200.00"
    val hundred: BigDecimal = BigDecimal(100.00)

    val taxTypeDetail: TaxTypeDetail =
      TaxTypeDetail(reasonForSecurity = "b", taxTypeID = "a", amount = hundred)

    val taxTypeContainer: Seq[TaxTypeContainer] = Seq(TaxTypeContainer(taxTypeDetail))

    val taxGroupDetail: TaxGroupDetail = TaxGroupDetail(
      taxGroupDescription = "Customs",
      amount = twoHundred,
      taxTypes = taxTypeContainer
    )

    val taxGroupContainer: Seq[TaxGroupContainer] = Seq(TaxGroupContainer(taxGroupDetail))

    val populatedDeclarationDetails: DeclarationDetail = DeclarationDetail(
      declarationID = "DeclarationID",
      importerEORINumber = Some(EORI("importerEORI123")),
      declarantEORINumber = EORI("declarantEORI123"),
      declarantReference = Some("reference123"),
      postingDate = "2024-01-01",
      amount = "500",
      taxGroups = taxGroupContainer
    )

    val emptyDeclarationDetails: DeclarationDetail = DeclarationDetail(
      declarationID = "EmptyDeclarationID",
      importerEORINumber = None,
      declarantEORINumber = EORI("declarantEORI123"),
      declarantReference = None,
      postingDate = "2024-01-01",
      amount = "0",
      taxGroups = Seq.empty
    )
  }
}
