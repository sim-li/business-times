package model.conversion

import model.BusinessTime
import model.external.OpeningOrClosingTime
import org.scalatestplus.play.PlaySpec
import util.TimeUtil.truncatedTime

class BusinessTimesConverterSpec extends PlaySpec {
  "converter" should {
    "transform data" when {
      "closed all week" in {
        val businessTimesByWeekDay: Map[String, List[BusinessTime]] =
          BusinessTimesConverter.convert(
            Map()
          )

        assert(
          businessTimesByWeekDay === Map()
        )
      }

      "opening on one day of the week" in {
        val businessTimesByWeekDay: Map[String, List[BusinessTime]] =
          BusinessTimesConverter.convert(
            Map(
              "monday" -> List(
                OpeningOrClosingTime("open", 32400),
                OpeningOrClosingTime("close", 37800)
              )
            )
          )

        assert(
          businessTimesByWeekDay === Map(
            "monday" ->
              List(
                BusinessTime(
                  openingTime = truncatedTime(hour = 9, minute = 0),
                  closingTime = truncatedTime(hour = 10, minute = 30)
                )
              )
          )
        )
      }

      "opening and closing several times on one day" in {
        val businessTimesByWeekday =
          BusinessTimesConverter.convert(
            Map(
              "monday" -> List( //9AM-11AM, 4PM-11PM
                OpeningOrClosingTime("open", 32400),
                OpeningOrClosingTime("close", 39600),
                OpeningOrClosingTime("open", 57600),
                OpeningOrClosingTime("close", 82800)
              )
            )
          )

        assert(
          businessTimesByWeekday === Map(
            "monday" ->
              List(
                BusinessTime(
                  openingTime = truncatedTime(hour = 9, minute = 0),
                  closingTime = truncatedTime(hour = 11, minute = 0)
                ),
                BusinessTime(
                  openingTime = truncatedTime(hour = 16, minute = 0),
                  closingTime = truncatedTime(hour = 23, minute = 0)
                )
              )
          )
        )
      }

      "opening on every day of the week" in {
        val businessTimesByWeekday =
          BusinessTimesConverter.convert(
            Map(
              "monday" -> List(
                OpeningOrClosingTime("open", 32400), //9
                OpeningOrClosingTime("close", 39600) //11
              ),
              "tuesday" -> List(
                OpeningOrClosingTime("open", 43200), //12
                OpeningOrClosingTime("close", 46800) //13
              ),
              "wednesday" -> List(
                OpeningOrClosingTime("open", 50400), //14
                OpeningOrClosingTime("close", 54000) //15
              ),
              "thursday" -> List(
                OpeningOrClosingTime("open", 57600), //16
                OpeningOrClosingTime("close", 61200) //17
              ),
              "friday" -> List(
                OpeningOrClosingTime("open", 64800), //18
                OpeningOrClosingTime("close", 68400) //19
              ),
              "saturday" -> List(
                OpeningOrClosingTime("open", 72000), //20
                OpeningOrClosingTime("close", 75600) //21
              ),
              "sunday" -> List(
                OpeningOrClosingTime("open", 79200), //22
                OpeningOrClosingTime("close", 82800) //23
              )
            )
          )

        assert(
          businessTimesByWeekday === Map(
            "monday" ->
              List(
                BusinessTime(
                  openingTime = truncatedTime(hour = 9, minute = 0),
                  closingTime = truncatedTime(hour = 11, minute = 0)
                )
              ),
            "tuesday" ->
              List(
                BusinessTime(
                  openingTime = truncatedTime(hour = 12, minute = 0),
                  closingTime = truncatedTime(hour = 13, minute = 0)
                )
              ),
            "wednesday" ->
              List(
                BusinessTime(
                  openingTime = truncatedTime(hour = 14, minute = 0),
                  closingTime = truncatedTime(hour = 15, minute = 0)
                )
              ),
            "thursday" ->
              List(
                BusinessTime(
                  openingTime = truncatedTime(hour = 16, minute = 0),
                  closingTime = truncatedTime(hour = 17, minute = 0)
                )
              ),
            "friday" ->
              List(
                BusinessTime(
                  openingTime = truncatedTime(hour = 18, minute = 0),
                  closingTime = truncatedTime(hour = 19, minute = 0)
                )
              ),
            "saturday" ->
              List(
                BusinessTime(
                  openingTime = truncatedTime(hour = 20, minute = 0),
                  closingTime = truncatedTime(hour = 21, minute = 0)
                )
              ),
            "sunday" ->
              List(
                BusinessTime(
                  openingTime = truncatedTime(hour = 22, minute = 0),
                  closingTime = truncatedTime(hour = 23, minute = 0)
                )
              )
          )
        )
      }

      "closing time is one day after opening time" in {
        val businessTimesByWeekDay =
          BusinessTimesConverter.convert(
            Map(
              "friday" -> List(
                OpeningOrClosingTime("open", 36000)
              ),
              "saturday" -> List(
                OpeningOrClosingTime("close", 3600)
              )
            )
          )

        assert(
          businessTimesByWeekDay === Map(
            "friday" ->
              List(
                BusinessTime(
                  openingTime = truncatedTime(hour = 10, minute = 0),
                  closingTime = truncatedTime(hour = 1, minute = 0)
                )
              )
          )
        )
      }
    }
  }

  "throw an exception" when {
    "uneven number of opening and closing times found (e.g. business stays open)" in {
      assertThrows[OpeningTimesMalformedException] {
        BusinessTimesConverter.convert(
          Map(
            "friday" -> List(
              OpeningOrClosingTime("open", 36000)
            ),
            "saturday" -> List(
              OpeningOrClosingTime("close", 3600)
            ),
            "sunday" -> List(
              OpeningOrClosingTime("open", 36000)
            )
          )
        )
      }
    }

    "opening time not followed by closing time" when {
      "on one day" in {
        assertThrows[OpeningTimesMalformedException] {
          BusinessTimesConverter.convert(
            Map(
              "friday" -> List(
                OpeningOrClosingTime("open", 36000)
              )
            )
          )
        }
      }

      "spanning two consecutive days" in {
        assertThrows[OpeningTimesMalformedException] {
          BusinessTimesConverter.convert(
            Map(
              "friday" -> List(
                OpeningOrClosingTime("open", 36000)
              ),
              "saturday" -> List(
                OpeningOrClosingTime("open", 36000),
                OpeningOrClosingTime("close", 36500)
              )
            )
          )
        }
      }

      "never opening business" in {
        assertThrows[OpeningTimesMalformedException] {
          BusinessTimesConverter.convert(
            Map(
              "friday" -> List(
                OpeningOrClosingTime("close", 36000)
              ),
              "saturday" -> List(
                OpeningOrClosingTime("close", 36000),
                OpeningOrClosingTime("close", 36500)
              )
            )
          )
        }
      }
    }
  }
}
