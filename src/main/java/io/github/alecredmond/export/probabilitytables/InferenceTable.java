package io.github.alecredmond.export.probabilitytables;

import io.github.alecredmond.export.inference.direct.InferenceEngine;
import io.github.alecredmond.export.node.Node;
import io.github.alecredmond.export.node.NodeState;
import java.util.Arrays;
import java.util.Collection;
import java.util.Set;
import java.util.function.Supplier;

/**
 * Represents a table of potentials that an {@link InferenceEngine} uses during inference, for
 * example in the junction tree algorithm.
 *
 * <p>An {@code InferenceTable} is not conditional. Every node in the table is treated as an event
 * node, and the table has no condition nodes.
 *
 * @author Alec Redmond
 * @see Clique
 * @see Separator
 */
public interface InferenceTable extends ProbabilityTable {

  /** Resets the potentials of this table, discarding the changes made by earlier inference. */
  void resetPotentials();

  /**
   * Returns the sum of every potential in this table.
   *
   * @return the sum of the values in the array returned by {@link #getProbabilities()}
   */
  default double sumPotentials() {
    return Arrays.stream(getProbabilities()).sum();
  }

  @Override
  default double[] getProbabilities() {
    return getVector().getProbabilities();
  }

  ProbabilityVector getVector();

  NodeState[] getStatesFromIndex(int index);

  <T extends Collection<NodeState>, U extends T> T getStatesFromIndex(
      int index, Supplier<U> collectionSupplier);

  /**
   * Returns the sum of the potentials of every entry in this table that includes all the specified
   * states.
   *
   * @param states the states that an entry must include
   * @return the sum of the matching potentials
   * @see #sumPotentials(Collection)
   */
  default double sumPotentials(NodeState... states) {
    return sumPotentials(Arrays.asList(states));
  }

  /**
   * Returns the sum of the potentials of every entry in this table that includes all the specified
   * states.
   *
   * @param states the states that an entry must include
   * @return the sum of the matching potentials
   */
  double sumPotentials(Collection<NodeState> states);

  Set<Node> getNodes();

  /**
   * {@inheritDoc}
   *
   * <p>An inference table is not conditional, so this implementation always returns an empty set.
   */
  @Override
  default Set<Node> getConditions() {
    return Set.of();
  }

  /**
   * {@inheritDoc}
   *
   * <p>Every node in an inference table is an event node, so this implementation returns the same
   * set as {@link #getNodes()}.
   */
  @Override
  default Set<Node> getEvents() {
    return getNodes();
  }
}
