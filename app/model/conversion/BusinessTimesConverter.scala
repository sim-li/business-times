package model.conversion

import model.BusinessTime
import model.Types.Weekday
import model.external.OpeningOrClosingTime
import util.TimeUtil.epochToLocalTime

object BusinessTimesConverter {
  case class TimeWithWeekday(weekday: Weekday, openingOrClosingType: String, epoch: Int)

  def convert(
      timesByWeekday: Map[Weekday, List[OpeningOrClosingTime]]
  ): Map[Weekday, List[BusinessTime]] = {
    val sortedTimesWithWeekDay = toChronologicallySortedList(timesByWeekday)

    val openingAndClosingTimePairs = sortedTimesWithWeekDay.grouped(2).toList

    assertHasOpeningFollowedByClosingTime(openingAndClosingTimePairs)

    toBusinessTimesByWeekday(openingAndClosingTimePairs)
  }

  private def toChronologicallySortedList(
      timesByWeekday: Map[Weekday, List[OpeningOrClosingTime]]
  ) = {
    val timesWithWeekday = timesByWeekday
      .collect {
        case (weekday, openingOrClosingTime) =>
          openingOrClosingTime.map(t =>
            TimeWithWeekday(
              weekday = weekday,
              openingOrClosingType = t.`type`,
              epoch = t.value
            )
          )
      }
      .flatten
      .toList

    timesWithWeekday sortBy (t => (t.weekday, t.epoch))
  }

  private def assertHasOpeningFollowedByClosingTime(
      openingAndClosingTimePairs: List[List[TimeWithWeekday]]
  ) = {
    openingAndClosingTimePairs
      .foreach((t: List[TimeWithWeekday]) => {
        if (t.length < 2) {
          throw OpeningTimesMalformedException("Uneven number of opening and closing times")
        }
        if (!isOpeningFollowedByClosingTime(t)) {
          throw OpeningTimesMalformedException("Opening time was not followed by closing time")
        }
      })
  }

  private def isOpeningFollowedByClosingTime(t: List[TimeWithWeekday]) = {
    t(0).openingOrClosingType == "open" && t(1).openingOrClosingType == "close"
  }

  private def toBusinessTimesByWeekday(
      openingAndClosingTimePairs: List[List[TimeWithWeekday]]
  ) = {
    val businessTimeByWeekdayPairs = openingAndClosingTimePairs
      .map(t => {
        val businessOpening :: businessClosing :: _ = t
        // This is the logic we want for our view: Closing time is grouped with the
        // same weekday as the opening time, even if it's technically on the following week day.
        // E.g.: (Monday 11:00PM - Tuesday 2AM ) would both be grouped with Monday (Monday 11PM - 2AM)
        businessOpening.weekday -> BusinessTime(
          epochToLocalTime(businessOpening.epoch),
          epochToLocalTime(businessClosing.epoch)
        )
      })

    businessTimeByWeekdayPairs
      .groupBy(t => t._1)
      .view
      .mapValues(t => t.map(d => d._2))
      .toMap
  }
}
