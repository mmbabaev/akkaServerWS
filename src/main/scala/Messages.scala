/**
 * Created by Yaroslav on 06.08.15.
 */



//From Client
sealed trait ClientWSMessage
case class ClientPhoneInPut(phone: String) extends ClientWSMessage
case class ClientLogin(phone: String, password: Int) extends ClientWSMessage



//From Server
sealed trait ServerWSMessage
case class ServerSuccessPhone() extends ServerWSMessage
case class ServerSuccessRegistration() extends ServerWSMessage
case class ServerError(title: String, message: String) extends ServerWSMessage