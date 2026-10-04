package io.github.alecredmond.internal.application.vectoriterator.evidencetest;

import io.github.alecredmond.export.cartesianvector.CartesianState;
import io.github.alecredmond.export.cartesianvector.CartesianVariable;
import io.github.alecredmond.internal.application.vectoriterator.CartesianOdometer;
import java.util.function.Function;

public class VariableEvidenceChecker<
        N extends CartesianVariable, S extends CartesianState, T extends CartesianOdometer<N, S, ?>>
    extends OdometerEvidenceChecker<N, S, T> {

  public VariableEvidenceChecker(T odometer, Function<N, boolean[]> evidencePerNode) {
    super(odometer, evidencePerNode);
  }

  @Override
  public ResetLogicType getResetLogicType() {
    return ResetLogicType.VARIABLE;
  }

  @Override
  public void reset() {
    super.commonResetLogic();
  }
}
