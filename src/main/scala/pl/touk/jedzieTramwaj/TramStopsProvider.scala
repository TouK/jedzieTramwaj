package pl.touk.jedzieTramwaj

import java.text.Collator
import java.util.Locale

import scala.util.control.Exception.catching

object TramStopsProvider {

  private val allBusStops = new ZtmDataParser().parse(getClass.getResourceAsStream("/ztm.data"))

  def busStops: Seq[BusStop] = {
    val collator = Collator.getInstance(new Locale("pl", "PL"))
    allBusStops
      .filter(stop => isTramStop(stop))
      .map(stop => stop.copy(lines = stop.lines.filter((line: String) => isTramLine(line))))
      .sortWith { (a, b) =>
        // uwzględnia ogonki
        val nameResult = collator.compare(a.name, b.name)
        nameResult < 0 || (nameResult == 0 && collator.compare(a.direction, b.direction) < 0)
      }
  }

  private def isTramStop(stop: BusStop): Boolean = {
    stop.lines.exists((line: String) => isTramLine(line))
  }

  private def isTramLine(line: String): Boolean = {
    catching(classOf[NumberFormatException]).opt(line.toDouble).exists(lineInDouble => lineInDouble < 100)
  }
}
