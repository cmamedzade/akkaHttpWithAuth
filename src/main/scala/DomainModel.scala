import spray.json.DefaultJsonProtocol.jsonFormat4
import spray.json.DefaultJsonProtocol._

object DomainModel {

  trait AuthResult

  case object UnAuth extends AuthResult
  case object Auth extends AuthResult
  case object Expired extends AuthResult

  case class TopUp(id: Int, nameOfTerminal: String, clientName: String, amount: Double)
  case class Balance(id: Int, nameOfTerminal: String, clientName: String, amount: Double)
  case class User(name: String, password: String)


  type Transaction = (Int, String, String, Double)

  implicit val topUp = jsonFormat4(TopUp)
  implicit val balance = jsonFormat4(Balance)
  implicit val usr = jsonFormat2(User)

}