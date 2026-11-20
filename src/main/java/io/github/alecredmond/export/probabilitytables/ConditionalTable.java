package io.github.alecredmond.export.probabilitytables;

import io.github.alecredmond.export.network.BayesianNetwork;
import io.github.alecredmond.export.node.Node;
import io.github.alecredmond.export.node.NodeState;

/**
 * A conditional probability table (CPT) for a non-root {@link Node} within a {@link
 * BayesianNetwork}. A CPT maps the probability over all {@link NodeState} values of a single {@code
 * Node}, conditional on the states of its parents: {@code P(X|Pa(X))}.
 *
 * <p>Instances of this interface are not thread-safe. External synchronization is required for
 * concurrent access.
 *
 * @author Alec Redmond
 * @see ProbabilityTable
 * @see NetworkTable
 * @see RootNodeTable
 */
public interface ConditionalTable extends NetworkTable {

  /**
   * Builds a copy of this {@code ConditionalTable}. This deep-copies everything except the {@link
   * Node} and {@link NodeState} values, which maintain the original references.
   *
   * @return a copy of the current table
   */
  @Override
  ConditionalTable copyTable();
}
