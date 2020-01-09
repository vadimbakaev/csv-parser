package parser

object Application extends App {

  val bufferedSource = io.Source.fromFile("src/main/resources/automobiles.csv")

  for (line <- Parser.parse(bufferedSource.iter.toStream)()) println(line)

  bufferedSource.close

}
