package io.github.alecredmond.export.solver;

import io.github.alecredmond.export.network.BayesianNetwork;
import io.github.alecredmond.export.network.BayesianNetworkBuilder;
import java.util.Optional;

public interface BayesSolver {
  Optional<BayesianNetwork> solveNetwork(BayesianNetwork bayesianNetwork);

  default Optional<BayesianNetwork> solveNetwork(BayesianNetworkBuilder builder) {
    return builder.build(this);
  }
}
