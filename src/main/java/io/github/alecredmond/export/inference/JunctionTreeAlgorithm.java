package io.github.alecredmond.export.inference;

import io.github.alecredmond.export.network.BayesianNetwork;
import io.github.alecredmond.internal.method.cliquegraph.factory.InferenceGraphFactoryJTA;
import io.github.alecredmond.internal.method.inference.InferenceEngineImpl;

/**
 * The Junction Tree Algorithm (JTA) for Bayesian Inference. JTA decomposes the network into a
 * junction tree of smaller cliques, each with its own joint sub-table, connected by separators
 * formed from their shared nodes. Observations are applied per-clique and propagated via
 * message-passing through the separators. JTA scales on the treewidth of the network, and will
 * typically yield an exponential time and memory complexity improvement over the joint table method
 * at the cost of a small overhead. It is also the only option for networks whose full joint
 * Cartesian product would exceed 2<sup>31</sup>&minus;1 entries.
 */
public class JunctionTreeAlgorithm extends InferenceEngineImpl {
  public JunctionTreeAlgorithm(BayesianNetwork bayesianNetwork) {
    super(bayesianNetwork, new InferenceGraphFactoryJTA());
  }
}
