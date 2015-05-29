package pl.touk.jedzieTramwaj

import pl.touk.jedzieTramwaj.model._
import pl.touk.jedzieTramwaj.protocol.TramWithDistance
import spray.json.DefaultJsonProtocol

object protocol {
  case class TramsRequest(location: Location, lineNumbers: List[Int])
  case class TramWithDistance(tram: TramLocation, distanceInMeters: Double)
  type TramsResponse = List[TramWithDistance]
}

object protocolJson extends DefaultJsonProtocol {
  import modelJson._

  implicit val tramWithDistance = jsonFormat2(TramWithDistance.apply)
}

