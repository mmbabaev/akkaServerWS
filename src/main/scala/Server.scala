import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.ws.{Message, TextMessage}
import akka.http.scaladsl.server.Directives._
import akka.pattern.ask
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Flow
import akka.util.Timeout
import upickle.default._

import akka.event.Logging
import scala.concurrent.Await
import scala.concurrent.duration._

object Server extends App {

  val server = new Server()

  scala.io.StdIn.readLine()
  server.close()
}

class Server {

  val sd= new Smsc()
  println(sd.get_balance())

  implicit val actorSystem = ActorSystem("akka-system")
  implicit val flowMaterializer = ActorMaterializer()

  val system = ActorSystem("mySystem")
  val interface = "localhost"
  val port = 8080

  val echoService: Flow[Message, Message, _] = Flow[Message].map {
    case TextMessage.Strict(txt) => TextMessage(handleWebSocketMessage(txt))
    case _ => TextMessage("Message type unsupported")
  }

  val route = path("registration") {
    get {
      handleWebsocketMessages(echoService)
    }
  }

  val binding = Http().bindAndHandle(route, interface, port)

  println(s"Server is now online at http://$interface:$port")
  println(s"WebSocket: ws://$interface:$port/ws")
  println(s"Press RETURN to stop...")


  def handleWebSocketMessage(message: String): String = {
    println("Server received message: " + message)
    implicit val timeout = Timeout(200000 millis)
    val clientMessage = read[ClientWSMessage](message)
    clientMessage match {
      case phoneRequestMessage@ClientPhoneInPut(phone: String) =>
        val future = system.actorOf(UserActor.props(phone)) ? phoneRequestMessage
        val result = Await.result(future, timeout.duration).asInstanceOf[ServerWSMessage]
        println("Actor answer : " + result)
        write(result)
      case loginMessage@ClientLogin(phone: String, password: Int) =>
        val future = system.actorOf(UserActor.props(phone)) ? loginMessage
        val result = Await.result(future, timeout.duration).asInstanceOf[ServerWSMessage]
        write(result)
      case _ =>
        ("Wrong ClientWSMessage")
    }
  }

  def close(): Unit = {
    import actorSystem.dispatcher

    binding.flatMap(_.unbind()).onComplete(_ => actorSystem.shutdown())
    println("Server is down...")

  }
}



