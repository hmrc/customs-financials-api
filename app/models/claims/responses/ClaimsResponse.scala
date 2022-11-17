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

package models.claims.responses

import domain.tpi01.ResponseDetail
import play.api.libs.json.{Json, OFormat}

case class ClaimsResponse(sctyClaims: Seq[SctyClaimItem], ndrcClaims: Seq[NdrcClaimItem])

object ClaimsResponse {
  implicit val format: OFormat[ClaimsResponse] = Json.format[ClaimsResponse]

  def fromTpi01Response(responseDetail: ResponseDetail): ClaimsResponse = {

    val scty = responseDetail.CDFPayCase
      .flatMap(_.SCTYCases)
      .getOrElse(Seq.empty)
      .map(SctyClaimItem.fromTpi01Response)
      .filter(_.declarationID.isDefined)
    val ndrc = responseDetail.CDFPayCase
      .flatMap(_.NDRCCases)
      .getOrElse(Seq.empty)
      .map(NdrcClaimItem.fromTpi01Response)
      .filter(_.declarationID.isDefined)

    ClaimsResponse(scty, ndrc)
  }
}
