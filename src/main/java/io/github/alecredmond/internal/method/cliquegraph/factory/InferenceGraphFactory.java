package io.github.alecredmond.internal.method.cliquegraph.factory;

import io.github.alecredmond.export.network.BayesianNetwork;
import io.github.alecredmond.internal.method.cliquegraph.InferenceGraph;

public interface InferenceGraphFactory {
  InferenceGraph build(BayesianNetwork bayesianNetwork);
}
