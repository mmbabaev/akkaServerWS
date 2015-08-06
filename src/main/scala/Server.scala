import akka.actor.ActorSystem
import akka.http.javadsl.server.Directives
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.ws.{TextMessage, Message}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server._
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Flow

import scala.io.StdIn


object Server extends App {
  val system = ActorSystem("mySystem")

  val server = new Server()
  val testUser = system.actorOf(UserActor.props("000", server), "test")


  //testUser ! Register(1234)

  readLine()
  server.close()
}

class Server {
  implicit val actorSystem = ActorSystem("akka-system")
  implicit val flowMaterializer = ActorMaterializer()

  val interface = "localhost"
  val port = 8080

  val route = getRoute()

  val binding = Http().bindAndHandle(route, interface, port)
  println(s"Server is now online at http://$interface:$port\nWebsoket: ws://$interface:$port/ws\nPress RETURN to stop...")

  val echoService: Flow[Message, Message, _] = Flow[Message].map {
    case TextMessage.Strict(txt) => TextMessage(handleWebSocketMessage(txt))
    case _ => TextMessage("Message type unsupported")
  }

  def getRoute(): Route = path("ws") {
    get {
      handleWebsocketMessages(echoService)
    }
  }

  def handleWebSocketMessage(message: String): String = {
    "echo: " + message
  }


//  // commands
//  val REGISTER_COMMAND = "REGISTER_COMMAND"
//  val ERROR_COMMAND = "ERROR_COMMAND"
//  val NOTIFICATION_COMMAND = "NOTIFICATION_COMMAND"
//
//  def getAnswer(messageString: String): String = {
//    val json: JsValue = Json.parse(messageString)
//
//    val command = (json \ "command").as[String]
//
//    command match {
//      case REGISTER_COMMAND => {
//        val phone = (json \ "phone").as[String]
//
//        if (isAllowablePhoneNumber(phone) == false) {
//          return errorAnswer("Phone " + phone + " was used too many times.")
//        }
//
//        else {
//          println("sending message...")
//          val sms = new Smsc()
//          val password = 4242 //  gen.random?
//          val message = "Ваш пароль: " + password
//
//          val serviceAnswer = sms.send_sms(phone, message, 0, "", "1", 0, "mbabaev", "")
//
//          if (serviceAnswer.length == 4) {
//            // TODO: add phone + password to database
//          }
//          else {
//            errorAnswer("Sms cannot be send")
//          }
//
//          return notificationAnswer("Wait for SMS message with security code!")
//        }
//      }
//
//      case _ =>
//        println("Unknown command! " + command)
//        return errorAnswer("Unknown command! " + command)
//    }

  def isAllowablePhoneNumber(phoneNumber: String): Boolean = {

    //TODO: check is phone number spam, was phone registered, etc...

    return true
  }



  def close(): Unit = {
    import actorSystem.dispatcher

    binding.flatMap(_.unbind()).onComplete(_ => actorSystem.shutdown())
    println("Server is down...")

  }
}