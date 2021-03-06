package evaluation

import core.lars.{Diamond, LarsProgram, UserDefinedLarsRule}
import core.{BuilderCollection, not}
import fixtures._
import core.lars._
import org.scalatest.Matchers._
import org.scalatest.OptionValues._
import org.scalatest.Inspectors._


/**
  * Created by FM on 02.06.16.
  */
class JtmsEssenceSimpleWindowSample extends ConfigurableEvaluationSpec with TimeTestFixtures with TmsDirectPolicyEngine {
  val program = LarsProgram.from(
    a <= W(1, Diamond, c),
    c <= W(1, Diamond, a),
    b <= not(W(1, Diamond, a)),
    d <= W(1, Diamond, b),
    d <= W(1, Diamond, c)
  )


  "An empty data-stream" should "lead to model b,d" in {
    evaluationEngine.evaluate(t0).get.value should contain allOf(b, d)
  }
  "A data-stream with {1 -> a}" should "lead to (a, c, d)" in {
    evaluationEngine.append(t1)(a)

    evaluationEngine.evaluate(t1).get.value should contain allOf(a, c, d)
    evaluationEngine.evaluate(t2).get.value should contain allOf(a, c, d)
  }

  "A stream with alternating 'a' inputs" should "lead to (a, c, d) at all time points" in pendingWithTms("cycle between a <-> c"){
    (1 to 100 by 2) foreach (evaluationEngine.append(_)(a))

    assume(Set(b, d).subsetOf(evaluationEngine.evaluate(t0).get.value))
    assume(Set(a, c, d).subsetOf(evaluationEngine.evaluate(t1).get.value))

    forAll(1 to 100) {
      i => evaluationEngine.evaluate(i).get.value should contain allOf(a, c, d)
    }
  }

}

class JtmsEssenceTests extends  RunWithAllImplementations(new JtmsEssenceSimpleWindowSample)