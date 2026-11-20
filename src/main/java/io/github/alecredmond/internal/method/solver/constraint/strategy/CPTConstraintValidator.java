package io.github.alecredmond.internal.method.solver.constraint.strategy;

import io.github.alecredmond.export.solver.constraint.ProbabilityConstraint;

public interface CPTConstraintValidator<
        P extends ProbabilityConstraint, V extends ValidatedConstraint<P>>
    extends ConstraintValidator<P, V> {
  V validateCPTConstraint(P constraint);
}
