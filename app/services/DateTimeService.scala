/*
 * Copyright 2021 HM Revenue & Customs
 *
 */

package services

import org.joda.time.{DateTime, DateTimeZone}

import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.time.{Instant, LocalDateTime}
import javax.inject.{Inject, Singleton}

@Singleton
class DateTimeService @Inject()() {

  def now(): LocalDateTime = LocalDateTime.now()

  def timeStamp(): Long = Instant.now.toEpochMilli

  def currentDateTimeAsIso8601: String = {
    s"${DateTimeFormatter.ISO_DATE_TIME.format(now().truncatedTo(ChronoUnit.SECONDS))}Z"
  }

  def utcDateTime = DateTime.now(DateTimeZone.UTC)

}
