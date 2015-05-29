package pl.touk.jedzieTramwaj

import java.io.IOException
import java.util.concurrent.Future

import akka.actor.Actor
import akka.http.scaladsl.client.RequestBuilding
import akka.http.scaladsl.model.{HttpResponse, HttpRequest}
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.scaladsl.{Sink, Source}
import pl.touk.jedzieTramwaj.MainApp.Result

class LocationActor extends Actor {

  override def receive = {
    case a:String => sender ! Right(Result(s"$a + b"))

  }


  def telizeRequest(request: HttpRequest): Future[HttpResponse] = Source.single(request).via(telizeConnectionFlow).runWith(Sink.head)

  def fetchIpInfo(ip: String): Future[Either[String, IpInfo]] = {
    telizeRequest(RequestBuilding.Get(s"/geoip/$ip")).flatMap { response =>
      response.status match {
        case OK => Unmarshal(response.entity).to[IpInfo].map(Right(_))
        case BadRequest => Future.successful(Left(s"$ip: incorrect IP format"))
        case _ => Unmarshal(response.entity).to[String].flatMap { entity =>
          val error = s"Telize request failed with status code ${response.status} and entity $entity"
          logger.error(error)
          Future.failed(new IOException(error))
        }
      }
    }
  }
}

case class
