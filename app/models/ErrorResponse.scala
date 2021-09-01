/*
 * Copyright 2021 HM Revenue & Customs
 *
 */

package models

sealed trait ErrorResponse extends Throwable

case object ExceededThresholdErrorException extends ErrorResponse

case object NoAssociatedDataException extends ErrorResponse