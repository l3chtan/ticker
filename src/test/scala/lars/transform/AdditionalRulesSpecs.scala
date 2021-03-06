package lars.transform

import core.Atom
import core.lars._

/**
  * Created by FM on 05.05.16.
  */
class AdditionalRulesSpecs extends TransformLarsSpec {

  def Fluent(atom: Atom) = WindowAtom(FluentWindow, Diamond, atom)

  "A diamond-window atom" should "be transformed into some rule" in {
    assert(allWindowRules(WindowAtom(SlidingTimeWindow(1), Diamond, a)).nonEmpty)
  }
  "A box-window atom" should "be transformed into some rule" in {
    assert(allWindowRules(WindowAtom(SlidingTimeWindow(1), Box, a)).nonEmpty)
  }
  "A fluent-window atom" should "be transformed into some rule" in {
    assert(allWindowRules(Fluent(a)).nonEmpty)
  }
}
