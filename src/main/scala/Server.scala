import java.util.Calendar

import akka.actor.ActorSystem
import akka.http.javadsl.server.Directives
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.ws.{TextMessage, Message}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server._
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Flow
import upickle.default._
import akka.util.Timeout


import akka.actor._

import akka.pattern.ask
import akka.util.Timeout

import scala.concurrent.Await



object Server extends App {

  println(write(ClientPhoneInPut("e")))
  println(write(ClientLogin("e", 94959)))

  val server = new Server()

  readLine()
  server.close()
}

class Server {
  implicit val actorSystem = ActorSystem("akka-system")
  implicit val flowMaterializer = ActorMaterializer()
  val system = ActorSystem("mySystem")
  val interface = "localhost"
  val port = 8080

  val route = getRoute()

  val binding = Http().bindAndHandle(route, interface, port)
  println(s"Server is now online at http://$interface:$port\nWebsoket: ws://$interface:$port/ws\nPress RETURN to stop...")

  val echoService: Flow[Message, Message, _] = Flow[Message].map {
    case TextMessage.Strict(txt) => TextMessage(handleWebSocketMessage(txt))
    case _ => TextMessage("Message type unsupported")
  }

  def getRoute(): Route = path("registration") {
    get {
      handleWebsocketMessages(echoService)
    }
  }

  def handleWebSocketMessage(message: String): String = {
    println("Server received message: " + message)
    implicit val timeout = Timeout(5000)
    val clientMessage = read[ClientWSMessage](message)
    clientMessage match {
      case phoneRequestMessage@ClientPhoneInPut(phone: String) => {

        val future = system.actorOf(UserActor.props(phone)) ? phoneRequestMessage
        val result = Await.result(future, timeout.duration).asInstanceOf[ServerWSMessage]
        println("Actor answer : " + result)
        return write(result)
      }

      case loginMessage@ClientLogin(phone: String, password: Int) => {
        val future = system.actorOf(UserActor.props(phone)) ? loginMessage
        val result = Await.result(future, timeout.duration).asInstanceOf[ServerWSMessage]
        return write(result)
      }

      case _Any => {
        println("Wrong ClientWSMessage")
        throw new Exception("Wrong ClientWSMessage")
      }
    }


  }

  def close(): Unit = {
    import actorSystem.dispatcher

    binding.flatMap(_.unbind()).onComplete(_ => actorSystem.shutdown())
    println("Server is down...")

  }
}



