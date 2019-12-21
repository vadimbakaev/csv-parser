package parser

import akka.NotUsed
import akka.stream.scaladsl.{Flow, Framing}
import akka.util.ByteString
import parser.graphstages.BufferSquashStage

trait CsvParser {

  val parse: Flow[ByteString, List[String], NotUsed]

}

class CsvParserImpl(
    eol: String = "\n",
    delimiter: String = ",",
    quotingChar: Char = '"',
    maximumFrameLength: Int = 4028
) extends CsvParser {

  private val isBalancedBy: Char => ByteString => Boolean = quote =>
    byteString => byteString.count(_ == quote.toByte) % 2 == 0

  private val joinVia: String => (ByteString, ByteString) => ByteString = separator => _ ++ ByteString(separator) ++ _

  private val lines: Flow[ByteString, ByteString, NotUsed] =
    Framing
      .delimiter(ByteString(eol), maximumFrameLength, allowTruncation = true)
      .via(Flow.fromGraph(new BufferSquashStage[ByteString](isBalancedBy(quotingChar), joinVia(eol))))

  private val columns: Flow[ByteString, List[String], NotUsed] =
    Flow[ByteString]
      .map(byteSting => splitByPattern(quotingChar, delimiter)(byteSting.utf8String))

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
      .map {
        case ""                                    => null
        case x if x == s"$quotingChar$quotingChar" => ""
        case other =>
          other.replaceAll(s"(?<!$quotingChar)$quotingChar(?!$quotingChar)|($quotingChar)$quotingChar|$quotingChar",
                           "$1")
      }

}
