package views

import model.BusinessTime
import model.Types.Weekday
import org.joda.time.LocalTime

object Rendering {
  def renderBusinessTimesForAllWeekdays(
      businessTimesForWeekday: Map[Weekday, List[BusinessTime]]
  ): List[String] = {
    List(
      "monday",
      "tuesday",
      "wednesday",
      "thursday",
      "friday",
      "saturday",
      "sunday"
    ).map(dayOfWeek => {
      val timesFormatted = businessTimesForWeekday.get(dayOfWeek) match {
        case Some(businessTimes: List[BusinessTime]) => renderBusinessTimes(businessTimes)
        case _                                       => "Closed"
      }
      s"${dayOfWeek.capitalize}: $timesFormatted"
    })
  }

  def renderBusinessTimes(businessTimes: List[BusinessTime]) = {
    businessTimes
      .map(b => s"${renderTime(b.openingTime)} - ${renderTime(b.closingTime)}")
      .mkString(", ")
  }

  def renderTime(time: LocalTime) = {
    time.toString("h:mm a").toUpperCase
  }
}
