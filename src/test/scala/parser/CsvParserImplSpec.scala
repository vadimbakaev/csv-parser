package parser

import akka.actor.ActorSystem
import akka.stream.scaladsl.{Sink, Source}
import akka.testkit.TestKit
import akka.util.ByteString
import common.BaseSpec

import scala.collection.immutable.Seq
import scala.concurrent.Future

class CsvParserImplSpec extends TestKit(ActorSystem()) with BaseSpec {

  import CsvParserImplSpec._

  trait Fixture {
    val subject = new CsvParserImpl()
  }

  "parse" should {
    "correctly parse empty stream" in new Fixture {
      val future: Future[Seq[List[String]]] = Source.empty[ByteString].via(subject.parse).runWith(Sink.seq)

      future.futureValue should have size 0
    }

    "correctly parse header into single element stream" in new Fixture {
      val future: Future[Seq[List[String]]] = Source.single(Header).via(subject.parse).runWith(Sink.seq)

      future.futureValue shouldBe Vector(Seq("Year", "Make", "Model", "Description", "Price"))
    }

    "correctly parse header with escaped lines into stream of elements" in new Fixture {
      val future: Future[Seq[List[String]]] = Source(Header +: Lines).via(subject.parse).runWith(Sink.seq)

      future.futureValue shouldBe Vector(
        Seq("Year", "Make", "Model", "Description", "Price"),
        Seq("1970", "Dodge", "Challenger R/T", "426-cubic inch engine", "30000.00"),
        Seq("1997", "Ford", "E350", "\"ac, abs, moon\"", "3000.00"),
        Seq("1999", "Chevy", "\"Venture \"\"Extended Edition\"\"\"", "\"\"", "4900.00"),
        Seq("1999", "Chevy", "\"Venture \"\"Extended Edition, Very Large\"\"\"", "", "5000.00"),
        Seq("1996", "Jeep", "Grand Cherokee", "\"MUST SELL!\nair, moon roof, loaded\"", "4799.00")
      )
    }
  }
}

object CsvParserImplSpec {
  val Header: ByteString = ByteString("Year,Make,Model,Description,Price\n")
  val Lines: List[ByteString] = List(
    "1970,Dodge,Challenger R/T,426-cubic inch engine,30000.00\n",
    "1997,Ford,E350,\"ac, abs, moon\",3000.00\n",
    "1999,Chevy,\"Venture \"\"Extended Edition\"\"\",\"\",4900.00\n",
    "1999,Chevy,\"Venture \"\"Extended Edition, Very Large\"\"\",,5000.00\n",
    "1996,Jeep,Grand Cherokee,\"MUST SELL!\nair, moon roof, loaded\",4799.00\n",
  ).map(ByteString(_))
}