package controllers

import model.Types.Weekday
import model.conversion.{BusinessTimesConverter}
import model.external.OpeningOrClosingTime
import play.api.mvc._
import javax.inject.{Inject, Singleton}
import play.api.Logger
import scala.util.{Failure, Success}
import scala.util.control.Exception.{allCatch}

@Singleton
class BusinessTimesController @Inject() (
    val controllerComponents: ControllerComponents
) extends BaseController {
  import model.external.OpeningOrClosingTimeJsonReads.reads

  val logger: Logger = Logger(this.getClass())

  def businessTimesRenderEndpoint() =
    Action(parse.json) { request =>
      request.body
        .validate[Map[Weekday, List[OpeningOrClosingTime]]]
        .map {
          case timesByWeekday: Map[Weekday, List[OpeningOrClosingTime]] =>
            respondWithExceptionHandling(timesByWeekday)
        }
        .recoverTotal { jsError =>
          logger.error("Error parsing json: ${jsError}")
          BadRequest(s"Error parsing json: ${jsError.toString}")
        }
    }

  def respondWithExceptionHandling(
      timesByWeekday: Map[Weekday, List[OpeningOrClosingTime]]
  ): Result = {
    allCatch.withTry {
      BusinessTimesConverter.convert(timesByWeekday)
    } match {
      case Success(businessTimesByWeekday) => Ok(views.txt.index(businessTimesByWeekday))
      case Failure(conversionError) =>
        logger.error("Error converting business times: ", conversionError)
        BadRequest(s"Error converting business times: ${conversionError.getMessage}")
    }
  }
}
