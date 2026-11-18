package io.github.alecredmond.export.network.cptdefinition;

import io.github.alecredmond.export.network.BayesianNetworkBuilder;
import io.github.alecredmond.export.node.Node;
import io.github.alecredmond.export.node.NodeState;
import io.github.alecredmond.export.probabilitytables.ProbabilityVector;

/**
 * A structure for a user-defined Conditional Probability Table (CPT) in {@link
 * BayesianNetworkBuilder}. This provides a structure for mapping each permutation of {@link
 * NodeState}s in a CPT to its conditional probability.
 */
public interface CptDefinition {

  /**
   * Returns the CPT as a {@link ProbabilityVector}.
   *
   * @return the {@link ProbabilityVector} indexing this CPT.
   */
  ProbabilityVector getVector();

  /**
   * Returns the measured {@link Node} in the CPT. This is {@code X} in {@code P(X|Pa(X))}.
   *
   * @return the measured {@link Node}.
   */
  Node measuredNode();
}
