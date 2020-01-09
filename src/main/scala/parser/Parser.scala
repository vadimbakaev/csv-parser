package parser

object Parser {

  def parse(
      chars: Stream[Char]
  )(
      eol: String = "\n",
      delimiter: Char = ',',
      quotation: Char = '"',
      maybeHeader: Option[List[String]] = None
  ): Stream[Map[String, String]] = {

    def lines(xs: Stream[Char],
              acc: List[String] = Nil,
              col: Option[String] = None,
              toEsc: Boolean = false): Stream[List[String]] =
      xs match {
        case empty if empty.isEmpty                      => Stream.empty
        case _ if xs.startsWith(eol) && toEsc            => lines(xs.drop(eol.length), acc, col.map(eol + _), toEsc)
        case _ if xs.startsWith(eol)                     => (col.map(_.reverse).orNull :: acc).reverse #:: lines(xs.drop(eol.length))
        case `delimiter` #:: xs if toEsc                 => lines(xs, acc, col.map(delimiter + _), toEsc)
        case `delimiter` #:: xs                          => lines(xs, col.map(_.reverse).orNull :: acc, col = None, toEsc)
        case `quotation` #:: `quotation` #:: xs if toEsc => lines(xs, acc, col.map(quotation + _), toEsc)
        case `quotation` #:: xs                          => lines(xs, acc, Some("" + col.getOrElse("")), toEsc = !toEsc)
        case x #:: xs                                    => lines(xs, acc, Some(x + col.getOrElse("")), toEsc)
      }

    maybeHeader
      .map(Stream(_))
      .getOrElse(Stream.empty) #::: lines(chars) match {
      case header #:: xs => xs.map(header.zip(_).toMap)
      case _             => Stream.empty
    }
  }

}
