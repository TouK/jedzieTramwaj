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
import spray.json.DefaultJsonProtocol
import scala.concurrent.duration._


import scala.concurrent.Future


object MainApp extends App with DefaultJsonProtocol {
  implicit val system = ActorSystem()
  implicit val executor = system.dispatcher
  implicit val materializer = ActorFlowMaterializer()
  implicit val timeout = Timeout(20 seconds)

  val config = ConfigFactory.load()
  val logger = Logging(system, getClass)

  case class Result(res: String)


  implicit val resultFormat = jsonFormat1(Result.apply)


  val location = system.actorOf(Props.create(classOf[LocationActor]))


  def getResult(arg : String) : Future[Either[String, Result]] = (location ? arg).mapTo[Either[String, Result]]

  val routes = {
    logRequestResult("jedzieTramwaj") {
      pathPrefix("przystanek") {
        (get & path(Segment)) { przystanek =>
          complete {
            getResult(przystanek).map[ToResponseMarshallable] {
              case Right(ipInfo) => ipInfo
              case Left(errorMessage) => BadRequest -> errorMessage
            }
          }
        }
      }
    }
  }

  Http().bindAndHandle(routes, config.getString("http.interface"), config.getInt("http.port"))


}
