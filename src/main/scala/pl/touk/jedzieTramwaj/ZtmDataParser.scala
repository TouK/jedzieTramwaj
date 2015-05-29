package pl.touk.jedzieTramwaj

import java.io.InputStream

class ZtmDataParser {

  def parse(stream: InputStream): Seq[BusStop] = ???

}

case class BusStop(name: String, direction: String, loc: Localization, lines: List[String])
case class Localization(lng: Double, lat: Double)