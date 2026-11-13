package io.github.alecredmond.export.solver;

import io.github.alecredmond.export.network.BayesianNetwork;
import io.github.alecredmond.export.solver.ipfp.IPFPJunctionTreeSolver;
import java.util.Optional;

public interface BayesSolver {
  static BayesSolver create() {
    return new MultiStageSolver(new IPFPJunctionTreeSolver());
  }

  Optional<BayesianNetwork> solveNetwork(BayesianNetwork bayesianNetwork);
}
