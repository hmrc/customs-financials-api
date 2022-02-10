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

package services.ccs

import uk.gov.hmrc.http.{HeaderCarrier, HeaderNames}

class CcsHeaders {
  def getHeaders(headerCarrier: HeaderCarrier): Seq[(String, String)] =
    List(
      headerCarrier.requestId.map(rid => headerCarrier.names.xRequestId -> rid.value),
      headerCarrier.sessionId.map(sid => headerCarrier.names.xSessionId -> sid.value),
      headerCarrier.forwarded.map(f => headerCarrier.names.xForwardedFor -> f.value),
      Some(headerCarrier.names.xRequestChain -> headerCarrier.requestChain.value),
      headerCarrier.authorization.map(auth => headerCarrier.names.authorisation -> auth.value),
      headerCarrier.trueClientIp.map(HeaderNames.trueClientIp -> _),
      headerCarrier.trueClientPort.map(HeaderNames.trueClientPort -> _),
      headerCarrier.gaToken.map(HeaderNames.googleAnalyticTokenId -> _),
      headerCarrier.gaUserId.map(HeaderNames.googleAnalyticUserId -> _),
      headerCarrier.deviceID.map(HeaderNames.deviceID -> _),
      headerCarrier.akamaiReputation.map(HeaderNames.akamaiReputation -> _.value)
    ).flatten ++ headerCarrier.extraHeaders ++ headerCarrier.otherHeaders
}
