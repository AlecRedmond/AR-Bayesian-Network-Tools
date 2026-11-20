package io.github.alecredmond.export.network.cptdefinition;

import io.github.alecredmond.export.network.BayesianNetworkBuilder;
import io.github.alecredmond.export.node.Node;
import io.github.alecredmond.export.node.NodeState;
import io.github.alecredmond.export.probabilitytables.ProbabilityVector;

/**
 * Defines a user-supplied conditional probability table (CPT) for use in a {@link
 * BayesianNetworkBuilder}.
 *
 * <p>A definition maps each permutation of {@link NodeState}s in the CPT of a single {@link Node}
 * to its conditional probability.
 *
 * @author Alec Redmond
 * @see OrderedCptDefinition
 * @see HalfCptDefinition
 * @see BayesianNetworkBuilder#defineCPT(CptDefinition)
 */
public interface CptDefinition {

  /**
   * Returns the CPT as a {@link ProbabilityVector}.
   *
   * @return the {@code ProbabilityVector} that indexes this CPT
   */
  ProbabilityVector getVector();

  /**
   * Returns the measured {@link Node} of the CPT. This is {@code X} in {@code P(X|Pa(X))}.
   *
   * @return the measured node
   */
  Node measuredNode();
}
