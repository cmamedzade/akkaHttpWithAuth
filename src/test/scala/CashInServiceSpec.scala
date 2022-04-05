import akka.http.scaladsl.model._
import akka.http.scaladsl.testkit.ScalatestRouteTest
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import akka.actor.testkit.typed.scaladsl.ActorTestKit
import akka.http.scaladsl.model.headers.BasicHttpCredentials
import org.scalamock.scalatest.MockFactory

class CashInServiceSpec extends  AnyWordSpec  with Matchers with ScalaFutures with ScalatestRouteTest with MockFactory {

  lazy val testKit = ActorTestKit()
  implicit def typedSystem = testKit.system
  override def createActorSystem(): akka.actor.ActorSystem =
    testKit.system.classicSystem

  "CashInRoutes" should {

    val routes = CashInService.route

    "user authenticate" in {

      val credential = BasicHttpCredentials("somename","pasword123")
      val request = HttpRequest(HttpMethods.POST,uri = "/getToken").addCredentials(credential)

      request ~> routes ~> check {
        status should ===(StatusCodes.OK)
        contentType should ===(ContentTypes.`text/plain(UTF-8)`)
        entityAs[String] should ===("OK")
      }
    }
  }
}