package controllers

import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerTest
import play.api.Play.materializer
import play.api.libs.json.Json
import play.api.mvc.Headers
import play.api.test.Helpers.{CONTENT_TYPE, GET, status, stubControllerComponents}
import play.api.test.{FakeRequest, Injecting}
import play.api.test.Helpers._

class BusinessTimesRenderControllerSpec extends PlaySpec with GuiceOneAppPerTest with Injecting {
  "Business Times rendering" should {
    "render business times for one day" in {
      val controller = new BusinessTimesController(stubControllerComponents());
      val response = controller
        .businessTimesRenderEndpoint()
        .apply(
          FakeRequest(GET, "/businessTimes/render")
            .withHeaders(Headers(CONTENT_TYPE -> "application/json"))
            .withBody(
              Json.parse(
                """
                  | {
                  |    "monday" : [{
                  |      "type" : "open",
                  |      "value" : 32400
                  |     }, {
                  |       "type" : "close",
                  |       "value" : 72000
                  |     }]
                  | }
                |""".stripMargin
              )
            )
        )

      status(response) mustBe OK
      contentType(response) mustBe Some("text/plain")
      stripped(contentAsString(response)) must be(
        stripped(
          """
          |Monday: 9:00 AM - 8:00 PM
          |Tuesday: Closed
          |Wednesday: Closed
          |Thursday: Closed
          |Friday: Closed
          |Saturday: Closed
          |Sunday: Closed
          """
        )
      )
    }

    "render business times for two days and multiple opening times" in {
      val controller = new BusinessTimesController(stubControllerComponents());
      val response = controller
        .businessTimesRenderEndpoint()
        .apply(
          FakeRequest(GET, "/businessTimes/render")
            .withHeaders(Headers(CONTENT_TYPE -> "application/json"))
            .withBody(
              Json.parse(
                """
                 {
                  |  "friday": [
                  |    {
                  |      "type": "open",
                  |      "value": 64800
                  |    }
                  |  ],
                  |  "saturday": [
                  |    {
                  |      "type": "close",
                  |      "value": 3600
                  |    },
                  |    {
                  |      "type": "open",
                  |      "value": 32400
                  |    },
                  |    {
                  |      "type": "close",
                  |      "value": 39600
                  |    },
                  |    {
                  |      "type": "open",
                  |      "value": 57600
                  |    },
                  |    {
                  |      "type": "close",
                  |      "value": 82800
                  |    }
                  |  ]
                  |}
                  |""".stripMargin
              )
            )
        )

      status(response) mustBe OK
      contentType(response) mustBe Some("text/plain")
      stripped(contentAsString(response)) must be(
        stripped("""
          |Monday: Closed
          |Tuesday: Closed
          |Wednesday: Closed
          |Thursday: Closed
          |Friday: 6:00 PM - 1:00 AM
          |Saturday: 9:00 AM - 11:00 AM, 4:00 PM - 11:00 PM
          |Sunday: Closed
        """)
      )
    }

    "render business times for whole week" in {
      val controller = new BusinessTimesController(stubControllerComponents());
      val response = controller
        .businessTimesRenderEndpoint()
        .apply(
          FakeRequest(GET, "/businessTimes/render")
            .withHeaders(Headers(CONTENT_TYPE -> "application/json"))
            .withBody(
              Json.parse(
                """
                  |{
                  |    "monday":[
                  |    ],
                  |    "tuesday":[
                  |       {
                  |          "type":"open",
                  |          "value":36000
                  |       },
                  |       {
                  |          "type":"close",
                  |          "value":64800
                  |       }
                  |    ],
                  |    "wednesday":[
                  |       
                  |    ],
                  |    "thursday":[
                  |       {
                  |          "type":"open",
                  |          "value":36000
                  |       },
                  |       {
                  |          "type":"close",
                  |          "value":64800
                  |       }
                  |    ],
                  |    "friday":[
                  |       {
                  |          "type":"open",
                  |          "value":36000
                  |       }
                  |    ],
                  |    "saturday":[
                  |       {
                  |          "type":"close",
                  |          "value":3600
                  |       },
                  |       {
                  |          "type":"open",
                  |          "value":36000
                  |       }
                  |    ],
                  |    "sunday":[
                  |       {
                  |          "type":"close",
                  |          "value":3600
                  |       },
                  |       {
                  |          "type":"open",
                  |          "value":43200
                  |       },
                  |       {
                  |          "type":"close",
                  |          "value":75600
                  |       }
                  |    ]
                  |}  
                  |""".stripMargin
              )
            )
        )

      status(response) mustBe OK
      contentType(response) mustBe Some("text/plain")
      stripped(contentAsString(response)) must be(
        stripped(
          """
          |Monday: Closed
          |Tuesday: 10:00 AM - 6:00 PM
          |Wednesday: Closed
          |Thursday: 10:00 AM - 6:00 PM
          |Friday: 10:00 AM - 1:00 AM
          |Saturday: 10:00 AM - 1:00 AM
          |Sunday: 12:00 PM - 9:00 PM
        """
        )
      )
    }

    //missing linebreak between business days may remain undetected
    def stripped(s: String) = s.stripMargin.stripTrailing

    "return bad request for malformed opening times" in {
      val controller = new BusinessTimesController(stubControllerComponents());
      val response = controller
        .businessTimesRenderEndpoint()
        .apply(
          FakeRequest(GET, "/businessTimes/render")
            .withHeaders(Headers(CONTENT_TYPE -> "application/json"))
            .withJsonBody(
              Json.parse(
                """
                  | {
                  |    "monday" : [{
                  |      "type" : "open",
                  |      "value" : 32400
                  |     }, {
                  |       "type" : "open",
                  |       "value" : 72000
                  |     }]
                  | }
                  |""".stripMargin
              )
            )
        )

      status(response) mustBe BAD_REQUEST
    }

    "return bad request for wrong field names" in {
      val controller = new BusinessTimesController(stubControllerComponents());
      val response = controller
        .businessTimesRenderEndpoint()
        .apply(
          FakeRequest(GET, "/businessTimes/render")
            .withHeaders(Headers(CONTENT_TYPE -> "application/json"))
            .withJsonBody(
              Json.parse(
                """
                  | {
                  |    "monday" : [{
                  |      "wrong" : "open",
                  |      "fields" : 32400
                  |     }, {
                  |       "are" : "open",
                  |       "everywhere" : 72000
                  |     }]
                  | }
                  |""".stripMargin
              )
            )
        )

      status(response) mustBe BAD_REQUEST
    }
  }
}
