package io.github.alecredmond.export.inference.direct;

import io.github.alecredmond.export.network.BayesianNetwork;
import io.github.alecredmond.internal.method.cliquegraph.factory.InferenceGraphFactorySTA;
import io.github.alecredmond.internal.method.inference.direct.AbstractInferenceEngine;

/**
 * The Single Table Algorithm (STA) for Bayesian inference. STA maps the Cartesian product of all
 * node states in the network to a single joint probability table. This has a low overhead but very
 * poor time and memory complexity scaling. There may be some benefit in very small networks (with a
 * joint Cartesian product of 2<sup>9</sup> entries or fewer) under high loads of observation
 * queries. In all other circumstances, use {@link JunctionTreeAlgorithm}. The absolute upper limit
 * for STA is a table length of 2<sup>31</sup>&minus;1 entries, the maximum length of a Java array.
 *
 * @author Alec Redmond
 * @see InferenceEngine
 */
public final class SingleTableAlgorithm extends AbstractInferenceEngine {

  /**
   * Constructs a new {@code SingleTableAlgorithm} inference engine.
   *
   * @param bayesianNetwork the {@link BayesianNetwork} for inference.
   */
  public SingleTableAlgorithm(BayesianNetwork bayesianNetwork) {
    super(bayesianNetwork, new InferenceGraphFactorySTA());
  }
}
