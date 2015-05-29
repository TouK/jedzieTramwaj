package pl.touk.jedzieTramwaj

import java.time.LocalDateTime

object model {

  case class TramLocation(id: TramId, point: LocationPoint)
  case class TramId(line: Int, brigade: String, sideNumber: String)
  case class LocationPoint(date: LocalDateTime, location: Location)

  case class Location(lat: Double, lon: Double) {
    import Math._
    //http://stackoverflow.com/questions/8588095/calculate-distance-with-2-geo-points
    def distanceInMeters(location: Location) = {
      toRadians(acos(sin(toRadians(location.lat)) * sin(toRadians(lat)) + cos(toRadians(location.lat))
        * cos(toRadians(lat)) * cos(toRadians(location.lon - lon)))) * 60 * 1.1515 * 1.609344 * 1000
    }

  }

}
