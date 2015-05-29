package pl.touk.jedzieTramwaj

import pl.touk.jedzieTramwaj.model._
import pl.touk.jedzieTramwaj.protocol.{TramWithSpeed, TramWithDistance}
import spray.json.DefaultJsonProtocol

object protocol {
  case class TramsRequestByStop(busStop : BusStop)
  case class TramsRequestByLines(numbers: List[String])
  case object AllTramsRequest

  case class TramWithSpeed(tram: TramLocation, speed: Double)
  case class TramWithDistance(tram: TramLocation, speed: Double, distanceInMeters: Double)
  type TramsResponse = List[TramWithDistance]
  type BusStopsResponse = List[BusStop]
}

trait JsonProtocol extends DefaultJsonProtocol with JsonModel {

  implicit val tramWithSpeedFormat = jsonFormat2(TramWithSpeed.apply)
  implicit val tramWithDistanceFormat = jsonFormat3(TramWithDistance.apply)
  implicit val busStopsFormat = jsonFormat6(BusStop.apply)
}

