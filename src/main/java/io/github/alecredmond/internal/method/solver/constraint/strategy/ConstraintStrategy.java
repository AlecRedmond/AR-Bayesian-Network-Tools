package io.github.alecredmond.internal.method.solver.constraint.strategy;

import io.github.alecredmond.export.solver.constraint.ProbabilityConstraint;
import io.github.alecredmond.export.solver.constraint.serialized.SerializedProbabilityConstraint;
import io.github.alecredmond.internal.method.probabilitytables.JunctionTreeTable;
import io.github.alecredmond.internal.method.solver.constraint.base.ConstraintSolverBase;

public interface ConstraintStrategy<T extends ProbabilityConstraint> {
  default ConstraintSolver buildSolverHandler(
      JunctionTreeTable table, ProbabilityConstraint constraint) {
    return new ConstraintSolverBase(constraint, table);
  }

  ConstraintValidator<T, ?> getConstraintValidator();

  boolean constraintIsInstance(ProbabilityConstraint constraint);

  boolean serializedIsInstance(SerializedProbabilityConstraint serialized);

  ConstraintSerializer<T, ?> getConstraintSerializer();
}
