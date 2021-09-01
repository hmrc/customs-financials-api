/*
 * Copyright 2021 HM Revenue & Customs
 *
 */

package utils

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
  with BeforeAndAfterEach
