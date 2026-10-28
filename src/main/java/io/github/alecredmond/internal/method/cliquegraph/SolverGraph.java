package io.github.alecredmond.internal.method.cliquegraph;

import io.github.alecredmond.internal.application.cliquegraph.Clique;
import io.github.alecredmond.internal.method.constraints.strategy.ConstraintSolver;
import java.util.List;
import java.util.Map;

public interface SolverGraph {
  Map<Clique, List<ConstraintSolver>> getSolversPerClique();

  void writeTablesToNetwork();

  void sumTransfer(Clique clique);

  void normalizeTables();
}
