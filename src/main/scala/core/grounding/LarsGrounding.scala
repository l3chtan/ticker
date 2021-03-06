package core.grounding

import core.lars.LarsProgram

/**
  * applicable only to lars programs without "@"
  *
  * Created by hb on 8/21/16.
  */
case class LarsGrounding(program: LarsProgram) {

  val inspect = StaticProgramInspection.forLars(program)
  val grounder = GrounderInstance.forLars(inspect)
  val groundRules = program.rules flatMap (grounder.ground(_))
  val groundProgram = LarsProgram(groundRules)

}

