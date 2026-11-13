package io.github.alecredmond.internal.method.solver.constraint.strategy;

import io.github.alecredmond.export.inference.direct.Clique;
import io.github.alecredmond.export.solver.constraint.ProbabilityConstraint;
import java.util.Map;
import java.util.Set;

public interface ConstraintSolver {
  double adjustAndReturnError();

  void updateResults(Map<ProbabilityConstraint, double[]> results, int cycle, Set<Clique> cliques);
}
