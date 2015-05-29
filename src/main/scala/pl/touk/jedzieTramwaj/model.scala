package pl.touk.jedzieTramwaj

import java.time.{ZoneId, LocalDateTime}
import java.util.Date

import spray.json.{JsNumber, JsValue, JsonFormat, DefaultJsonProtocol}

object model {

  case class TramLocation(id: TramId, point: LocationPoint)
  case class TramId(line: String, brigade: String, sideNumber: String)
  case class LocationPoint(date: LocalDateTime, location: Location)

  case class Location(lat: Double, lon: Double) {
    import Math._
    //http://stackoverflow.com/questions/8588095/calculate-distance-with-2-geo-points
    def distanceInMeters(location: Location) = {
      toRadians(acos(sin(toRadians(location.lat)) * sin(toRadians(lat)) + cos(toRadians(location.lat))
        * cos(toRadians(lat)) * cos(toRadians(location.lon - lon)))) * 60 * 1.1515 * 1.609344 * 1000 * 1000
    }
  }

}

object modelJson extends DefaultJsonProtocol {
  import model._

  private val defaultZone: ZoneId = ZoneId.systemDefault()

  implicit val localDate = new JsonFormat[LocalDateTime] {
    override def write(obj: LocalDateTime) = JsNumber(obj.atZone(defaultZone).toInstant.toEpochMilli)
    override def read(json: JsValue) = json match {
      case a:JsNumber => LocalDateTime.ofInstant(new Date(a.value.longValue()).toInstant, defaultZone)
    }
  }
  implicit val location = jsonFormat2(Location.apply)
  implicit val locationPoint = jsonFormat2(LocationPoint.apply)
  implicit val tramId = jsonFormat3(TramId.apply)
  implicit val tramLocation = jsonFormat2(TramLocation.apply)

}
