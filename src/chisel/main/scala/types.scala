package DMAUtils

object types {
  case class DummyAnnotation(any: Any*) extends firrtl.annotations.NoTargetAnnotation

  type ChiselStage = circt.stage.ChiselStage

  type RunFirrtlTransformAnnotation = DummyAnnotation
  val RunFirrtlTransformAnnotation = DummyAnnotation
}
