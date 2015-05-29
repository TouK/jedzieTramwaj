package pl.touk.jedzieTramwaj

import pl.touk.jedzieTramwaj.model._
import pl.touk.jedzieTramwaj.protocol.TramWithDistance
import spray.json.DefaultJsonProtocol

object protocol {
  case class TramsRequest(busStop : BusStop)
  case class TramWithDistance(tram: TramLocation, speed: Double, distanceInMeters: Double)
  type TramsResponse = List[TramWithDistance]
  type BusStopsResponse = List[BusStop]
}

trait JsonProtocol extends DefaultJsonProtocol with JsonModel{
  implicit val tramWithDistanceFormat = jsonFormat2(TramWithDistance.apply)
  implicit val busStopsFormat = jsonFormat6(BusStop.apply)
}

