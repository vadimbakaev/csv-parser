package parser

import java.nio.file.Paths

import akka.actor.ActorSystem
import akka.stream.scaladsl.FileIO

object Application extends App {
  implicit val actorSystem: ActorSystem = ActorSystem()

  val csvParser: CsvParser = new CsvParserImpl()

  FileIO
    .fromPath(Paths.get("src/main/resources/automobiles.csv"))
    .via(csvParser.parse)
    .runForeach(println)

}
