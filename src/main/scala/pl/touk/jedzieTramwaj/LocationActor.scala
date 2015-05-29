
package pl.touk.jedzieTramwaj

import java.io.IOException
import java.time.LocalDateTime
import java.time.format.{DateTimeFormatter, DateTimeFormatterBuilder}

import akka.actor.Actor
import akka.http.scaladsl.Http
import akka.http.scaladsl.client.RequestBuilding
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.model.{HttpRequest, HttpResponse}
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.scaladsl.{Flow, Sink, Source}
import com.typesafe.config.ConfigFactory
import pl.touk.jedzieTramwaj.MainApp._
import spray.json.DefaultJsonProtocol

import scala.concurrent.Future
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

class LocationFetcher extends DefaultJsonProtocol {

  val config = ConfigFactory.load()

  lazy val umConnectionFlow: Flow[HttpRequest, HttpResponse, Any] =
    Http().outgoingConnectionTls(config.getString("services.umHost"), config.getInt("services.umPort"))

  def umRequest(request: HttpRequest): Future[HttpResponse] =
    Source.single(request).via(umConnectionFlow).runWith(Sink.head)

  implicit val resultFormat1 = jsonFormat2(Value.apply)
  implicit val resultFormat2 = jsonFormat1(Values.apply)
  implicit val resultFormat3 = jsonFormat1(ValuesList.apply)

  def fetchLocations(): Future[ValuesList] = {
    logger.debug("Starting to fetch locations")
    val id = "daeea0db-0f9a-498d-9c4f-210897daffd2"
    umRequest(RequestBuilding.Get(s"/api/action/dbstore_get/?id=$id&apikey=${config.getString("apiKey")}")).flatMap { response =>
      response.status match {
        case OK => Unmarshal(response.entity).to[ValuesList]
        case BadRequest => Future.failed(new Exception("WAAAT?"))
        case _ => Unmarshal(response.entity).to[String].flatMap { entity =>
          val error = s"UM request failed with status code ${response.status} and entity $entity"
          logger.error(error)
          Future.failed(new IOException(error))
        }
      }
    }
  }
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