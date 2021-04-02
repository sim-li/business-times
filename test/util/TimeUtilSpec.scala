package util

import org.scalatestplus.play.PlaySpec
import TimeUtil._

class TimeUtilSpec extends PlaySpec {
  "truncate time" should {
    "produce time object with seconds/milliseconds truncated" in {
      val time = truncatedTime(hour = 9, minute = 30)

      assert(time.getHourOfDay() === 9)
      assert(time.getMinuteOfHour() === 30)
      assert(time.getSecondOfMinute === 0)
      assert(time.getMillisOfSecond === 0)
    }

    "compare equal when constructing two objects with same hour and minute" in {
      val someTime = truncatedTime(hour = 9, minute = 30)
      val anotherTime = truncatedTime(hour = 9, minute = 30)

      assert(someTime === anotherTime)
    }
  }

  "to epoch time conversion" should {
    "produce time object with UTC 0 offset" in {
      assert(epochToLocalTime(32400) === truncatedTime(9, 0))
    }
  }
}
