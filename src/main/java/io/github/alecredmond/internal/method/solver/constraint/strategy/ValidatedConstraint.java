package io.github.alecredmond.internal.method.solver.constraint.strategy;

import io.github.alecredmond.export.solver.constraint.ProbabilityConstraint;

public interface ValidatedConstraint<P extends ProbabilityConstraint> {
  P getConstraint();
}
