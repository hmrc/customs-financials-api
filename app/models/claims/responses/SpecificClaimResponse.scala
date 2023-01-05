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

package models.claims.responses

import domain.tpi02.ResponseDetail
import play.api.libs.json.{Json, OFormat}

case class SpecificClaimResponse(
                                  CDFPayService: String,
                                  CDFPayCaseFound: Boolean,
                                  NDRCCase: Option[NdrcClaimDetails],
                                  SCTYCase: Option[SctyClaimDetails]
)

object SpecificClaimResponse {
  implicit val format:  OFormat[SpecificClaimResponse] = Json.format[SpecificClaimResponse]

  def fromTpi02Response(responseDetail: ResponseDetail): SpecificClaimResponse = {
      SpecificClaimResponse(
        responseDetail.CDFPayService,
        responseDetail.CDFPayCaseFound,
        responseDetail.NDRCCase.map(NdrcClaimDetails.fromTpi02Response),
        responseDetail.SCTYCase.map(SctyClaimDetails.fromTpi02Response)
      )
  }
}
