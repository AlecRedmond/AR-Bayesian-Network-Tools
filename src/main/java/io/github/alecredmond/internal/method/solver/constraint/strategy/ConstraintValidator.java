package io.github.alecredmond.internal.method.solver.constraint.strategy;

import io.github.alecredmond.export.node.Node;
import io.github.alecredmond.export.solver.constraint.ProbabilityConstraint;
import io.github.alecredmond.internal.application.solver.constraint.ConstraintBuilderData;
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

  boolean checkInputsValid(ConstraintBuilderData data);

  V buildFromInputs(ConstraintBuilderData data);
}
