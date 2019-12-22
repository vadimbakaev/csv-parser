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
    val subject = new CsvParserImpl(eol = Eol.toString, delimiter = Delimiter, quotingChar = QuotingChar)
  }

  "The parser" should {
    "handle empty stream" in new Fixture {
      val future: Future[Seq[List[String]]] = Source.empty[ByteString].via(subject.parse).runWith(Sink.seq)

      future.futureValue should have size 0
    }

    "handle single element stream" in new Fixture {
      val future: Future[Seq[List[String]]] = Source.single(Header).via(subject.parse).runWith(Sink.seq)

      future.futureValue shouldBe Vector(Seq("Year", "Make", "Model", "Description", "Price"))
    }

    "handle null element stream" in new Fixture {
      val future: Future[Seq[List[String]]] = Source.single(ByteString(s"${QuotingChar}a${QuotingChar},,c")).via(subject.parse).runWith(Sink.seq)

      future.futureValue shouldBe Vector(Seq(s"a", null, "c"))
    }

    "handle new line characters embedded in a quoted cell" in new Fixture {
      val future: Future[Seq[List[String]]] = Source.single(ByteString("a,\"a split\ncell\",\nb,\"something else\"")).via(subject.parse).runWith(Sink.seq)

      future.futureValue shouldBe Vector(
        Seq("a", "a split\ncell", null),
        Seq("b", "something else")
      )
    }

    "handle end of the line as null" in new Fixture {
      val future: Future[Seq[List[String]]] = Source.single(ByteString("a,b,")).via(subject.parse).runWith(Sink.seq)

      future.futureValue.head shouldBe  Seq("a", "b", null)
    }

    "handle partially quoted fields" in new Fixture {
      val future: Future[Seq[List[String]]] = Source.single(ByteString("\"abc,\"onetwo,three,doremi")).via(subject.parse).runWith(Sink.seq)

      future.futureValue.head shouldBe  Seq("abc,onetwo", "three", "doremi")
    }

    "correctly parse header with escaped lines into stream of elements" in new Fixture {
      val future: Future[Seq[List[String]]] = Source(Header +: Lines).via(subject.parse).runWith(Sink.seq)

      private val resultList: Seq[List[String]] = future.futureValue
      resultList.head shouldBe Seq("Year", "Make", "Model", "Description", "Price")
      resultList(1) shouldBe Seq("1970", "Dodge", "Challenger R/T", "426-cubic inch engine", "30000.00")
      resultList(2) shouldBe  Seq("1997", "Ford", "E350", "ac, abs, moon", "3000.00")
      resultList(3) shouldBe  Seq("1999", "Chevy", s"Venture ${QuotingChar}Extended Edition$QuotingChar", "", "4900.00")
      resultList(4) shouldBe  Seq("1999", "Chevy", s"Venture ${QuotingChar}Extended Edition, Very Large$QuotingChar", null, "5000.00")
      resultList(5) shouldBe  Seq("1996", "Jeep", "Grand Cherokee", s"""MUST SELL!${Eol}air, moon roof, loaded""", "4799.00")
    }
  }
}

object CsvParserImplSpec {
  val Eol: Char = '\n'
  val Delimiter: String = ","
  val QuotingChar: Char = '"'
  val Header: ByteString = ByteString(s"Year,Make,Model,Description,Price$Eol")
  val Lines: List[ByteString] = List(
    s"""1970,Dodge,Challenger R/T,426-cubic inch engine,30000.00${Eol}1997,Ford,E350,${QuotingChar}ac, abs, moon${QuotingChar},3000.00${Eol}1999,Chevy,""",
    s"""${QuotingChar}Venture ${QuotingChar}${QuotingChar}Extended Edition${QuotingChar}${QuotingChar}${QuotingChar},${QuotingChar}${QuotingChar},4900.00$Eol""",
    s"""1999,Chevy,${QuotingChar}Venture ${QuotingChar}${QuotingChar}Extended""",
    s""" Edition, Very Large${QuotingChar}${QuotingChar}${QuotingChar},,5000.00$Eol""",
    s"""1996,Jeep,Grand Cherokee,${QuotingChar}MUST SELL!${Eol}air, moon roof, loaded${QuotingChar},4799.00$Eol"""
  ).map(ByteString(_))
}