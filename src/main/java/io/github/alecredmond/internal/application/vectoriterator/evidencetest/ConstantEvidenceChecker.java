package io.github.alecredmond.internal.application.vectoriterator.evidencetest;

import io.github.alecredmond.export.cartesianvector.CartesianState;
import io.github.alecredmond.export.cartesianvector.CartesianVariable;
import io.github.alecredmond.internal.application.vectoriterator.CartesianOdometer;
import java.util.function.Function;

public class ConstantEvidenceChecker<
        N extends CartesianVariable, S extends CartesianState, T extends CartesianOdometer<N, S, ?>>
    extends OdometerEvidenceChecker<N, S, T> {
  private boolean hasRunOnce = false;

  public ConstantEvidenceChecker(T odometer, Function<N, boolean[]> evidencePerNode) {
    super(odometer, evidencePerNode);
  }

  @Override
  public ResetLogicType getResetLogicType() {
    return ResetLogicType.CONSTANT;
  }

  @Override
  public void reset() {
    if (hasRunOnce) return;
    super.commonResetLogic();
    hasRunOnce = true;
  }
}
