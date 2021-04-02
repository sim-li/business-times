package model.external

import org.scalatestplus.play.PlaySpec
import play.api.libs.json._

class OpeningOrClosingTimeJsonReadsSpec extends PlaySpec {
  "json reads" should {
    import OpeningOrClosingTimeJsonReads.reads

    "read opening time" in {
      val js = Json.obj("type" -> "open", "value" -> 64800)
      val o = js.as[OpeningOrClosingTime]

      assert(o.`type` === "open")
      assert(o.value === 64800)
    }

    "read closing time" in {
      val js = Json.obj("type" -> "close", "value" -> 64800)
      val o = js.as[OpeningOrClosingTime]

      assert(o.`type` === "close")
      assert(o.value === 64800)
    }

    "read opening and closing times for weekday" in {
      val js = Json.arr(
        Json.obj("type" -> "open", "value" -> 32400),
        Json.obj("type" -> "close", "value" -> 3600)
      )
      val o = js.as[List[OpeningOrClosingTime]]

      assert(
        o === List(
          OpeningOrClosingTime("open", 32400),
          OpeningOrClosingTime("close", 3600)
        )
      )
    }

    "read opening and closing times for several weekdays" in {
      val js = Json.obj(
        "monday" -> Json.arr(
          Json.obj("type" -> "open", "value" -> 32400),
          Json.obj("type" -> "close", "value" -> 36000)
        ),
        "tuesday" -> Json.arr(
          Json.obj("type" -> "open", "value" -> 36000),
          Json.obj("type" -> "close", "value" -> 64800)
        )
      )

      val o = js.as[Map[String, List[OpeningOrClosingTime]]]

      assert(
        o === Map(
          "monday" -> List(
            OpeningOrClosingTime("open", 32400),
            OpeningOrClosingTime("close", 36000)
          ),
          "tuesday" -> List(
            OpeningOrClosingTime("open", 36000),
            OpeningOrClosingTime("close", 64800)
          )
        )
      )
    }

    "read for one weekday empty" in {
      val js = Json.obj(
        "monday" -> Json.arr(
          Json.obj("type" -> "open", "value" -> 32400),
          Json.obj("type" -> "close", "value" -> 36000)
        ),
        "tuesday" -> Json.arr()
      )

      val o = js.as[Map[String, List[OpeningOrClosingTime]]]

      assert(
        o === Map(
          "monday" -> List(
            OpeningOrClosingTime("open", 32400),
            OpeningOrClosingTime("close", 36000)
          ),
          "tuesday" -> List()
        )
      )
    }
  }
}
