package pl.touk.jedzieTramwaj

import pl.touk.jedzieTramwaj.model._
import pl.touk.jedzieTramwaj.protocol.{BusStopWithTrams, TramWithSpeed, TramWithDistance}
import spray.json.DefaultJsonProtocol

object protocol {
  case class TramsRequestByStop(busStop : BusStop)
  case class TramsRequestByLines(numbers: List[String])
  case object AllTramsRequest

  case class TramWithSpeed(tram: TramLocation, speed: Double)
  case class TramWithDistance(tram: TramLocation, speed: Double, distanceInMeters: Double)
  case class BusStopWithTrams(id: Long, name: String, description: String, direction: String, loc: Location, lines: List[TramWithDistance])
  type TramsResponse = List[TramWithDistance]
  type BusStopsResponse = List[BusStop]
}

trait JsonProtocol extends DefaultJsonProtocol with JsonModel {

  implicit val tramWithSpeedFormat = jsonFormat2(TramWithSpeed.apply)
  implicit val tramWithDistanceFormat = jsonFormat3(TramWithDistance.apply)
  implicit val busStopsFormat = jsonFormat6(BusStop.apply)
  implicit val busStopWithTramsFormat = jsonFormat6(BusStopWithTrams.apply)
}

