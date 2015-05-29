package pl.touk.jedzieTramwaj

import java.io.IOException

import akka.http.scaladsl.Http
import akka.http.scaladsl.client.RequestBuilding
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.model.{ResponseEntity, HttpResponse, HttpRequest}
import akka.http.scaladsl.unmarshalling.{Unmarshal, Unmarshaller}
import akka.stream.scaladsl.{Sink, Source, Flow}
import com.typesafe.config.ConfigFactory
import pl.touk.jedzieTramwaj.MainApp._

import scala.concurrent.Future

trait UMFetcher {

  def url : String

  val config = ConfigFactory.load()

  lazy val umConnectionFlow: Flow[HttpRequest, HttpResponse, Any] =
    Http().outgoingConnectionTls(config.getString("services.umHost"), config.getInt("services.umPort"))

  def umRequest(request: HttpRequest): Future[HttpResponse] =
    Source.single(request).via(umConnectionFlow).runWith(Sink.head)

  def fetch[T:({type Za[A]=Unmarshaller[ResponseEntity, A]})#Za](paramsMap: Map[String, String] = Map()): Future[T] = {
    logger.debug("Starting to fetch locations")
    //TODO: co tu w sumie ma byc??
    val id = "daeea0db-0f9a-498d-9c4f-210897daffd2"
    val params = paramsMap.map(v => s"&${v._1}=${v._2}").mkString
    umRequest(RequestBuilding.Get(s"$url?id=$id$params&apikey=${config.getString("apiKey")}")).flatMap { response =>
      response.status match {
        case OK => Unmarshal(response.entity).to[T]
        case BadRequest => Future.failed(new Exception("Bad request"))
        case _ => Unmarshal(response.entity).to[String].flatMap { entity =>
          val error = s"UM request failed with status code ${response.status} and entity $entity"
          logger.error(error)
          Future.failed(new IOException(error))
        }
      }
    }
  }

}

