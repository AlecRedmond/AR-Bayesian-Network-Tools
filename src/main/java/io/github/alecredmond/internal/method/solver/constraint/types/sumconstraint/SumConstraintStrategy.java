package io.github.alecredmond.internal.method.solver.constraint.types.sumconstraint;

import io.github.alecredmond.export.solver.constraint.ProbabilityConstraint;
import io.github.alecredmond.export.solver.constraint.SumProbabilityConstraint;
import io.github.alecredmond.export.solver.constraint.serialized.SerializedProbabilityConstraint;
import io.github.alecredmond.export.solver.constraint.serialized.SerializedSumConstraint;
import io.github.alecredmond.internal.method.solver.constraint.strategy.ConstraintStrategy;
import lombok.Getter;

@Getter
public class SumConstraintStrategy implements ConstraintStrategy<SumProbabilityConstraint> {
  private final SumConstraintValidator constraintValidator;
  private final SumConstraintSerializer constraintSerializer;

  public SumConstraintStrategy() {
    constraintValidator = new SumConstraintValidator();
    constraintSerializer = new SumConstraintSerializer();
  }

  @Override
  public boolean constraintIsInstance(ProbabilityConstraint constraint) {
    return constraint instanceof SumProbabilityConstraint;
  }

  @Override
  public boolean serializedIsInstance(SerializedProbabilityConstraint serialized) {
    return serialized instanceof SerializedSumConstraint;
  }

}
