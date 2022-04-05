import DomainModel.TopUp
import akka.Done
import akka.actor.typed.ActorSystem
import akka.stream.alpakka.slick.scaladsl.{Slick, SlickSession}
import akka.stream.scaladsl.{Sink, Source}
import slick.basic.DatabaseConfig
import slick.dbio
import slick.jdbc.JdbcProfile
import slick.jdbc.PostgresProfile.api._

import scala.concurrent.Future


class Persistent{

  private val config = DatabaseConfig.forConfig[JdbcProfile]("slick-postgres")
  private implicit val session = SlickSession.forConfig(config)

  private class TransactionTable(tag: Tag) extends Table[(Int, String, String, Double)](tag, "transaction") {
    def id = column[Int]("id")
    def nameOfTerminal = column[String]("nameOfTerminal")
    def nameOfClient = column[String]("nameOfClient")
    def amount = column[Double]("amount")
    def * = (id, nameOfTerminal, nameOfClient, amount)
  }

  private val typedInsert: TableQuery[TransactionTable] = TableQuery[TransactionTable]

  protected def writeTo(input: TopUp): dbio.DBIO[Int] = typedInsert += (input.id, input.nameOfTerminal, input.clientName, input.amount)

  def insert(transaction: TopUp)(implicit actorSystem: ActorSystem[Nothing]): Future[Done] = {

    Source
      .single(transaction)
      .runWith(Slick.sink(parallelism = 1, writeTo))

  }

  def getBalance(id: Int)(implicit system: ActorSystem[_]): Future[Seq[(Int, String, String, Double)]] = {

    Slick
      .source(TableQuery[TransactionTable].filter(_.id === id).result)
      .runWith(Sink.seq)
  }
}