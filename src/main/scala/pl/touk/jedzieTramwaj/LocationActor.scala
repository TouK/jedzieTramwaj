
package pl.touk.jedzieTramwaj

import java.time.LocalDateTime
import java.time.format.{DateTimeFormatter, DateTimeFormatterBuilder}

import akka.actor.Actor
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import pl.touk.jedzieTramwaj.MainApp._
import spray.json.DefaultJsonProtocol

import scala.concurrent.duration._
import scala.util.Failure

class LocationActor extends Actor {

  val locationFetcher = new LocationFetcher

  var tramLocations = List[TramLocation]()

  @throws[Exception](classOf[Exception])
  override def preStart() = {
    reloadLocations()
    context.system.scheduler.schedule(10 seconds, 10 seconds)(reloadLocations())
  }

  private def reloadLocations(): Unit = {
    locationFetcher.fetchLocations()
      .onComplete {
      case scala.util.Success(loc) => self ! loc
      case Failure(e) => logger.error(e, "Failed to fetch location")
    }
  }

  override def receive = {
    case a: String =>
      sender ! Right(Result(tramLocations.head.toString))
    case locations: ValuesList =>
      tramLocations = locations.result.map(TramLocationParser.parseLocation)
      logger.debug(s"Fetched ${tramLocations.size}")
  }

}

class LocationFetcher extends DefaultJsonProtocol with UMFetcher {

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

  val format = new DateTimeFormatterBuilder().parseCaseInsensitive.append(DateTimeFormatter.ISO_LOCAL_DATE)
    .appendLiteral(' ').append(DateTimeFormatter.ISO_LOCAL_TIME).toFormatter

  def parseLocation(raw: Values): TramLocation = {
    val value: String => String = (key) => raw.values.find(_.key == key).map(_.value).get
    TramLocation(value("linia").toInt, LocalDateTime.parse(value("ostatnia_aktualizacja"), format),
      value("brygada"), Location(value("gps_dlug").toDouble, value("gps_szer").toDouble))
  }
}