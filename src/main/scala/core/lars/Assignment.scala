package core.lars

import core.{Argument, Value, Variable}

/**
  * Created by hb on 8/21/16.
  */
case class Assignment(binding: Map[Variable,Value]) {
  def apply(arg: Argument): Option[Value] =
    arg match {
      case v: Variable => binding get v
      case _ => None
    }
}