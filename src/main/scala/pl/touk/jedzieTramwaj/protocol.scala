package pl.touk.jedzieTramwaj

import pl.touk.jedzieTramwaj.model._

object protocol {
  case class TramsRequest(location: Location, lineNumbers: List[Int])
  type TramsResponse = List[TramLocation]
}

