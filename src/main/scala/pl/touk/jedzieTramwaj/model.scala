package pl.touk.jedzieTramwaj

import java.time.LocalDateTime

object model

case class TramLocation(line: Int, date: LocalDateTime, brigade: String, location: Location)
case class Location(lon: Double, lat: Double)
