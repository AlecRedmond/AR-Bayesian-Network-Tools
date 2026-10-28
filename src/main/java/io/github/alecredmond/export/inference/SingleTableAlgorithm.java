package io.github.alecredmond.export.inference;

import io.github.alecredmond.export.network.BayesianNetwork;
import io.github.alecredmond.internal.method.cliquegraph.factory.InferenceGraphFactorySTA;
import io.github.alecredmond.internal.method.inference.InferenceEngineImpl;

/**
 * The Single Table Algorithm (STA) for Bayesian inference. STA maps the Cartesian product of all
 * node states in the network to a single joint probability table. This has a low overhead but very
 * poor time and memory complexity scaling. There may be some benefit in very small networks (with a
 * joint Cartesian product of 2<sup>9</sup> entries or fewer) under high loads of observation
 * queries. The absolute upper limit for this variant is a table length of 2<sup>31</sup>&minus;1
 * entries, the maximum length of a Java array.
 */
public class SingleTableAlgorithm extends InferenceEngineImpl {
  public SingleTableAlgorithm(BayesianNetwork network) {
    super(network, new InferenceGraphFactorySTA());
  }
}
