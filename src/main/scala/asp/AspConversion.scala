package asp

import core._

/**
  * Created by FM on 22.02.16.
  */
object AspConversion {

  def apply(program: Program): Set[AspExpression] = {
    program.rules.map(apply).toSet
  }

  def apply(rule: Rule): AspExpression = {
    if (rule.body.isEmpty) {
      return AspExpression(apply(rule.head) + '.')
    } else {
      val iParts = rule.pos.map(apply)
      val oParts = rule.neg.map(apply).map("not " + _)

      val parts = iParts ++ oParts

      val expression = parts.mkString(apply(rule.head) + " :- ", ", ", ".").trim

      AspExpression(expression)
    }
  }

  def apply(atom: Atom): String = {
    val atomName = atom match {
      case x: ContradictionAtom => return ""
      case UserDefinedAtom(caption) => caption
      case _ => atom.toString
    }

    if (atomName.head.isUpper)
      throw new IllegalArgumentException("Currently only constants are allowed in an ASP expression. In ASP a constant starts with an lower-case character. You provided " + atom)

    if (atomName exists (_.isWhitespace))
      throw new IllegalArgumentException("Constants in ASP cannot contain a whitespace. You provided " + atom)

    if (!(atomName matches ("^[a-zA-Z0-9_]*$")))
      throw new IllegalArgumentException("Constants in ASP cannot contain illegal characters!. You provided " + atom)

    atomName
  }
}
