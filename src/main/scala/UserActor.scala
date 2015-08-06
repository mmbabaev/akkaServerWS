

import java.util.{Calendar, Date}

import akka.actor._
import akka.persistence._
import akka.pattern.ask

import scala.util.Random

/**
 * Created by Mihail on 05.08.15.
 */

case class Event(password: Int)

trait UserCommand
case class Register(userInputPassword: Int) extends UserCommand
case class CheckPhoneNumber(phone: String) extends UserCommand
case class Save() extends UserCommand

case class UserState(password: Int = 0, dayRequestCount: Int = 0, lastUpdate: Date = Calendar.getInstance().getTime()) {
  def update(password: Int,
             dayCount: Int = this.dayRequestCount,
             lastUpdate: Date = this.lastUpdate): UserState = copy(password, dayCount, lastUpdate)

  def isAllowable() = {
    if (Calendar.getInstance().getTime.getDate == lastUpdate.getDate &&
        dayRequestCount >= 5) {
      false
    }
    else {
      true
    }
  }
}

object UserActor {
  def props(phone: String, server: Server) = Props(new UserActor(phone, server: Server))
}

class UserActor(phone: String, server: Server) extends PersistentActor {
  override def persistenceId = phone



  var state = UserState()

  val receiveRecover: Receive = {
    case event: Event => state.update(event.password)
    case SnapshotOffer(_, snapshot: UserState) => state = snapshot
  }

  val receiveCommand: Receive = {

    case Register(pass) =>

      println("id: " + persistenceId + ", pass: " + pass + ", password: " + state.password)
      persist(Event(pass)) { event =>
        state.update(pass)

        println("id: " + persistenceId + ", pass: " + pass + ", password: " + state.password)
        context.system.eventStream.publish(event)
        saveSnapshot(state)
      }

    case CheckPhoneNumber(phone) => {
      if (state.password == 0) {

      }
      else {

      }

      server
    }

    case Save => saveSnapshot(state)

    //case _ => println("TEST")

  }

  def generatePassword() = {
    val gen = new Random()
    val password = 10000 + gen.nextInt(89999)
    state = UserState(password)
  }

}

