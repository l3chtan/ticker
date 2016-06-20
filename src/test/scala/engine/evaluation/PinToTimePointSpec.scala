package engine.evaluation

import core.asp.{AspFact, AspProgram}
import engine.asp.oneshot.AspEvaluationEngine
import engine.asp.{PinToTimePoint, now}
import engine.{StreamEntry, _}
import fixtures.TimeTestFixtures
import org.scalatest.FlatSpec
import org.scalatest.Matchers._

/**
  * Created by FM on 13.05.16.
  */
class PinToTimePointSpec extends FlatSpec with TimeTestFixtures {

  "An dataStream at t1" should "contain only 'now(t1).'" in {
    val dataStream: Stream = Set()

    val transformed = AspEvaluationEngine.pinnedInput(t1, dataStream)
    transformed should contain(AspFact(now(t1)))
  }

  "An dataStream 't1 -> a' at t1" should "contain  'a(t1), now(t1).'" in {
    val p = AspProgram()
    val dataStream: Stream = Set(StreamEntry(t1, Set(a)))

    val transformed = AspEvaluationEngine.pinnedInput(t1, dataStream)

    transformed should contain allOf(AspFact(a(t1)), AspFact(now(t1)))
  }

  "A dataStream 't1 -> {a,c}, t2 -> b' at t2" should "contain  'a(t1)., b(t2). now(t2).'" in {
    val dataStream: Stream = Set(
      StreamEntry(t1, Set(a, c)),
      StreamEntry(t2, Set(b))
    )

    val transformed = AspEvaluationEngine.pinnedInput(t2, dataStream)

    transformed should contain allOf(AspFact(a(t1)), AspFact(c(t1)), AspFact(b(t2)), AspFact(now(t2)))
  }
}
