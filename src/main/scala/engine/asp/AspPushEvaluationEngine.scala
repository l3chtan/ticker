package engine.asp

import core.Atom
import core.lars.TimePoint
import engine._
import engine.asp.evaluation.{AspEvaluation, AspEvaluationT, StreamingAspInterpeter}

/**
  * Created by FM on 21.04.16.
  */

case class AspPushEvaluationEngine(private val aspEvaluation: AspEvaluationT) extends EvaluationEngine {

  val atomStream: OrderedAtomStream = new OrderedAtomStream

  val cachedResults = scala.collection.mutable.HashMap[TimePoint, Result]()

  def prepare(time: TimePoint) = {
    // TODO: decide if we want to use evaluateUntil or the whole stream
    val result = aspEvaluation(time, atomStream.evaluateUntil(time))

    cachedResults.put(time, result)
  }

  def evaluate(time: TimePoint) = {
    cachedResults.getOrElse(time, EmptyResult)
  }

  override def append(time: TimePoint)(atoms: Atom*): Unit = {
    atomStream.append(time)(atoms.toSet)
    // TODO: implement invalidation of result
    // a results.remove(time) is probably not enough
    prepare(time)
  }
}
