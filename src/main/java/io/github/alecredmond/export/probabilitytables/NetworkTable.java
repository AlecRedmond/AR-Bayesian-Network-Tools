package io.github.alecredmond.export.probabilitytables;

import io.github.alecredmond.export.network.BayesianNetwork;
import io.github.alecredmond.export.node.Node;
import io.github.alecredmond.export.node.NodeState;
import io.github.alecredmond.export.probabilitytables.tableentry.CptEntry;
import io.github.alecredmond.export.probabilitytables.tableentry.CptRow;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * A conditional probability table (CPT) for a {@link Node} within a {@link BayesianNetwork}. A CPT
 * maps the probability over all {@link NodeState} values of a single {@code Node}, conditional on
 * the states of its parents: {@code P(X|Pa(X))}.
 *
 * <p>There are two variants of {@code NetworkTable}:
 *
 * <ul>
 *   <li>{@link ConditionalTable} for non-root nodes, conditional on their parents
 *   <li>{@link RootNodeTable} for the root nodes, which have no parents
 * </ul>
 *
 * <p>Instances of this interface are not thread-safe. External synchronization is required for
 * concurrent access.
 *
 * @author Alec Redmond
 * @see ProbabilityTable
 */
public interface NetworkTable extends CloneableTable<NetworkTable> {

  /**
   * Returns the {@link Node} within the {@link BayesianNetwork} that this CPT measures. This
   * represents the {@code X} variable within the expression {@code P(X|Pa(X))}.
   *
   * @return the {@code Node} measured by this CPT
   */
  Node getNetworkNode();

  /**
   * Builds a copy of this {@code NetworkTable}. This deep-copies everything except the {@link Node}
   * and {@link NodeState} values, which maintain the original references.
   *
   * @return a copy of the current table
   */
  @Override
  NetworkTable copyTable();

  /**
   * Returns a cross-section of probabilities for every {@link NodeState} of the network node, under
   * the given conditions.
   *
   * <p><b>This method is affected by Safe Mode.</b> When Safe Mode is enabled, the input must
   * contain a {@code NodeState} for every {@code Node} in {@link NetworkTable#getConditions()},
   * otherwise the method returns an empty {@link HashMap}.
   *
   * @param condition a collection of {@code NodeState} values that define a condition within the
   *     table
   * @return a new map of each state of the network node and its probability under the given
   *     conditions, or an empty {@code HashMap} if Safe Mode validation failed
   */
  Map<NodeState, Double> getConditionalProb(Collection<NodeState> condition);

  /**
   * Returns every {@link CptEntry} within this {@code NetworkTable}. A {@code CptEntry} contains
   * details of the measured {@link NodeState} of the network node, any conditioning states acting
   * upon it, its conditional probability, and a reference to its index in the probability {@code
   * double[]} of this {@code NetworkTable}.
   *
   * @return a new {@code CptEntry} array, ordered identically to the probability array of this
   *     {@code NetworkTable}
   */
  CptEntry[] getEntries();

  /**
   * Returns every {@link CptRow} within this {@code NetworkTable}. Each {@code CptRow} groups the
   * entries that share a single combination of condition states.
   *
   * @return an array of the rows of this table
   */
  CptRow[] getRows();
}
