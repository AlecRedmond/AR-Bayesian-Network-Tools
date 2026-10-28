package io.github.alecredmond.internal.method.cliquegraph.factory;

import io.github.alecredmond.export.network.BayesianNetwork;
import io.github.alecredmond.internal.method.cliquegraph.SolverGraph;

public interface SolverGraphFactory {
  SolverGraph build(BayesianNetwork bayesianNetwork);
}
