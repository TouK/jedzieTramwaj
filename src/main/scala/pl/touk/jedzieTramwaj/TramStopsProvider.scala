package pl.touk.jedzieTramwaj

import scala.util.control.Exception.catching

object tramStopsProvider {

  private val allBusStops = new ZtmDataParser().parse(getClass.getResourceAsStream("/ztm.data"))

  def busStops: Seq[BusStop] = {
    allBusStops.filter((stop: BusStop) => isTramStop(stop))
      .map((stop: BusStop) => hasTramsOnly(stop) match {
      case true => stop
      case false => stop.copy(lines = stop.lines.filter((line: String) => isTramLine(line)))
    })
  }

  private def isTramStop(stop: BusStop): Boolean = {
    stop.lines.exists((line: String) => isTramLine(line))
  }

  private def isTramLine(line: String): Boolean = {
    catching(classOf[NumberFormatException]).opt(line.toDouble) match {
      case Some(lineInDouble) => lineInDouble < 100
      case None => false
    }
  }

  private def hasTramsOnly(busStop: BusStop): Boolean = {
    busStop.lines.forall((line: String) => isTramLine(line))
  }
}
