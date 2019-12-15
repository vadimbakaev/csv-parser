package common

import org.mockito.{ArgumentMatchersSugar, MockitoSugar}
import org.scalatest.ParallelTestExecution
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike

import scala.concurrent.duration._
import scala.language.postfixOps

trait BaseSpec
    extends AnyWordSpecLike
    with MockitoSugar
    with ArgumentMatchersSugar
    with Matchers
    with ScalaFutures
    with ParallelTestExecution {
  implicit val config: PatienceConfig = PatienceConfig(2 seconds, 20 milliseconds)
}
