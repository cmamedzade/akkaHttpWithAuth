import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import DomainModel._
import akka.http.scaladsl.model.headers.RawHeader
import akka.http.scaladsl.server.directives.Credentials
import authentikat.jwt.{JsonWebToken, JwtClaimsSet, JwtClaimsSetMap, JwtHeader}

import java.util.concurrent.TimeUnit
import scala.util.{Failure, Success}

object CashInService {

  implicit val system = ActorSystem(Behaviors.empty,"CashIn")
  implicit val ex = system.executionContext
  final val AccessTokenHeaderName = "X-Access-Token"

  private val tokenExpiryPeriodInMinutes = 5
  private val secretKey               = "super_secret_key"
  private val header                  = JwtHeader("HS256")

  def authenticate(credentials: Credentials): Option[String] = {
    credentials match {
      case p @ Credentials.Provided(identifier) if p.verify("pasword123") && p.identifier.equals("somename") => Some(identifier)
      case _ => None
    }
  }

  private def setClaims(username: String, expiryPeriodInDays: Long): JwtClaimsSetMap =
    JwtClaimsSet(
      Map("user" -> username,
        "expiredAt" -> (System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(expiryPeriodInDays)))
    )

  protected def getBalanceWithAuth(persistent: Persistent, id: Int) = {
    optionalHeaderValueByName("Authorization") {
      case Some(jwt) if isTokenExpired(jwt) =>
        complete(StatusCodes.Forbidden -> "Session expired!")
      case Some(jwt) if JsonWebToken.validate(jwt, secretKey) =>
        onComplete(persistent.getBalance(id)) {
          case Failure(exception) =>
            println(s"error: ${exception.getMessage}")
            complete(StatusCodes.InternalServerError)
          case Success(seq) => complete(Balance(seq.head._1, seq.head._2, seq.head._3, seq.head._4))
        }
    }
  }

  protected def createTransactionWithAuth(persistent: Persistent, topUp: TopUp) = {
    optionalHeaderValueByName("Authorization") {
      case Some(jwt) if isTokenExpired(jwt) =>
        complete(StatusCodes.Forbidden -> "Session expired!")
      case Some(jwt) if JsonWebToken.validate(jwt, secretKey) =>
        onComplete(persistent.insert(topUp)(system)) {
          case Failure(exception) =>
            println(s"unable connect: ${exception.getMessage}")
            complete(StatusCodes.InternalServerError)
          case Success(done) => complete(StatusCodes.OK,"transaction added...")
        }
    }
  }

  private def getClaims(jwt: String): Map[String, String] = jwt match {
    case JsonWebToken(_, claims, _) => claims.asSimpleMap.getOrElse(Map.empty[String, String])
  }

  private def isTokenExpired(jwt: String): Boolean =
    getClaims(jwt).get("expiredAt").exists(_.toLong < System.currentTimeMillis())

  val persistent = new  Persistent

  val route =
    concat(
      post{
        path("getToken"){
          authenticateBasic(realm = "login", authenticate){
            username =>
              val claims = setClaims(username, tokenExpiryPeriodInMinutes)
              respondWithHeader(RawHeader(AccessTokenHeaderName, JsonWebToken(header, claims, secretKey))) {
                complete(StatusCodes.OK)
              }
          }
        }
      },
      get {
        path("getBalance" / IntNumber) { id =>
          getBalanceWithAuth(persistent,id)
        }
      },
      post {
        path("create-transaction") {
          entity(as[TopUp]) { topUp =>
            createTransactionWithAuth(persistent,topUp)
          }
        }
      }
    )


  def main(args: Array[String]): Unit = {

    "docker start cashin" !   // first create docker container by name cashin
    Thread.sleep(1000)

    val binding = Http()
      .newServerAt("127.0.0.1", 8080)
      .bind(route)

    binding.onComplete {
      case Success(binding) =>
        val address = binding.localAddress
        println("CashIn server bound to {}:{}", address.getHostString, address.getPort)
      case Failure(ex) =>
        println("Failed to bind endpoint, terminating system", ex)
        system.terminate()
    }
  }
}