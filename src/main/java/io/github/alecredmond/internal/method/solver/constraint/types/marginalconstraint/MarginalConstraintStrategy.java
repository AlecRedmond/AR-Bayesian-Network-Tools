package io.github.alecredmond.internal.method.solver.constraint.types.marginalconstraint;

import io.github.alecredmond.export.solver.constraint.MarginalConstraint;
import io.github.alecredmond.export.solver.constraint.ProbabilityConstraint;
import io.github.alecredmond.export.solver.constraint.serialized.SerializedMarginalConstraint;
import io.github.alecredmond.export.solver.constraint.serialized.SerializedProbabilityConstraint;
import io.github.alecredmond.internal.method.solver.constraint.strategy.ConstraintStrategy;
import lombok.Getter;

@Getter
public class MarginalConstraintStrategy implements ConstraintStrategy<MarginalConstraint> {
  private final MarginalConstraintValidator constraintValidator;
  private final MarginalConstraintSerializer constraintSerializer;

  public MarginalConstraintStrategy() {
    constraintSerializer = new MarginalConstraintSerializer();
    constraintValidator = new MarginalConstraintValidator();
  }

  @Override
  public boolean constraintIsInstance(ProbabilityConstraint constraint) {
    return constraint instanceof MarginalConstraint;
  }

  @Override
  public boolean serializedIsInstance(SerializedProbabilityConstraint serialized) {
    return serialized instanceof SerializedMarginalConstraint;
  }

}
