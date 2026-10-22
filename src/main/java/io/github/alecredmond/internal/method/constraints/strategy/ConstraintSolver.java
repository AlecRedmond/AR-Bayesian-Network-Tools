package io.github.alecredmond.internal.method.constraints.strategy;

import io.github.alecredmond.export.constraints.ProbabilityConstraint;
import io.github.alecredmond.internal.application.junctiontree.Clique;
import java.util.Map;
import java.util.Set;

public interface ConstraintSolver {
  double adjustAndReturnError();

  void updateResults(Map<ProbabilityConstraint, double[]> results, int cycle, Set<Clique> cliques);
}
