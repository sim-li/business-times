package util

import org.joda.time.{DateTimeZone, Instant, LocalTime}

object TimeUtil {
  // We want to make sure that always when we use epochToLocalTime we're in GMT-0/UTC-0.
  // This produces the time output specified in the requirements but could easily be adapted if necessary.
  setTimeZoneToUTCZero

  def epochToLocalTime(epochSeconds: Int): LocalTime = {
    val localTime = Instant.ofEpochSecond(epochSeconds).toDateTime
    truncatedTime(
      hour = localTime.getHourOfDay(),
      minute = localTime.getMinuteOfHour()
    )
  }

  def setTimeZoneToUTCZero = {
    val utc = DateTimeZone.UTC
    utc.adjustOffset(0L, true)
    DateTimeZone.setDefault(utc)
  }

  def truncatedTime(hour: Int, minute: Int): LocalTime = {
    new LocalTime()
      .withHourOfDay(hour)
      .withMinuteOfHour(minute)
      .withSecondOfMinute(0)
      .withMillisOfSecond(0)
  }
}
