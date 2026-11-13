package io.github.alecredmond.internal.method.solver.constraint.types.jointconstraint;

import io.github.alecredmond.export.solver.constraint.JointProbabilityConstraint;
import io.github.alecredmond.export.solver.constraint.ProbabilityConstraint;
import io.github.alecredmond.export.solver.constraint.serialized.SerializedJointProbabilityConstraint;
import io.github.alecredmond.export.solver.constraint.serialized.SerializedProbabilityConstraint;
import io.github.alecredmond.internal.method.solver.constraint.strategy.ConstraintStrategy;
import lombok.Getter;

@Getter
public class JointConstraintStrategy implements ConstraintStrategy<JointProbabilityConstraint> {
  private final JointConstraintValidator constraintValidator;
  private final JointConstraintSerializer constraintSerializer;

  public JointConstraintStrategy() {
    constraintValidator = new JointConstraintValidator();
    constraintSerializer = new JointConstraintSerializer();
  }

  @Override
  public boolean constraintIsInstance(ProbabilityConstraint constraint) {
    return constraint instanceof JointProbabilityConstraint;
  }

  @Override
  public boolean serializedIsInstance(SerializedProbabilityConstraint serialized) {
    return serialized instanceof SerializedJointProbabilityConstraint;
  }

}
