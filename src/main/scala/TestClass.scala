import akka.actor._
import akka.pattern.ask
import akka.util.Timeout

import scala.concurrent.Await

/**
 * Created by Yaroslav on 06.08.15.
 */
class TestClass {

}
sealed trait a

case class AskNameMessage()
case class Answer() extends a
class TestActor extends Actor {
  def receive = {
    case AskNameMessage => // respond to the "ask" request
      sender ! Answer()
    case _ => println("that was unexpected")
  }
}

object AskTest extends App {

  // create the system and actor
  val system = ActorSystem("AskTestSystem")
  val myActor = system.actorOf(Props[TestActor], name = "myActor")

  // (1) this is one way to "ask" another actor
  implicit val timeout = Timeout(5000)
  val future = myActor ? AskNameMessage
  val result = Await.result(future, timeout.duration).asInstanceOf[a]
  println(upickle.default.write(result))


  system.shutdown

}
