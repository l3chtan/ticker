package engine.config

import clingo.reactive.ReactiveClingoProgram
import clingo.{ClingoConversion, ClingoProgramWithLars, ClingoWrapper}
import core.lars.{EngineTimeUnit, LarsProgram}
import engine.EvaluationEngine
import engine.asp._
import engine.asp.oneshot._
import engine.asp.reactive.ReactiveEvaluationEngine
import engine.asp.tms.TmsEvaluationEngine
import engine.asp.tms.policies.{ImmediatelyAddRemovePolicy, TmsPolicy}
import engine.config.EvaluationModifier.EvaluationModifier
import engine.config.EvaluationTypes.EvaluationTypes
import jtms.JtmsUpdateAlgorithm
import jtms.algorithms.JtmsGreedy
import jtms.networks.OptimizedNetwork

import scala.concurrent.duration._
import scala.util.Random

/**
  * Created by FM on 14.05.16.
  */
object BuildEngine {
  def withProgram(program: LarsProgram) = EngineEvaluationConfiguration(program)
}

case class EngineEvaluationConfiguration(larsProgram: LarsProgram, withTickSize: EngineTimeUnit = 1 second) {

  def withConfiguration(evaluationType: EvaluationTypes, evaluationModifier: EvaluationModifier) = ArgumentBasedConfiguration(larsProgram, withTickSize).build(evaluationType, evaluationModifier)

  //TODO hb: assuming correct understanding: due to the new mapping, we should simply have a "LarsToAsp" mapping, since the result
  //is no longer "pinned" (in the sense that only some atoms get an additional time argument)
  def configure() = AspEngineEvaluationConfiguration(larsProgram, withTickSize)

  def withTickSize(tickSize: EngineTimeUnit) = EngineEvaluationConfiguration(larsProgram, tickSize)
}

//TODO hb name misleading: if we use TMS, why would we call it "AspEngine"? the name hints at something like clingo or dlv
case class AspEngineEvaluationConfiguration(program: LarsProgram, withTickSize: EngineTimeUnit) {

  private val aspMapped = PlainLarsToAspMapper(withTickSize)(program)
  private val reactiveMapped = PlainLarsToReactiveMapper(withTickSize)(program)

  def withClingo() = EvaluationModeConfiguration(ClingoConversion.fromLars(aspMapped))

  def withReactive() = ReactiveClingoConfiguration(reactiveMapped)

  def withTms(): TmsConfiguration = {
    TmsConfiguration(aspMapped)
  }

}

case class TmsConfiguration(pinnedProgram: LarsProgramEncoding, policy: TmsPolicy = ImmediatelyAddRemovePolicy(JtmsGreedy(new OptimizedNetwork(), new Random))) {

  def withRandom(random: Random) = TmsConfiguration(pinnedProgram, ImmediatelyAddRemovePolicy(JtmsGreedy(new OptimizedNetwork(), random)))

  def useTms(jtms: JtmsUpdateAlgorithm) = TmsConfiguration(pinnedProgram, ImmediatelyAddRemovePolicy(jtms))

  def withPolicy(tmsPolicy: TmsPolicy) = TmsConfiguration(pinnedProgram, tmsPolicy)

}

object TmsConfiguration {
  implicit def toEvaluationModeConfig(config: TmsConfiguration): StartableEngineConfiguration = StartableEngineConfiguration(TmsEvaluationEngine(config.pinnedProgram, config.policy))
}

case class EvaluationModeConfiguration(clingoProgram: ClingoProgramWithLars) {

  def use(evaluationMode: EvaluationMode = Direct) = {
    val aspEvaluation = buildEvaluationMode(OneShotEvaluationEngine(clingoProgram, StreamingClingoInterpreter(clingoProgram)), evaluationMode)
    EvaluationStrategyConfiguration(aspEvaluation)
  }

  private def buildEvaluationMode(aspEvaluation: OneShotEvaluation, evaluationMode: EvaluationMode) = evaluationMode match {
    case UseFuture(waitingAtMost: Duration) => FutureStreamingAspInterpreter(aspEvaluation, waitingAtMost)
    case _ => aspEvaluation
  }
}

case class EvaluationStrategyConfiguration(aspEvaluation: OneShotEvaluation) {

  def usePull() = StartableEngineConfiguration(AspPullEvaluationEngine(aspEvaluation))

  def usePush() = StartableEngineConfiguration(AspPushEvaluationEngine(aspEvaluation))

}

case class ReactiveClingoConfiguration(program: LarsProgramEncoding, wrapper: ClingoWrapper = ClingoWrapper()) {
  def withWrapper(wrapper: ClingoWrapper) = ReactiveClingoConfiguration(program, wrapper)

  def startable() = StartableEngineConfiguration(ReactiveEvaluationEngine(program, wrapper))
}

object ReactiveClingoConfiguration {
  implicit def toStartable(config: ReactiveClingoConfiguration): StartableEngineConfiguration = config.startable()
}


case class StartableEngineConfiguration(evaluationEngine: EvaluationEngine) {
  def start() = evaluationEngine
}
