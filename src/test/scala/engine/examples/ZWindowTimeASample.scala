package engine.examples

import core.Atom
import core.lars._
import engine.asp.evaluation.{AspEvaluationEngine, StreamingClingoInterpreter}
import engine.asp.{AspPullEvaluationEngine, now}
import engine.config.BuildEngine
import org.scalatest.FlatSpec
import org.scalatest.Matchers._
import org.scalatest.OptionValues._

/**
  * Created by FM on 26.04.16.
  */
class ZWindowTimeASample extends FlatSpec {
  val aspProgram =
    """
    z(X) :- w2ta(U,T), X = U + 1.
    w2ta(U,T) :- now(T), reach(U,T), a(U).
    reach(U,T) :- now(T), U=T-2..T.

    i(T) :- w1d_z(T).

    w1d_z(T) :- z(U), now(T), U >= T - 1, U <= T.

    #show a/1.
    #show z/1.
    #show i/1.
    """.stripMargin

  val aspExpressions = aspProgram.split('\n') toSet

  val z = Atom("z")
  val w1d_a = Atom("w1d_a")
  val a = Atom("a")
  val i = Atom("i")

  val t0 = TimePoint(0)
  val t1 = TimePoint(1)
  val t2 = TimePoint(2)
  val t3 = TimePoint(3)
  val t4 = TimePoint(4)

  val U = TimeVariable("U")

  val larsProgram = Program.from(
    z(U + 1) <= WindowAtom(SlidingTimeWindow(2), At(U), a),
    i <= W(1, Diamond, z)
  )


  def evaluation = {
    //    val e = AspPullEvaluationEngine(AspEvaluationEngine(StreamingClingoInterpreter(aspExpressions)))
    val e = BuildEngine.withProgram(larsProgram).useAsp().withClingo().use().usePull().start()

    e.append(t1)(a)

    e
  }

  "Given 't1 -> a' " should "not lead to z at t0" in {
    evaluation.evaluate(t0).get shouldNot contain(z("0"))
  }

  it should "not lead to z at t1" in {
    evaluation.evaluate(t1).get.value shouldNot contain(z("1"))
  }

  it should "lead to z and i at t2" in {
    evaluation.evaluate(t2).get.value should contain allOf(z("2"), i("2"))
  }
  it should "not lead to z but to i at t3" in {
    val result = evaluation.evaluate(t3).get.value

    result should contain(i("3"))
    result shouldNot contain(z("3"))
  }

  it should "not lead to z or i at t4" in {
    val result = evaluation.evaluate(t4).get.value

    result shouldNot contain allOf(i("4"), z("4"))
  }
}
