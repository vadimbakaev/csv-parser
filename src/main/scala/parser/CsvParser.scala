package parser

import akka.NotUsed
import akka.stream.scaladsl.{Flow, Framing}
import akka.util.ByteString
import parser.graphstages.BufferSquashStage

import scala.annotation.tailrec

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
      .map(byteSting => splitUnquoted(quotingChar, delimiter)(byteSting.utf8String))

  override val parse: Flow[ByteString, List[String], NotUsed] =
    lines.via(columns)

  private def splitUnquoted(quotingChar: Char, separator: String)(str: String): List[String] =
    bufferingByQuotingChars(quotingChar, separator: String, str.split(separator, Int.MaxValue).toList)
      .map {
        case ""                                    => null
        case x if x == s"$quotingChar$quotingChar" => ""
        case other                                 => other.replaceAll(s"($quotingChar)$quotingChar|$quotingChar", "$1")
      }

  @tailrec
  private def bufferingByQuotingChars(quotingChar: Char,
                                      separator: String,
                                      xs: List[String],
                                      result: List[String] = Nil,
                                      acc: List[String] = Nil): List[String] = {
    val isBalanced: String => Boolean = _.count(_ == quotingChar) % 2 == 0
    xs match {
      case Nil =>
        result
      case x :: xs =>
        val nextAcc             = acc :+ x
        val nextStringCandidate = nextAcc.mkString(separator)

        if (isBalanced(nextStringCandidate)) {
          bufferingByQuotingChars(quotingChar, separator, xs, result :+ nextStringCandidate)
        } else {
          bufferingByQuotingChars(quotingChar, separator, xs, result, nextAcc)
        }
    }
  }

}
