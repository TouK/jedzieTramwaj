package pl.touk.jedzieTramwaj

import java.io.IOException

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

class LocationActor extends Actor {

  override def receive = {
    case a:String => sender ! Right(Result(s"$a + b"))

  }

}

class LocationFetcher extends DefaultJsonProtocol {

  val config = ConfigFactory.load()

  lazy val umConnectionFlow: Flow[HttpRequest, HttpResponse, Any] =
    Http().outgoingConnection(config.getString("services.umHost"), config.getInt("services.umPort"))

  def umRequest(request: HttpRequest): Future[HttpResponse] =
    Source.single(request).via(umConnectionFlow).runWith(Sink.head)

  implicit val resultFormat1 = jsonFormat2(Value.apply)
  implicit val resultFormat2 = jsonFormat1(Values.apply)
  implicit val resultFormat3 = jsonFormat1(TramLocations.apply)


  def fetchIpInfo(ip: String): Future[Either[String, TramLocations]] = {
    umRequest(RequestBuilding.Get(s"/api/action/dbstore_get/?id=11&apikey=${config.getString("apiKey")}")).flatMap { response =>
      response.status match {
        case OK => Unmarshal(response.entity).to[TramLocations].map(Right(_))
        case BadRequest => Future.successful(Left(s"$ip: incorrect IP format"))
        case _ => Unmarshal(response.entity).to[String].flatMap { entity =>
          val error = s"UM request failed with status code ${response.status} and entity $entity"
          logger.error(error)
          Future.failed(new IOException(error))
        }
      }
    }
  }
}

case class TramLocations(result : Values)
case class Values(items: List[Value])
case class Value(key: String, value: String)