package io.github.alecredmond.internal.method.cliquegraph;

import io.github.alecredmond.export.inference.direct.Clique;
import io.github.alecredmond.internal.method.solver.constraint.strategy.ConstraintSolver;
import java.util.List;
import java.util.Map;

public interface SolverGraph {
  Map<Clique, List<ConstraintSolver>> getSolversPerClique();

  void writeTablesToNetwork();

  void sumTransfer(Clique clique);

  void normalizeTables();
}
