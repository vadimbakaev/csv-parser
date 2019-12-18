package parser

import akka.NotUsed
import akka.stream.scaladsl.Flow
import akka.util.ByteString

trait CsvParser {

  val parse: Flow[ByteString, List[String], NotUsed]

}

class CsvParserImpl(
    eol: String = "\r\n",
    delimiter: String = ",",
    quotingChar: Char = '"'
) extends CsvParser {

  private val lines: Flow[ByteString, String, NotUsed] =
    Flow[ByteString]
      .mapConcat(byteString => splitByPattern(quotingChar, eol)(byteString.utf8String))

  private val columns: Flow[String, List[String], NotUsed] =
    Flow[String]
      .map(splitByPattern(quotingChar, delimiter))

  override val parse: Flow[ByteString, List[String], NotUsed] =
    lines.via(columns)

  private def splitByPattern(quotingChar: Char, separator: String)(str: String): List[String] =
    str
      .split(
        s"$separator" +                               // match a separator
        s"(?=" +                                      // start positive look ahead
        s"(?:" +                                      // start non-capturing group 1
        s"[^$quotingChar]*" +                         //     match 'otherThanQuote' zero or more times
        s"$quotingChar[^$quotingChar]*$quotingChar" + //     match 'quotedString'
        s")*" +                                       //   end group 1 and repeat it zero or more times
        s"[^$quotingChar]*" +                         //   match 'otherThanQuote'
        s"$$" +                                       // match the end of the string
        s")" // stop positive look ahead
      )
      .toList
}
