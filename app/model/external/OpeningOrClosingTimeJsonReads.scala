package model.external

import play.api.libs.functional.syntax.toFunctionalBuilderOps
import play.api.libs.json.{JsPath, Reads}

object OpeningOrClosingTimeJsonReads {
  implicit val reads: Reads[OpeningOrClosingTime] = (
    (JsPath \ "type").read[String] and
      (JsPath \ "value").read[Int]
  )(OpeningOrClosingTime.apply _)
}
