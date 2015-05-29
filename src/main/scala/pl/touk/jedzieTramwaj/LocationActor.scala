
package pl.touk.jedzieTramwaj

import java.time.LocalDateTime
import java.time.format.{DateTimeFormatter, DateTimeFormatterBuilder}
import java.time.temporal.ChronoUnit

import akka.actor.Actor
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import pl.touk.jedzieTramwaj.MainApp._
import pl.touk.jedzieTramwaj.model._
import pl.touk.jedzieTramwaj.protocol._
import spray.json.DefaultJsonProtocol

import scala.concurrent.duration._
import scala.util.Failure

class LocationActor extends Actor {

  val reloadDelay = 30 seconds

  val locationFetcher = new LocationFetcher

  var tramLocations = List[TramLocation]()

  var tramData : Map[TramId, ExtendedTramData] = Map()

  @throws[Exception](classOf[Exception])
  override def preStart() = {
    reloadLocations()
    context.system.scheduler.schedule(reloadDelay, reloadDelay)(reloadLocations())
  }

  override def receive = {
    case TramsRequestByStop(stop) => sender ! prepareLocations(stop.loc, stop.lines)
    case TramsRequestByLines(list) => sender ! tramLocations.filter(loc => list.contains(loc.id.line))
      .map(loc => TramWithSpeed(loc, speed(loc)))
    case AllTramsRequest => sender ! tramLocations.map(loc => TramWithSpeed(loc, speed(loc)))

    case locations: ValuesList =>
      tramLocations = locations.result.map(TramLocationParser.parseLocation)
      recalculateExtendedData()
      logger.debug(s"Fetched ${tramLocations.size}")
  }

  private def reloadLocations(): Unit = {
    locationFetcher.fetchLocations()
      .onComplete {
      case scala.util.Success(loc) => self ! loc
      case Failure(e) => logger.error(e, "Failed to fetch location")
    }
  }

  private def recalculateExtendedData(): Unit = {
    val currentTrams = tramLocations.map(_.id)
    val filtered = tramData.filter(a => currentTrams.contains(a._1))
    tramData = tramLocations.foldLeft(filtered)((map, tram) =>
      map.updated(tram.id, map.getOrElse(tram.id, ExtendedTramData()).addLocation(tram.point)))
  }

  private def prepareLocations(point: Location, lineNumbers: Seq[String]) =
    tramLocations.filter(tl => lineNumbers.contains(tl.id.line))
      .map(loc => TramWithDistance(loc, speed(loc),
        loc.point.location.distanceInMeters(point)))
      .sortBy(_.distanceInMeters)

  private def speed(loc: TramLocation) : Double = tramData(loc.id).speedKmph.getOrElse(0)

  case class ExtendedTramData(lastLocations: List[LocationPoint] = List()) {
    def speedKmph : Option[Double] = lastLocations match {
      case a::b::c => Some(a.location.distanceInMeters(b.location) * 0.001 /
        ChronoUnit.HOURS.between(a.date, b.date))
      case _ => None
    }

    def addLocation(point : LocationPoint) = {
      val locations = lastLocations match {
        case first::last if first.date != point.date => point::lastLocations
        case _ => lastLocations
      }
      //no tak, tak, to jest niewydajne...
      copy(lastLocations = locations.take(10))
    }
  }

}

class LocationFetcher extends DefaultJsonProtocol with UMFetcher with SprayJsonSupport {

  def url = "/api/action/dbstore_get/"

  implicit val valueFormat = jsonFormat2(Value.apply)
  implicit val valuesFormat = jsonFormat1(Values.apply)
  implicit val valueListFormat = jsonFormat1(ValuesList.apply)

  def fetchLocations() = fetch[ValuesList]()
}

case class ValuesList(result: List[Values])
case class Values(values: List[Value])
case class Value(key: String, value: String)

object TramLocationParser {

  val format = new DateTimeFormatterBuilder()
    .parseCaseInsensitive.append(DateTimeFormatter.ISO_LOCAL_DATE)
    .appendLiteral(' ').append(DateTimeFormatter.ISO_LOCAL_TIME).toFormatter

  def parseLocation(raw: Values): TramLocation = {
    val value: String => String = (key) => raw.values.find(_.key == key).map(_.value).get
    TramLocation(TramId(value("linia"), value("brygada"), value("taborowy")),
      LocationPoint(LocalDateTime.parse(value("ostatnia_aktualizacja"), format),
      Location(value("gps_szer").toDouble, value("gps_dlug").toDouble)))
  }
}