package pl.touk.jedzieTramwaj

import akka.actor.{ActorSystem, Props}
import akka.event.Logging
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.server.Directives._
import akka.pattern._
import akka.stream.ActorFlowMaterializer
import akka.util.Timeout
import com.typesafe.config.ConfigFactory
import pl.touk.jedzieTramwaj.model.Location
import pl.touk.jedzieTramwaj.protocol.{TramsRequest, TramsResponse}

import scala.concurrent.Future
import scala.concurrent.duration._


object MainApp extends App with JsonProtocol with SprayJsonSupport {

  implicit val system = ActorSystem()
  implicit val executor = system.dispatcher
  implicit val materializer = ActorFlowMaterializer()
  implicit val timeout = Timeout(20 seconds)

  val config = ConfigFactory.load()
  val logger = Logging(system, getClass)

  val location = system.actorOf(Props.create(classOf[LocationActor]))

  val busStops = new ZtmDataParser().parse(getClass.getResourceAsStream("/ztm.data"))

  val busStopsMap = busStops
    .map(bs => (bs.id, bs))
    .toMap
  //TODO: usunac...
  println(busStopsMap)

  def getResult(arg : Long) : Future[TramsResponse] =
    (location ? TramsRequest(busStopsMap(arg))).mapTo[TramsResponse]

  val routes = {
    logRequestResult("jedzieTramwaj") {
      pathPrefix("tramwaje") {
        (get & path(Segment)) { przystanekId =>
          complete {
            //yyy?
            getResult(przystanekId.toLong).map[ToResponseMarshallable](id => id)
          }
        }
      } ~
      pathPrefix("przystanki") {
        get {
          complete {
            Future.successful(busStops).map[ToResponseMarshallable](id => id)
          }
        }
      }
    }
  }

  Http().bindAndHandle(routes, config.getString("http.interface"), config.getInt("http.port"))


}
