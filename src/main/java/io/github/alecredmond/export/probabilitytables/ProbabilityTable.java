package io.github.alecredmond.export.probabilitytables;

import io.github.alecredmond.export.node.Node;
import io.github.alecredmond.export.node.NodeState;
import io.github.alecredmond.export.probabilitytables.tableentry.TableEntry;
import java.util.Collection;
import java.util.Set;
import java.util.function.Supplier;

/**
 * A generic probability table for use within a Bayesian network, solver, or inference engine. Each
 * table holds a reference to its event and condition nodes, a {@link ProbabilityVector} that maps
 * the Cartesian product of all node states to a backing array, and query methods.
 *
 * <p>Instances of this interface are not thread-safe. External synchronization is required for
 * concurrent access.
 *
 * @author Alec Redmond
 * @see NetworkTable
 * @see ObservedTable
 * @see InferenceTable
 */
public interface ProbabilityTable {

  /**
   * Returns the {@link ProbabilityVector} that stores the probability values for this table. A
   * {@code ProbabilityVector} contains the information used to map every possible {@link NodeState}
   * combination within this table to an index in a {@code double} array that holds the associated
   * probability value.
   *
   * @return the probability vector of this table
   */
  ProbabilityVector getVector();

  /**
   * Returns the complete, unmodifiable set of all {@link Node}s used within this table, including
   * both conditions and events.
   *
   * @return a set of every node in this table
   */
  Set<Node> getNodes();

  /**
   * Returns the unmodifiable set of event {@link Node}s used within this table. Events represent
   * {@code E} in the expression {@code P(E|C)}, or the child {@code X} in network CPT expressions
   * {@code P(X|Pa(X))}.
   *
   * @return a set of every event node in this table
   */
  Set<Node> getEvents();

  /**
   * Returns the unmodifiable set of condition {@link Node}s used within this table. Conditions
   * represent {@code C} in the expression {@code P(E|C)}, or the parents {@code Pa(X)} in network
   * CPT expressions {@code P(X|Pa(X))}.
   *
   * @return a set of every condition node in this table
   */
  Set<Node> getConditions();

  /**
   * Returns the structural name of this table. Names typically follow the pattern {@code
   * P(E1,...,En|C1,...,Cm)}, where {@code E} represents event node names and {@code C} represents
   * condition node names.
   *
   * @return the name of this table
   */
  String getTableName();

  /**
   * Returns the flat probability array backing this table. The array size matches the Cartesian
   * product of all {@link NodeState} combinations within this table.
   *
   * @return the underlying {@code double} array that contains the raw probabilities
   */
  double[] getProbabilities();

  /**
   * Returns the probability associated with all the given states in the form {@code
   * P(e1,...,en|c1,...,cm)}, where {@code e1,...,en} are the event states and {@code c1,...,cm} are
   * the condition states. The input collection {@code {e1,...,en,c1,...,cm}} is not required to be
   * ordered.
   *
   * <p><b>This method is affected by Safe Mode.</b> When Safe Mode is enabled, this requires one
   * {@link NodeState} for every {@link Node} within the table, otherwise {@code null} is returned.
   *
   * @param states the collection of all states to be queried
   * @return the probability associated with the input states, or {@code null} if Safe Mode
   *     validation fails
   */
  Double getProbability(Collection<NodeState> states);

  /**
   * Returns the {@link NodeState}s that the specified index of the probability array represents.
   *
   * <p>The result contains one state for each node in this table, in the order of {@link
   * ProbabilityVector#getOrderedNodes()}.
   *
   * @param index the index in the array returned by {@link #getProbabilities()}
   * @return an array of the states at the specified index
   */
  NodeState[] getStatesFromIndex(int index);

  /**
   * Returns the {@link NodeState}s that the specified index of the probability array represents,
   * collected in a collection supplied by the caller.
   *
   * @param <T> the base collection type of the result
   * @param <U> the concrete collection type created by the supplier
   * @param index the index in the array returned by {@link #getProbabilities()}
   * @param collectionSupplier a supplier of the empty collection that receives the states
   * @return the collection created by the supplier, containing the states at the specified index
   * @see #getStatesFromIndex(int)
   */
  <T extends Collection<NodeState>, U extends T> T getStatesFromIndex(
      int index, Supplier<U> collectionSupplier);

  /**
   * Normalizes this {@code ProbabilityTable}. For non-conditional tables, this adjusts the
   * probability array so that all values sum to 1. For conditional tables, the Cartesian product of
   * the event states sums to 1 for each combination of condition states.
   */
  void normalizeTable();

  /**
   * Sets the Safe Mode flag for this {@code ProbabilityTable} (default: {@code true}). Safe Mode
   * runs a validation check on any method that processes a {@link NodeState} collection, to ensure
   * that every {@link Node} relevant to the method is queried. For example, the method {@link
   * #getProbability(Collection)} requires one {@code NodeState} for every {@code Node} in {@link
   * #getNodes()}.
   *
   * <p>This validation check accounts for the majority of compute time in most of these methods,
   * and is unnecessary in situations where the presence of all relevant states can be assured.
   * Under these circumstances, Safe Mode may be turned off to bypass this check.
   *
   * @param safeMode {@code true} to validate {@code NodeState} inputs, or {@code false} to skip the
   *     validation
   */
  void setSafeMode(boolean safeMode);

  /**
   * Returns every {@link TableEntry} within this {@code ProbabilityTable}. A {@code TableEntry}
   * contains details of all measured {@link NodeState}s, a reference to its index in the
   * probability {@code double[]} of this {@code ProbabilityTable}, and the probability itself.
   *
   * @return a new {@code TableEntry} array, ordered identically to the probability array of this
   *     {@code ProbabilityTable}
   */
  TableEntry[] getEntries();
}
