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
 * @see ProbabilityTable
 * @see NetworkTable
 * @see ConditionalTable
 * @author Alec Redmond
 */
public interface RootNodeTable extends NetworkTable {

  /**
   * Builds a copy of this {@code RootNodeTable}. This deep-copies everything except the {@link
   * Node} and {@link NodeState} values, which maintain the original references.
   *
   * @return a copy of the current table.
   */
  RootNodeTable copyTable();

  /**
   * Returns the probability associated with a state within {@link #getNetworkNode()} in the form:
   * <br>
   * {@code P(X=x)}<br>
   * where {@code X} is the root node and {@code x} is one of its possible states.
   *
   * <p><b>This method is affected by Safe Mode.</b> When Safe Mode is enabled, this will require
   * the given {@link NodeState} to be in the root node's state list, otherwise {@code null} will be
   * returned.
   *
   * @param state the state within the {@code RootNodeTable}'s network node to be queried.
   * @return the probability table entry associated with the input state, or {@code null} if safe
   *     mode validation fails.
   */
  Double getProbability(NodeState state);

  /**
   * Returns a map of each {@link NodeState} within the table's root {@link Node}, paired to its
   * associated probability value {@code P(X=x)}. The map is a {@link LinkedHashMap} which maintains
   * the same order as the same order as the {@link NodeState} list within the root {@link Node}.
   *
   * @return a new {@link LinkedHashMap} linking each {@link NodeState} in the root node to its
   *     probability table entry.
   */
  Map<NodeState, Double> buildProbabilityMap();
}
