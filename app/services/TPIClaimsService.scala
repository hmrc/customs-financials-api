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

import connectors.{Tpi01Connector, Tpi02Connector}
import domain._
import javax.inject.{Inject, Singleton}
import models.EORI
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class TPIClaimsService @Inject()(tpi01Connector: Tpi01Connector,
                                 tpi02Connector: Tpi02Connector)(implicit executionContext: ExecutionContext) {

  def getClaims(eori: EORI): Future[Option[Seq[tpi01.CDFPayCaseDetail]]] = {
    tpi01Connector.retrieveReimbursementClaims(eori).map{ response =>
      response.getReimbursementClaimsResponse.responseDetail.map { detail =>
        detail.CDFPayCases.getOrElse(Seq.empty).map(_.CDFPayCase.toCDSPayCaseDetail)
      }
    }
  }

  def getSpecificClaim(cdfPayService: String, cdfPayCaseNumber: String): Future[Option[tpi02.CDFPayCase]] = {
    tpi02Connector.retrieveSpecificClaim(cdfPayService, cdfPayCaseNumber).map { response =>
      response.getSpecificClaimResponse.responseDetail.flatMap { detail =>
        detail.CDFPayCase.map(_.toCDSPayCaseDetail)
      }
    }
  }
}
