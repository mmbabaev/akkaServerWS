

import java.util.{Calendar, Date}


import akka.actor._
import akka.persistence._

import scala.util.Random


/**
 * Created by Mihail on 05.08.15.
 */

case class Event(password: Int, dayCount: Int, date : Date)

trait UserCommand

case class Save() extends UserCommand



case class UserState(password: Int = 0, dayRequestCount: Int = 0, lastUpdate: Date = Calendar.getInstance().getTime()) {

  def update(ev: Event): UserState = {

    //println("update")
    copy(ev.password, ev.dayCount, ev.date)

  }

  def isTodayDate = {
    (Calendar.getInstance().getTime.getDate == lastUpdate.getDate &&
      (Calendar.getInstance().getTime.getMonth == lastUpdate.getMonth) &&
      (Calendar.getInstance().getTime.getYear == lastUpdate.getYear))
  }

  def isSMSLimitExceeded() = {
    isTodayDate && dayRequestCount >= 5
  }

  def isRegistered() = {
    !(password == 0)
  }
}

object UserActor {
  def props(phone: String) = Props(new UserActor(phone))
}

class UserActor(phone: String) extends PersistentActor {
  override def persistenceId = phone


  var state = UserState()

  val receiveRecover: Receive = {
    case event: Event =>
      println("receiveRecover")
      state.update(event)
    case SnapshotOffer(_, snapshot: UserState) => state = snapshot
  }

  val receiveCommand: Receive = {
    receive
  }

    override def receive = {

      case ClientPhoneInPut(phone: String) => {
        if (state.isTodayDate == false) {
          state = UserState(state.password, 0, Calendar.getInstance().getTime)
        }
        if (state.isSMSLimitExceeded()) {
          sender() ! ServerError("ERROR", "The limit of request was exceeded.")
        }
        else {
          val sms = new Smsc()
          persist(Event(generatePassword(), state.dayRequestCount + 1, Calendar.getInstance().getTime)) { event =>
            updateState(event)
            context.system.eventStream.publish(event)
            saveSnapshot(state)
          }
          //val serviceAnswer = sms.send_sms(phone, "Your password:" + state.password , 0, "", "1", 0, "iDecide", "")
          //TODO: Handle SMSc server error
          println("new pass =  " + state.password)
          println("SMS was sent")
          sender ! ServerSuccessPhone()
        }
      }

      case ClientLogin(phone: String, password: Int) => {
        if (!phone.equals(persistenceId)) {
          sender() ! ServerError("ERROR", "Wrong phone number")
        }
        else {
          if (!password.equals(state.password)) {
            sender() ! ServerError("ERROR", "Wrong password")
          }
          else {
            sender() ! ServerSuccessRegistration()
          }
        }
      }



      case _ => println("WTF")
    }


  def generatePassword() = {

    val gen = new Random()
    val password = 10000 + gen.nextInt(89999)
    state = UserState(password, state.dayRequestCount, state.lastUpdate)
    password
  }

  def updateState(ev: Event) {
    state = state.update(ev)
  }
}

