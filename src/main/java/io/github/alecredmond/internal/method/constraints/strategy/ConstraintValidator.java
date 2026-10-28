package io.github.alecredmond.internal.method.constraints.strategy;

import io.github.alecredmond.export.constraints.ProbabilityConstraint;
import io.github.alecredmond.export.node.Node;
import io.github.alecredmond.internal.application.constraint.ConstraintBuilderData;
import java.util.Set;

public interface ConstraintValidator<
    P extends ProbabilityConstraint, V extends ValidatedConstraint<P>> {
  default V validateConstraint(
      Set<Node> allDeclaredNodes,
      Set<ProbabilityConstraint> declaredConstraints,
      ProbabilityConstraint constraint) {
    return validateConstraint(
        new ConstraintBuilderData(allDeclaredNodes, declaredConstraints, constraint));
  }

  V validateConstraint(ConstraintBuilderData data);

  Class<P> getConstraintClass();

  boolean checkInputsValid(ConstraintBuilderData data);

  V buildFromInputs(ConstraintBuilderData data);
}
