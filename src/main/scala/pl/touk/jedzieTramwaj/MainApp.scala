package pl.touk.jedzieTramwaj

import akka.actor.{Props, ActorSystem}
import akka.event.Logging
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.stream.ActorFlowMaterializer
import akka.util.Timeout
import com.typesafe.config.ConfigFactory
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.pattern._
import pl.touk.jedzieTramwaj.model.Location
import pl.touk.jedzieTramwaj.protocol.{TramsRequest, TramsResponse}
import spray.json.DefaultJsonProtocol
import scala.concurrent.duration._


import scala.concurrent.Future


object MainApp extends App {
  import protocolJson._


  implicit val system = ActorSystem()
  implicit val executor = system.dispatcher
  implicit val materializer = ActorFlowMaterializer()
  implicit val timeout = Timeout(20 seconds)

  val config = ConfigFactory.load()
  val logger = Logging(system, getClass)

  val location = system.actorOf(Props.create(classOf[LocationActor]))

  val busStops = new ZtmDataParser().parse(getClass.getResourceAsStream("/ztm.data"))
    .map(bs => (bs.id, bs))
    .toMap
  //TODO: usunac...
  println(busStops)

  def getResult(arg : Long) : Future[TramsResponse] =
    (location ? TramsRequest(busStops(arg))).mapTo[TramsResponse]

  val routes = {
    logRequestResult("jedzieTramwaj") {
      pathPrefix("tramwaje") {
        (get & path(Segment)) { przystanekId =>
          complete {
            //yyy?
            getResult(przystanekId.toLong).map[ToResponseMarshallable](id => id)
          }
        }
      }
    }
  }

  Http().bindAndHandle(routes, config.getString("http.interface"), config.getInt("http.port"))


}
