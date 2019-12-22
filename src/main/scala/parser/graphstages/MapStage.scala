package parser.graphstages

import akka.stream.{Attributes, FlowShape, Inlet, Outlet}
import akka.stream.stage.{GraphStage, GraphStageLogic, InHandler, OutHandler}

class MapStage[A](inHeader: Option[List[A]] = None) extends GraphStage[FlowShape[List[A], Map[A, A]]] {

  val inPort: Inlet[List[A]]     = Inlet[List[A]]("in")
  val outPort: Outlet[Map[A, A]] = Outlet[Map[A, A]]("out")

  override def createLogic(inheritedAttributes: Attributes): GraphStageLogic = new GraphStageLogic(shape) {

    var header: Option[List[A]] = inHeader

    setHandlers(
      inPort,
      outPort,
      new InHandler with OutHandler {
        override def onPush(): Unit = {
          val nextElement = grab(inPort)

          if (header.isDefined) header.foreach { h =>
            push(outPort, h.zip(nextElement).toMap)
          } else {
            header = Some(nextElement)
            this.onPull()
          }
        }

        override def onPull(): Unit = pull(inPort)
      }
    )
  }

  override def shape: FlowShape[List[A], Map[A, A]] = FlowShape(inPort, outPort)

}
