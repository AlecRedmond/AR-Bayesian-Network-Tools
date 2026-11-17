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
 * maps the probability over all {@link NodeState} values of a single {@link Node}, conditional on
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
   * @return the {@link Node} measured by this CPT.
   */
  Node getNetworkNode();

  /**
   * Builds a copy of this {@code NetworkTable}. This deep-copies everything except the {@link Node}
   * and {@link NodeState} values, which maintain the original references.
   *
   * @return a copy of the current table.
   */
  @Override
  NetworkTable copyTable();

  /**
   * Returns a cross-section of probabilities for every {@link NodeState} in the Network {@link
   * Node}, under the given conditions.
   *
   * <p><b>This method is affected by Safe Mode.</b> When Safe Mode is enabled, the input must
   * contain a {@link NodeState} for every {@link Node} value in {@link
   * NetworkTable#getConditions()}, otherwise the method will return an empty {@link HashMap}.
   *
   * @param condition a collection of {@link NodeState} values which define a condition within the
   *     table
   * @return a new map of each Network node's {@link NodeState} and its probability under the given
   *     conditions, or an empty {@link HashMap} if Safe Mode validation failed.
   */
  Map<NodeState, Double> getConditionalProb(Collection<NodeState> condition);

  /**
   * Returns every {@link CptEntry} within this {@code NetworkTable}. A {@link CptEntry} contains
   * details of the measured {@link NodeState} within the table's Network {@link Node}, any
   * conditioning states acting upon it, its conditional probability, and a reference to its index
   * in the probability {@code double[]} in this {@code NetworkTable}.
   *
   * @return A new {@link CptEntry} array, ordered identically to this {@code NetworkTable}'s
   *     probability array.
   */
  CptEntry[] getEntries();

  CptRow[] getRows();
}
