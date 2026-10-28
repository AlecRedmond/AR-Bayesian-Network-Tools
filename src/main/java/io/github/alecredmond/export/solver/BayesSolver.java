package io.github.alecredmond.export.solver;

import io.github.alecredmond.export.network.BayesianNetwork;
import java.util.Optional;

public interface BayesSolver {
  Optional<BayesianNetwork> solveNetwork(BayesianNetwork bayesianNetwork);
}
