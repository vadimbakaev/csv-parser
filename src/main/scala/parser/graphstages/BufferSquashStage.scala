package parser.graphstages

import akka.stream.{Attributes, FlowShape, Inlet, Outlet}
import akka.stream.stage.{GraphStage, GraphStageLogic, InHandler, OutHandler}

import scala.util.Try

class BufferSquashStage[A](predicate: A => Boolean, f: (A, A) => A) extends GraphStage[FlowShape[A, A]] {

  val inPort: Inlet[A]   = Inlet[A]("in")
  val outPort: Outlet[A] = Outlet[A]("out")

  override def createLogic(inheritedAttributes: Attributes): GraphStageLogic = new GraphStageLogic(shape) {
    var acc: List[A] = Nil
    var nextVale: A  = _

    setHandlers(
      inPort,
      outPort,
      new InHandler with OutHandler {

        override def onPush(): Unit = {

          Try {

            val nextElement = grab(inPort)
            acc = acc :+ nextElement
            nextVale = acc.reduce(f)

            if (predicate(nextVale)) {
              push(outPort, nextVale)
              acc = Nil
            } else {
              pull(inPort)
            }

          }.recover {
            case t: Throwable => failStage(t)
          }

          ()
        }

        override def onPull(): Unit = pull(inPort)

        override def onUpstreamFinish(): Unit = {
          if (acc.nonEmpty) push(outPort, nextVale)
          super.onUpstreamFinish()
        }

      }
    )
  }

  override def shape: FlowShape[A, A] = FlowShape(inPort, outPort)
}
