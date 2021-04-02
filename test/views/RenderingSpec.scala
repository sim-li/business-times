package views

import model.BusinessTime
import org.joda.time.{LocalTime}
import org.scalatestplus.play.PlaySpec
import views.Rendering.{renderBusinessTimes, renderBusinessTimesForAllWeekdays, renderTime}

class RenderingSpec extends PlaySpec {
  "rendering" should {
    "render business times for all weekdays" in {
      val renderedBusinessTimesForWeekdays = renderBusinessTimesForAllWeekdays(
        Map(
          "monday" -> List(
            BusinessTime(
              openingTime = LocalTime.parse("9:00"),
              closingTime = LocalTime.parse("16:00")
            ),
            BusinessTime(
              openingTime = LocalTime.parse("19:00"),
              closingTime = LocalTime.parse("22:00")
            )
          ),
          "saturday" -> List(
            BusinessTime(
              openingTime = LocalTime.parse("9:00"),
              closingTime = LocalTime.parse("16:00")
            )
          )
        )
      )

      assert(
        renderedBusinessTimesForWeekdays === List(
          "Monday: 9:00 AM - 4:00 PM, 7:00 PM - 10:00 PM",
          "Tuesday: Closed",
          "Wednesday: Closed",
          "Thursday: Closed",
          "Friday: Closed",
          "Saturday: 9:00 AM - 4:00 PM",
          "Sunday: Closed"
        )
      )
    }

    "render business times with dash seperation" in {
      val renderedBusinessTimes = renderBusinessTimes(
        List(
          BusinessTime(
            openingTime = LocalTime.parse("9:00"),
            closingTime = LocalTime.parse("16:00")
          )
        )
      )

      assert(renderedBusinessTimes === "9:00 AM - 4:00 PM")
    }

    "render time with hours/minutes plus 12 hour time period" in {
      val renderedTime = renderTime(LocalTime.parse("14:00"))

      assert(renderedTime === "2:00 PM")
    }
  }
}
