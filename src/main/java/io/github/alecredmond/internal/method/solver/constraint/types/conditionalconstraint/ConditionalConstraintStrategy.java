package io.github.alecredmond.internal.method.solver.constraint.types.conditionalconstraint;

import io.github.alecredmond.export.solver.constraint.ConditionalConstraint;
import io.github.alecredmond.export.solver.constraint.ProbabilityConstraint;
import io.github.alecredmond.export.solver.constraint.serialized.SerializedConditionalConstraint;
import io.github.alecredmond.export.solver.constraint.serialized.SerializedProbabilityConstraint;
import io.github.alecredmond.internal.method.solver.constraint.strategy.ConstraintStrategy;
import lombok.Getter;

@Getter
public class ConditionalConstraintStrategy implements ConstraintStrategy<ConditionalConstraint> {
  private final ConditionalConstraintValidator constraintValidator;
  private final ConditionalConstraintSerializer constraintSerializer;

  public ConditionalConstraintStrategy() {
    this.constraintValidator = new ConditionalConstraintValidator();
    this.constraintSerializer = new ConditionalConstraintSerializer();
  }

  @Override
  public boolean constraintIsInstance(ProbabilityConstraint constraint) {
    return constraint instanceof ConditionalConstraint;
  }

  @Override
  public boolean serializedIsInstance(SerializedProbabilityConstraint serialized) {
    return serialized instanceof SerializedConditionalConstraint;
  }

}
