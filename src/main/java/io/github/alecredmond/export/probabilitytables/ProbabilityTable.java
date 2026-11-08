package io.github.alecredmond.export.probabilitytables;

import io.github.alecredmond.export.node.Node;
import io.github.alecredmond.export.node.NodeState;
import java.util.Collection;
import java.util.Set;

/**
 * A generic probability table for use within a Bayesian network, solver, or inference engine. Each
 * table holds a reference to its event and condition nodes, a {@link ProbabilityVector} mapping the
 * Cartesian product of all node states to a backing array, and a query tool.
 *
 * <p>Instances of this interface are not thread-safe. External synchronization is required for
 * concurrent access.
 *
 * @see NetworkTable
 * @see ObservedTable
 * @author Alec Redmond
 */
public interface ProbabilityTable {

  /**
   * Returns the {@link ProbabilityVector} which stores probability values for this instance. A
   * {@link ProbabilityVector} contains information used to map all possible {@link NodeState}
   * combinations within this table to an index in a double array containing the associated
   * probability value.
   *
   * @return the probability vector for this table.
   */
  ProbabilityVector getVector();

  /**
   * Returns the complete, unmodifiable set of all {@link Node}s used within this table, including
   * both conditions and events.
   *
   * @return a set of every node in this table.
   */
  Set<Node> getNodes();

  /**
   * Returns the unmodifiable set of event {@link Node}s used within this table. Events represent
   * {@code E} in the expression {@code P(E|C)}, or the child {@code X} in network CPT expressions
   * {@code P(X|Pa(X))}.
   *
   * @return a set of every event node in this table.
   */
  Set<Node> getEvents();

  /**
   * Returns the unmodifiable set of condition {@link Node}s used within this table. Conditions
   * represent {@code C} in the expression {@code P(E|C)}, or the parents {@code Pa(X)} in network
   * CPT expressions {@code P(X|Pa(X))}.
   *
   * @return a set of every condition node in this table.
   */
  Set<Node> getConditions();

  /**
   * Returns the structural name of this table. Names typically follow the pattern {@code
   * P(E1,...,En|C1,...,Cm)}, where {@code E} represents event node identifiers and {@code C}
   * represents condition node identifiers.
   *
   * @return the serialized name of this table.
   */
  String getTableName();

  /**
   * Returns the flat probability array backing this table. The array size matches the Cartesian
   * product of all {@link NodeState} combinations within this table.
   *
   * @return the underlying double array containing raw probabilities.
   */
  double[] getProbabilities();

  /**
   * Returns the probability associated with all the given states in the form {@code
   * P(e1,...,en|c1,...,cm)} where {@code e1,...,en} are the event states, and {@code c1,...,cm} the
   * condition states. The input collection {@code {e1,...,en,c1,...,cn}} is not required to be
   * ordered.
   *
   * <p><b>This method is affected by Safe Mode.</b> When Safe Mode is enabled, this requires one
   * {@link NodeState} for every {@link Node} within the table, otherwise {@code null} will be
   * returned.
   *
   * @param states the collection of all states to be queried.
   * @return the probability associated with the input states, or {@code null} if safe mode
   *     validation fails.
   */
  Double getProbability(Collection<NodeState> states);

  /**
   * Builds a copy of this {@code ProbabilityTable}. This deep-copies everything except the {@link
   * Node} and {@link NodeState} values, which maintain the original references.
   *
   * @return a copy of the current table.
   */
  ProbabilityTable copyTable();

  /**
   * Normalizes this {@code ProbabilityTable}. For non-conditional tables, this will adjust the
   * probability array so that all values sum to 1. For conditional tables, the Cartesian product of
   * the event states will sum to 1 for each combination of condition states.
   */
  void normalizeTable();

  /**
   * Sets the safe mode flag for this {@code ProbabilityTable} (default: {@code true}). Safe mode
   * runs a validation check on any method that processes a {@link NodeState} collection to ensure
   * that every {@link Node} relevant to the method is queried. For example, the method {@link
   * #getProbability(Collection)} requires one {@link NodeState} for every {@link Node} in {@link
   * #getNodes()}.
   *
   * <p>This validation check accounts for the majority of compute time in most of these methods,
   * and is unnecessary in situations where the presence of all relevant states can be assured.
   * Under these circumstances, safe mode may be turned off to bypass this check.
   *
   * @param safeMode {@code true} to validate {@link NodeState} inputs, otherwise {@code false}.
   */
  void setSafeMode(boolean safeMode);
}
