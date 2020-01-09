package parser

import common.BaseSpec
import parser.ParserSpec._

import scala.collection.immutable.Seq

class ParserSpec extends BaseSpec {

  "The parser" should {
    "handle empty iterator" in {
      Parser.parse("".toStream)(eol = Eol, delimiter = Delimiter, quotation = Quotation) should have size 0
    }

    "handle single line" in {
      Parser.parse(Header.toStream)(eol = Eol, delimiter = Delimiter, quotation = Quotation) should have size 0
    }

    "handle null element line" in {
      Parser
        .parse(s"${Quotation}a$Quotation,,c$Eol".toStream)(eol = Eol,
                                                           delimiter = Delimiter,
                                                           quotation = Quotation,
                                                           maybeHeader = Some(List("1", "2", "3")))
        .toList shouldBe List(
        Map(
          "1" -> "a",
          "2" -> null,
          "3" -> "c"
        )
      )
    }

    "handle new line characters embedded in a quoted cell" in {
      Parser
        .parse("a,\"a split\ncell\",\n,\"something else\"\n".toStream)(
          eol = Eol,
          delimiter = Delimiter,
          quotation = Quotation,
          maybeHeader = Some(List("1", "2", "3"))
        )
        .toList shouldBe Vector(
        Map("1" -> "a", "2"  -> s"a split${Eol}cell", "3" -> null),
        Map("1" -> null, "2" -> s"something else")
      )
    }

    "handle end of the line as null" in {
      Parser
        .parse(s"a,b,$Eol".toStream)(eol = Eol,
                                     delimiter = Delimiter,
                                     quotation = Quotation,
                                     maybeHeader = Some(List("1", "2", "3")))
        .head shouldBe Map("1" -> "a", "2" -> "b", "3" -> null)
    }

    "handle partially quoted fields" in {
      Parser
        .parse(s"""\"abc,\"onetwo,three,doremi${Eol}""".toStream)(eol = Eol,
                                                                  delimiter = Delimiter,
                                                                  quotation = Quotation,
                                                                  maybeHeader = Some(List("1", "2", "3")))
        .toList shouldBe List(
        Map("1" -> "abc,onetwo", "2" -> "three", "3" -> "doremi")
      )
    }

    "correctly parse header with escaped parse into stream of elements" in {
      val resultList: Seq[Map[String, String]] =
        Parser.parse((Header ++ Lines).toStream)(eol = Eol, delimiter = Delimiter, quotation = Quotation)
      val header = Seq("Year", "Make", "Model", "Description", "Price")

      resultList.head shouldBe header
        .zip(Seq("1970", "Dodge", "Challenger R/T", "426-cubic inch engine", "30000.00"))
        .toMap
      resultList(1) shouldBe header.zip(Seq("1997", "Ford", "E350", "ac, abs, moon", "3000.00")).toMap
      resultList(2) shouldBe header
        .zip(Seq("1999", "Chevy", s"Venture ${Quotation}Extended Edition$Quotation", "", "4900.00"))
        .toMap
      resultList(3) shouldBe header
        .zip(Seq("1999", "Chevy", s"Venture ${Quotation}Extended Edition, Very Large$Quotation", null, "5000.00"))
        .toMap
      resultList(4) shouldBe header
        .zip(Seq("1996", "Jeep", "Grand Cherokee", s"""MUST SELL!${Eol}air, moon roof, loaded""", "4799.00"))
        .toMap
    }
  }
}

object ParserSpec {
  val Eol: String     = "\n"
  val Delimiter: Char = ','
  val Quotation: Char = '"'

  val Header: String = s"Year,Make,Model,Description,Price$Eol"
  val Lines: String = List(
    s"""1970,Dodge,Challenger R/T,426-cubic inch engine,30000.00${Eol}1997,Ford,E350,${Quotation}ac, abs, moon${Quotation},3000.00${Eol}1999,Chevy,""",
    s"""${Quotation}Venture ${Quotation}${Quotation}Extended Edition${Quotation}${Quotation}${Quotation},${Quotation}${Quotation},4900.00$Eol""",
    s"""1999,Chevy,${Quotation}Venture ${Quotation}${Quotation}Extended""",
    s""" Edition, Very Large${Quotation}${Quotation}${Quotation},,5000.00$Eol""",
    s"""1996,Jeep,Grand Cherokee,${Quotation}MUST SELL!${Eol}air, moon roof, loaded${Quotation},4799.00$Eol"""
  ).mkString("")
}
