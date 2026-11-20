package io.github.alecredmond.export.probabilitytables;

import io.github.alecredmond.export.network.BayesianNetwork;
import io.github.alecredmond.export.node.Node;
import io.github.alecredmond.export.node.NodeState;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * An unconditional probability table for a root {@link Node} within a {@link BayesianNetwork}. Root
 * nodes have no parents; therefore, this table contains exactly one entry per {@link NodeState} of
 * the associated node, defining the explicit prior probability distribution of each state.
 *
 * <p>Instances of this interface are not thread-safe. External synchronization is required for
 * concurrent access.
 *
 * @author Alec Redmond
 * @see ProbabilityTable
 * @see NetworkTable
 * @see ConditionalTable
 */
public interface RootNodeTable extends NetworkTable {

  /**
   * Builds a copy of this {@code RootNodeTable}. This deep-copies everything except the {@link
   * Node} and {@link NodeState} values, which maintain the original references.
   *
   * @return a copy of the current table
   */
  RootNodeTable copyTable();

  /**
   * Returns the probability associated with a state of {@link #getNetworkNode()} in the form {@code
   * P(X=x)}, where {@code X} is the root node and {@code x} is one of its possible states.
   *
   * <p><b>This method is affected by Safe Mode.</b> When Safe Mode is enabled, the given {@link
   * NodeState} must be in the state list of the root node, otherwise {@code null} is returned.
   *
   * @param state the state of the root node to be queried
   * @return the probability of the specified state, or {@code null} if Safe Mode validation fails
   */
  Double getProbability(NodeState state);

  /**
   * Returns a map of each {@link NodeState} of the root {@link Node}, paired to its associated
   * probability value {@code P(X=x)}. The map is a {@link LinkedHashMap} that maintains the same
   * order as the {@code NodeState} list of the root node.
   *
   * @return a new {@code LinkedHashMap} linking each state of the root node to its probability
   */
  Map<NodeState, Double> buildProbabilityMap();
}
