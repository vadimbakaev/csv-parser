package parser

import akka.NotUsed
import akka.stream.scaladsl.{Flow, Framing}
import akka.util.ByteString
import parser.CsvParserImpl._

trait CsvParser {

  val parse: Flow[ByteString, List[String], NotUsed]

}

class CsvParserImpl(
    eol: Char = '\n',
    delimiter: Char = ',',
    //                     quotingChar: Char = '"'
) extends CsvParser {

  //TODO handle quoted eol
  private val lines: Flow[ByteString, ByteString, NotUsed] =
    Framing.delimiter(ByteString(eol), DefaultFrame, AllowTruncation)

  //TODO handle quoted delimiter
  private val columns: Flow[ByteString, List[String], NotUsed] =
    Flow[ByteString]
      .map(bs => bs.utf8String.split(delimiter).toList)

  override val parse: Flow[ByteString, List[String], NotUsed] =
    Flow[ByteString]
      .via(lines)
      .via(columns)

}

object CsvParserImpl {
  val DefaultFrame: Int        = 1024
  val AllowTruncation: Boolean = true
}
